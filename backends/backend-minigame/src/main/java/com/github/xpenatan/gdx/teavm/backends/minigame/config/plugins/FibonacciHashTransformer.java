package com.github.xpenatan.gdx.teavm.backends.minigame.config.plugins;

import com.github.xpenatan.gdx.teavm.backends.minigame.utils.FibonacciHash;
import java.util.HashSet;
import java.util.Set;
import org.teavm.model.BasicBlock;
import org.teavm.model.CallLocation;
import org.teavm.model.ClassHolder;
import org.teavm.model.ClassHolderTransformer;
import org.teavm.model.ClassHolderTransformerContext;
import org.teavm.model.Instruction;
import org.teavm.model.MethodHolder;
import org.teavm.model.MethodReference;
import org.teavm.model.Program;
import org.teavm.model.ValueType;
import org.teavm.model.Variable;
import org.teavm.model.instructions.BinaryInstruction;
import org.teavm.model.instructions.BinaryOperation;
import org.teavm.model.instructions.CastNumberInstruction;
import org.teavm.model.instructions.EmptyInstruction;
import org.teavm.model.instructions.ExitInstruction;
import org.teavm.model.instructions.GetFieldInstruction;
import org.teavm.model.instructions.IntegerConstantInstruction;
import org.teavm.model.instructions.InvocationType;
import org.teavm.model.instructions.InvokeInstruction;
import org.teavm.model.instructions.JumpInstruction;
import org.teavm.model.instructions.LongConstantInstruction;
import org.teavm.model.instructions.NumericOperandType;
import org.teavm.model.text.ListingBuilder;

/**
 * Rewrites libGDX's hash collections so that a lookup allocates nothing.
 * <p>
 * Every libGDX map and set places a key with {@code (int)(hash * 0x9E3779B97F4A7C15L >>> shift)}.
 * TeaVM compiles that {@code long} arithmetic to BigInt, which allocates about 15 objects per
 * lookup (measured: about 60% of the idle JavaScript allocation of a game). This transformer
 * replaces the {@code long} part of each {@code place()} with a call to
 * {@link FibonacciHash#place(int, int)} or {@link FibonacciHash#placeLong(long, int)}, which
 * compute the identical slot with int arithmetic. The hash itself ({@code hashCode()},
 * {@code System.identityHashCode()}, the int key) and the {@code shift} field read stay as they are.
 * <p>
 * The rewrite is guarded: within {@code com.badlogic.gdx.*}, every method that uses the multiplier
 * must be a {@code place()} of exactly the known shape, and every class listed in
 * {@link #PLACE_CLASSES} must, when it is compiled, have a {@code place()} that is free of long
 * arithmetic afterwards. Anything else is a build error with the method's listing, so a libGDX
 * upgrade that changes the hashing fails loudly instead of silently allocating again. Game code
 * outside libGDX is never rewritten.
 */
public final class FibonacciHashTransformer implements ClassHolderTransformer {
    private static final String GDX_PACKAGE = "com.badlogic.gdx.";
    private static final String SHIFT_FIELD = "shift";

    /**
     * The libGDX classes that hash keys in {@code place()}; each must end up without long arithmetic. In 1.14.0
     * all of them use the multiplier (the whole gdx jar was scanned by bytecode); 1.14.2 adds LongSet and
     * changes IdentityMap to an int-only {@code identityHashCode(item) >>> shift}, which needs no rewrite.
     */
    static final Set<String> PLACE_CLASSES = Set.of(
        "com.badlogic.gdx.utils.IdentityMap",
        "com.badlogic.gdx.utils.IntFloatMap",
        "com.badlogic.gdx.utils.IntIntMap",
        "com.badlogic.gdx.utils.IntMap",
        "com.badlogic.gdx.utils.IntSet",
        "com.badlogic.gdx.utils.LongMap",
        "com.badlogic.gdx.utils.LongSet",
        "com.badlogic.gdx.utils.ObjectFloatMap",
        "com.badlogic.gdx.utils.ObjectIntMap",
        "com.badlogic.gdx.utils.ObjectLongMap",
        "com.badlogic.gdx.utils.ObjectMap",
        "com.badlogic.gdx.utils.ObjectSet");

