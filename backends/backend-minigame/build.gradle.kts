plugins {
    id("java-library")
}

val moduleName = "backend-minigame"

sourceSets["main"].java.setSrcDirs(mutableSetOf("emu", "gen", "src/main/java/"))

val compileJavaTask = tasks.getByPath("compileJava")!!
compileJavaTask.doFirst {
    sourceSets["main"].runtimeClasspath.forEach {
        println(it)
    }
}
compileJavaTask.dependsOn("clean")
compileJavaTask.mustRunAfter("clean")

dependencies {
    api(project(":backends:backend-shared"))

    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan:jMultiplatform:${LibExt.jMultiplatform}")
}

val regressionProbes = layout.buildDirectory.dir("regression-probes")

// src/test holds TeaVM probe programs, not JVM tests; testRuntime executes them.
tasks.test {
    failOnNoDiscoveredTests = false
}

tasks.register<JavaExec>("buildRegressionProbes") {
    description = "Compile the src/test regression probes into Mini Game packages"
    group = "verification"
    mainClass.set("com.github.xpenatan.gdx.teavm.backends.minigame.regression.RegressionProbeBuilder")
    // TeaVMResourceProperties only copies engine scripts out of JARs, so use the backend
    // JAR, as a consuming game does, instead of the main class and resource directories.
    classpath = files(tasks.jar) + sourceSets["test"].output +
        (sourceSets["test"].runtimeClasspath - sourceSets["main"].output)
    args(regressionProbes.get().asFile.absolutePath)
}

// Runs the bundled engine and the compiled probes in Node (node must be on PATH).
tasks.register<Exec>("testRuntime") {
    description = "Run the src/test/js Node regression tests against the real engine WASM"
    group = "verification"
    dependsOn("buildRegressionProbes")
    commandLine("node", "--test", "src/test/js/*.test.mjs")
}

tasks.named("check") {
    dependsOn("testRuntime")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = moduleName
            group = LibExt.groupId
            version = LibExt.libVersion
            from(components["java"])
        }
    }
}
