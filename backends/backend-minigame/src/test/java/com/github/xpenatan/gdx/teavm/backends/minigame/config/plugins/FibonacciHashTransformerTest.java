package com.github.xpenatan.gdx.teavm.backends.minigame.config.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.xpenatan.gdx.teavm.backends.minigame.utils.FibonacciHash;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.teavm.cache.IncrementalDependencyRegistration;
import org.teavm.diagnostics.Diagnostics;
import org.teavm.model.BasicBlock;
import org.teavm.model.CallLocation;
import org.teavm.model.ClassHierarchy;
import org.teavm.model.ClassHolder;
import org.teavm.model.ClassHolderSource;
import org.teavm.model.ClassHolderTransformerContext;
import org.teavm.model.Instruction;
import org.teavm.model.MethodHolder;
import org.teavm.model.PreOptimizingClassHolderSource;
import org.teavm.model.Program;
import org.teavm.model.ReferenceCache;
import org.teavm.model.ValueType;
import org.teavm.model.instructions.BinaryInstruction;
import org.teavm.model.instructions.CastNumberInstruction;
import org.teavm.model.instructions.IntegerConstantInstruction;
import org.teavm.model.instructions.InvokeInstruction;
import org.teavm.model.instructions.LongConstantInstruction;
import org.teavm.model.instructions.NumericOperandType;
import org.teavm.parsing.ClasspathClassHolderSource;

/**
 * Runs the transformer on the real libGDX classes, parsed by TeaVM from the test classpath, and on
 * damaged copies of them, without a full compilation. TeaVMBuilder hands transformers pre-optimized
 * programs (copies propagated), so the test reads through the same PreOptimizingClassHolderSource.
 */
public class FibonacciHashTransformerTest {
    private final ClassHolderSource source =
        new PreOptimizingClassHolderSource(new ClasspathClassHolderSource(new ReferenceCache()));
    private final List<String> errors = new ArrayList<>();
    private final ClassHolderTransformerContext context = new ClassHolderTransformerContext() {
        private final ClassHierarchy hierarchy = new ClassHierarchy(source);

        @Override
        public ClassHierarchy getHierarchy() {
            return hierarchy;
        }

        @Override
        public Diagnostics getDiagnostics() {
            return new Diagnostics() {
                @Override
                public void error(CallLocation location, String error, Object... params) {
                    errors.add(error);
                }

                @Override
                public void warning(CallLocation location, String error, Object... params) {
                }
            };
        }

        @Override
        public IncrementalDependencyRegistration getIncrementalCache() {
            return null;
        }

        @Override
        public boolean isObfuscated() {
            return false;
        }

        @Override
        public boolean isStrict() {
            return false;
        }

        @Override
        public String getEntryPoint() {
            return null;
        }

        @Override
        public void submit(ClassHolder cls) {
        }
    };

    @Test
    public void rewritesEveryCollectionToAnIntCall() {
        for(String name : FibonacciHashTransformer.PLACE_CLASSES) {
            ClassHolder cls = source.get(name);
            if(cls == null) {
                continue; // LongSet is not in the libGDX this backend compiles against
            }
            MethodHolder place = place(cls);
            assertTrue(name + " before", usesLong(place.getProgram()));
            new FibonacciHashTransformer().transformClass(cls, context);
            assertEquals(name, List.of(), errors);
            assertFalse(name + " after", usesLong(place.getProgram()));
            String helper = name.endsWith(".LongMap") || name.endsWith(".LongSet") ? "placeLong" : "place";
            assertTrue(name + " calls FibonacciHash." + helper, callsHelper(place.getProgram(), helper));
            // The hash producer survived: an object key is still hashed by a call, an int key is used directly.
            assertEquals(name, !name.contains(".Int") && !name.contains(".Long"), invokes(place.getProgram()) == 2);
        }
        assertNotNull(source.get("com.badlogic.gdx.utils.ObjectMap"));
    }

    @Test
    public void rejectsAPlaceWithAnUnexpectedInstruction() {
        ClassHolder cls = source.get("com.badlogic.gdx.utils.ObjectIntMap");
        MethodHolder place = place(cls);
        Program program = place.getProgram();
        IntegerConstantInstruction extra = new IntegerConstantInstruction();
        extra.setConstant(7);
        extra.setReceiver(program.createVariable());
        program.basicBlockAt(program.basicBlockCount() - 1).getLastInstruction().insertPrevious(extra);

        new FibonacciHashTransformer().transformClass(cls, context);

        // The shape error, and the class guard because place() was left as it was.
        assertEquals(errors.toString(), 2, errors.size());
        assertTrue(errors.get(0), errors.get(0).contains("not the known place() shape (unexpected instruction IntegerConstantInstruction)"));
        assertTrue(errors.get(0), errors.get(0).contains("ObjectIntMap.place"));
        assertTrue(errors.get(1), errors.get(1).contains("ObjectIntMap.place(Ljava/lang/Object;)I still computes with long"));
        assertTrue(usesLong(program));
    }

    @Test
    public void rejectsAPlaceThatHashesWithAnotherLongConstant() {
        ClassHolder cls = source.get("com.badlogic.gdx.utils.IntMap");
        Program program = place(cls).getProgram();
        for(BasicBlock block : program.getBasicBlocks()) {
            for(Instruction instruction : block) {
                if(instruction instanceof LongConstantInstruction) {
                    ((LongConstantInstruction)instruction).setConstant(0x9E3779B97F4A7C13L);
                }
            }
        }

        new FibonacciHashTransformer().transformClass(cls, context);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0), errors.get(0).contains("IntMap.place(I)I still computes with long"));
    }

    @Test
    public void leavesGameCodeAlone() {
        // FibonacciHashTest's reference expressions use the multiplier outside com.badlogic.gdx.
        ClassHolder cls = source.get("com.github.xpenatan.gdx.teavm.backends.minigame.utils.FibonacciHashTest");
        new FibonacciHashTransformer().transformClass(cls, context);
        assertEquals(List.of(), errors);
        boolean multiplied = false;
        for(MethodHolder method : cls.getMethods()) {
            multiplied |= method.getProgram() != null && usesLong(method.getProgram());
        }
        assertTrue(multiplied);
    }

    private static MethodHolder place(ClassHolder cls) {
        for(MethodHolder method : cls.getMethods()) {
            if(method.getName().equals("place") && method.parameterCount() == 1
                && method.getResultType().equals(ValueType.INTEGER)) {
                return method;
            }
        }
        throw new AssertionError(cls.getName() + " has no place()");
    }

    private static boolean usesLong(Program program) {
        for(BasicBlock block : program.getBasicBlocks()) {
            for(Instruction instruction : block) {
                if(instruction instanceof LongConstantInstruction
                    || instruction instanceof BinaryInstruction
                        && ((BinaryInstruction)instruction).getOperandType() == NumericOperandType.LONG
                    || instruction instanceof CastNumberInstruction
                        && ((CastNumberInstruction)instruction).getSourceType() == NumericOperandType.LONG) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean callsHelper(Program program, String helper) {
        for(BasicBlock block : program.getBasicBlocks()) {
            for(Instruction instruction : block) {
                if(instruction instanceof InvokeInstruction) {
                    InvokeInstruction call = (InvokeInstruction)instruction;
                    if(call.getMethod().getClassName().equals(FibonacciHash.class.getName())
                        && call.getMethod().getName().equals(helper)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int invokes(Program program) {
        int count = 0;
        for(BasicBlock block : program.getBasicBlocks()) {
            for(Instruction instruction : block) {
                if(instruction instanceof InvokeInstruction) {
                    count++;
                }
            }
        }
        return count;
    }
}