    private static final MethodReference PLACE = new MethodReference(FibonacciHash.class.getName(), "place",
        ValueType.INTEGER, ValueType.INTEGER, ValueType.INTEGER);
    private static final MethodReference PLACE_LONG = new MethodReference(FibonacciHash.class.getName(), "placeLong",
        ValueType.LONG, ValueType.INTEGER, ValueType.INTEGER);

    @Override
    public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
        if(!cls.getName().startsWith(GDX_PACKAGE)) {
            return;
        }
        for(MethodHolder method : cls.getMethods()) {
            Program program = method.getProgram();
            if(program == null || callsFibonacciHash(program) || !usesMultiplier(program)) {
                continue;
            }
            String problem = rewrite(method, program);
            if(problem != null) {
                context.getDiagnostics().error(new CallLocation(method.getReference()),
                    "FibonacciHashTransformer: " + method.getReference() + " uses the Fibonacci hashing multiplier"
                        + " but is not the known place() shape (" + problem + "). Update the transformer for this"
                        + " libGDX version or drop it; the method listing is:\n" + listing(program));
            }
        }
        if(PLACE_CLASSES.contains(cls.getName())) {
            MethodHolder place = findPlace(cls);
            if(place == null) {
                context.getDiagnostics().error(null, "FibonacciHashTransformer: " + cls.getName()
                    + " has no int place(key) method. Update PLACE_CLASSES for this libGDX version.");
            }
            else if(usesLongArithmetic(place.getProgram())) {
                context.getDiagnostics().error(new CallLocation(place.getReference()), "FibonacciHashTransformer: "
                    + place.getReference() + " still computes with long after the rewrite, so every lookup would"
                    + " allocate BigInts. Update the transformer for this libGDX version; the method listing is:\n"
                    + listing(place.getProgram()));
            }
        }
    }

    private static MethodHolder findPlace(ClassHolder cls) {
        for(MethodHolder method : cls.getMethods()) {
            if(method.getName().equals("place") && method.parameterCount() == 1
                && method.getResultType().equals(ValueType.INTEGER) && method.getProgram() != null) {
                return method;
            }
        }
        return null;
    }

    /** Whether the program has a long constant, a long operation or a conversion from or to long. */
    private static boolean usesLongArithmetic(Program program) {
        for(BasicBlock block : program.getBasicBlocks()) {
            for(Instruction instruction : block) {
                if(instruction instanceof LongConstantInstruction
                    || instruction instanceof BinaryInstruction
                        && ((BinaryInstruction)instruction).getOperandType() == NumericOperandType.LONG
                    || instruction instanceof CastNumberInstruction
                        && (((CastNumberInstruction)instruction).getSourceType() == NumericOperandType.LONG
                            || ((CastNumberInstruction)instruction).getTargetType() == NumericOperandType.LONG)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean usesMultiplier(Program program) {
        for(BasicBlock block : program.getBasicBlocks()) {
            if(usesMultiplier(block)) {
                return true;
            }
        }
        return false;
    }

    private static boolean usesMultiplier(BasicBlock block) {
        for(Instruction instruction : block) {
            if(isMultiplier(instruction)) {
                return true;
            }
        }
        return false;
    }

    private static boolean callsFibonacciHash(Program program) {
        for(BasicBlock block : program.getBasicBlocks()) {
            for(Instruction instruction : block) {
                if(instruction instanceof InvokeInstruction
                    && ((InvokeInstruction)instruction).getMethod().getClassName().equals(FibonacciHash.class.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isMultiplier(Instruction instruction) {
        return instruction instanceof LongConstantInstruction
            && ((LongConstantInstruction)instruction).getConstant() == FibonacciHash.MULTIPLIER;
    }

    /**
     * Rewrites {@code place()} in place, or returns why the method is not the expected shape.
     * The expected body block (TeaVM's parser precedes it with an empty block that jumps to it, and may
     * add {@code nop}s) is, in order, with {@code key} the parameter and {@code this} the instance:
     * <pre>
     *   [hash = invoke(key)]               only for object keys: key.hashCode() or System.identityHashCode(key)
     *   x = (long)hash                     int keys: hash is the key itself
     *   -- or, for long keys --
     *   c32 = 32; k2 = key >>> c32; x = key ^ k2
     *   c = 0x9E3779B97F4A7C15L
     *   p = x * c
     *   s = this.shift
     *   q = p >>> s
     *   r = (int)q
     *   return r
     * </pre>
     * Only the lines from {@code x} to {@code r} change: they become {@code r = FibonacciHash.place(hash, s)}
     * or {@code r = FibonacciHash.placeLong(key, s)}.
     */
    private static String rewrite(MethodHolder method, Program program) {
        if(!method.getName().equals("place") || method.parameterCount() != 1
            || !method.getResultType().equals(ValueType.INTEGER)) {
            return "not an int place(key) method";
        }
        // TeaVM's parser puts the body in one block, after an entry block that only jumps to it.
        BasicBlock block = null;
        for(BasicBlock candidate : program.getBasicBlocks()) {
            if(!candidate.getTryCatchBlocks().isEmpty() || !candidate.getPhis().isEmpty()) {
                return "unexpected try/catch or phi";
            }
            if(usesMultiplier(candidate)) {
                if(block != null) {
                    return "the multiplier appears in two blocks";
                }
                block = candidate;
            }
            else if(candidate.instructionCount() != 1 || !(candidate.getFirstInstruction() instanceof JumpInstruction)) {
                return "unexpected block $" + candidate.getIndex();
            }
        }
        if(block == null) {
            return "the multiplier disappeared";
        }
        Variable self = program.variableAt(0);
        Variable key = program.variableAt(1);

        LongConstantInstruction multiplier = null;
        BinaryInstruction multiply = null;
        GetFieldInstruction shift = null;
        BinaryInstruction unsignedShift = null;
        CastNumberInstruction toInt = null;
        ExitInstruction exit = null;
        Set<Instruction> nops = new HashSet<>();
        for(Instruction instruction : block) {
            if(isMultiplier(instruction)) {
                if(multiplier != null) {
                    return "the multiplier appears twice";
                }
                multiplier = (LongConstantInstruction)instruction;
            }
            else if(isBinary(instruction, BinaryOperation.MULTIPLY)) {
                multiply = (BinaryInstruction)instruction;
            }
            else if(instruction instanceof GetFieldInstruction) {
                shift = (GetFieldInstruction)instruction;
            }
            else if(isBinary(instruction, BinaryOperation.SHIFT_RIGHT_UNSIGNED) && multiply != null) {
                unsignedShift = (BinaryInstruction)instruction;
            }
            else if(instruction instanceof CastNumberInstruction
                && ((CastNumberInstruction)instruction).getSourceType() == NumericOperandType.LONG) {
                toInt = (CastNumberInstruction)instruction;
            }
            else if(instruction instanceof ExitInstruction) {
                exit = (ExitInstruction)instruction;
            }
            else if(instruction instanceof EmptyInstruction) {
                nops.add(instruction);
            }
        }
        if(multiplier == null || multiply == null || shift == null || unsignedShift == null || toInt == null
            || exit == null) {
            return "missing an instruction of the multiply, shift, cast, return chain";
        }
        // The chain must connect: the product of x and the multiplier, shifted by this.shift, cast to int, returned.
        Variable x = otherOperand(multiply, multiplier.getReceiver());
        if(x == null) {
            return "the multiplier is not an operand of the multiplication";
        }
        if(!shift.getField().getFieldName().equals(SHIFT_FIELD) || !ValueType.INTEGER.equals(shift.getFieldType())
            || shift.getInstance() != self) {
            return "expected this.shift (int) as the shift count";
        }
        if(unsignedShift.getFirstOperand() != multiply.getReceiver()
            || unsignedShift.getSecondOperand() != shift.getReceiver()) {
            return "the unsigned shift is not product >>> this.shift";
        }
        if(toInt.getTargetType() != NumericOperandType.INT || toInt.getValue() != unsignedShift.getReceiver()) {
            return "the int cast is not of the shifted product";
        }
        if(exit.getValueToReturn() != toInt.getReceiver()) {
            return "the method does not return the cast";
        }

        // Everything in the block must be the chain, the hash producer, or the shift read.
        Set<Instruction> chain = new HashSet<>(Set.of(multiplier, multiply, unsignedShift, toInt));
        Set<Instruction> kept = new HashSet<>(Set.of(shift, exit));
        kept.addAll(nops);
        InvokeInstruction call = new InvokeInstruction();
        call.setType(InvocationType.SPECIAL);
        call.setReceiver(toInt.getReceiver());
        call.setLocation(toInt.getLocation());
        if(method.parameterType(0).equals(ValueType.LONG)) {
            // x = key ^ (key >>> 32)
            Instruction xor = producer(block, x);
            if(!isBinary(xor, BinaryOperation.XOR) || ((BinaryInstruction)xor).getFirstOperand() != key) {
                return "a long key must be folded as key ^ key >>> 32";
            }
            Instruction fold = producer(block, ((BinaryInstruction)xor).getSecondOperand());
            if(!isBinary(fold, BinaryOperation.SHIFT_RIGHT_UNSIGNED) || ((BinaryInstruction)fold).getFirstOperand() != key) {
                return "a long key must be folded as key ^ key >>> 32";
            }
            Instruction thirtyTwo = producer(block, ((BinaryInstruction)fold).getSecondOperand());
            if(!(thirtyTwo instanceof IntegerConstantInstruction)
                || ((IntegerConstantInstruction)thirtyTwo).getConstant() != 32) {
                return "a long key must be folded as key ^ key >>> 32";
            }
            chain.addAll(Set.of(xor, fold, thirtyTwo));
            call.setMethod(PLACE_LONG);
            call.setArguments(key, shift.getReceiver());
        }
        else {
            // x = (long)hash, with hash the int key or the result of one call on the object key
            Instruction toLong = producer(block, x);
            if(!(toLong instanceof CastNumberInstruction)
                || ((CastNumberInstruction)toLong).getSourceType() != NumericOperandType.INT
                || ((CastNumberInstruction)toLong).getTargetType() != NumericOperandType.LONG) {
                return "the multiplicand is not (long)hash";
            }
            chain.add(toLong);
            Variable hash = ((CastNumberInstruction)toLong).getValue();
            if(hash != key) {
                Instruction hashing = producer(block, hash);
                if(!(hashing instanceof InvokeInstruction) || !hashesKey((InvokeInstruction)hashing, key)) {
                    return "the hash is neither the key nor one call on the key";
                }
                kept.add(hashing);
            }
            call.setMethod(PLACE);
            call.setArguments(hash, shift.getReceiver());
        }
        for(Instruction instruction : block) {
            if(!chain.contains(instruction) && !kept.contains(instruction)) {
                return "unexpected instruction " + instruction.getClass().getSimpleName();
            }
        }

        exit.insertPrevious(call);
        for(Instruction instruction : chain) {
            instruction.delete();
        }
        return null;
    }

    private static boolean isBinary(Instruction instruction, BinaryOperation operation) {
        return instruction instanceof BinaryInstruction
            && ((BinaryInstruction)instruction).getOperation() == operation
            && ((BinaryInstruction)instruction).getOperandType() == NumericOperandType.LONG;
    }

    private static Variable otherOperand(BinaryInstruction binary, Variable operand) {
        if(binary.getFirstOperand() == operand) {
            return binary.getSecondOperand();
        }
        if(binary.getSecondOperand() == operand) {
            return binary.getFirstOperand();
        }
        return null;
    }

    private static boolean hashesKey(InvokeInstruction call, Variable key) {
        if(call.getReceiver() == null) {
            return false;
        }
        if(call.getInstance() == key) {
            return call.getArguments().isEmpty();
        }
        return call.getInstance() == null && call.getArguments().size() == 1 && call.getArguments().get(0) == key;
    }

    /** The instruction of the block that defines the variable, or null. */
    private static Instruction producer(BasicBlock block, Variable variable) {
        if(variable == null) {
            return null;
        }
        for(Instruction instruction : block) {
            if(definedVariable(instruction) == variable) {
                return instruction;
            }
        }
        return null;
    }

    private static Variable definedVariable(Instruction instruction) {
        if(instruction instanceof BinaryInstruction) {
            return ((BinaryInstruction)instruction).getReceiver();
        }
        if(instruction instanceof CastNumberInstruction) {
            return ((CastNumberInstruction)instruction).getReceiver();
        }
        if(instruction instanceof InvokeInstruction) {
            return ((InvokeInstruction)instruction).getReceiver();
        }
        if(instruction instanceof IntegerConstantInstruction) {
            return ((IntegerConstantInstruction)instruction).getReceiver();
        }
        return null;
    }

    private static String listing(Program program) {
        try {
            return new ListingBuilder().buildListing(program, "  ");
        } catch(RuntimeException e) {
            return "(listing unavailable: " + e + ")";
        }
    }
}
