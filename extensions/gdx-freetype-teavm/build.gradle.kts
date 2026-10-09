val moduleName = "gdx-freetype-teavm"

dependencies {
    // Compile-time only: the emu pixmap classes this extension references are supplied by whichever backend
    // the game uses (backend-web or backend-minigame, same names). A runtime dependency would put
    // backend-web's emu classes on a mini game's classpath, where only classpath order picks the right ones.
    compileOnly(project(":backends:backend-web"))
    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
}

// Runs the shipped FreeType loader and WASM in Node (node must be on PATH).
tasks.register<Exec>("testRuntime") {
    description = "Run the src/test/js Node tests against the bundled FreeType WASM"
    group = "verification"
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