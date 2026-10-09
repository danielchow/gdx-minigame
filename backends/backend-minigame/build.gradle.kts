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

    testImplementation("junit:junit:4.13.2")
    // FreeTypeGlyphProbe: the mini game FreeType extension, compiled against libGDX's FreeType API.
    testImplementation(project(":extensions:gdx-freetype-teavm"))
    testImplementation("com.badlogicgames.gdx:gdx-freetype:${LibExt.gdxVersion}")
}

val regressionProbes = layout.buildDirectory.dir("regression-probes")

// Files a previous build of the pixmap-buffer probe could have left behind. The probe
// build must delete all of them except the DevTools private config; BuildOutputTest checks.
val staleProbeOutput = mapOf(
    "game.json" to "{\"stale\": true}",
    "project.private.config.json" to "{\"planted\": true}",
    "app.js.map" to "stale",
    "app.js.teavmdbg" to "stale",
    "src/com/example/Stale.java" to "stale",
    "assets/removed-asset.png" to "stale",
    "assets/rename-map.json" to "{\"/old.bin\": \"/old.bin.txt\"}",
)

// src/test also holds TeaVM probe programs; testRuntime executes those, while JUnit
// tests inspect the probe build output.
tasks.test {
    failOnNoDiscoveredTests = false
    dependsOn("buildRegressionProbes")
    systemProperty("regressionProbes", regressionProbes.get().asFile.absolutePath)
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
    doFirst {
        val previousBuild = regressionProbes.get().dir("pixmap-buffer/minigame").asFile
        staleProbeOutput.forEach { (path, content) ->
            previousBuild.resolve(path).apply { parentFile.mkdirs() }.writeText(content)
        }
    }
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
