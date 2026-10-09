val moduleName = "gdx-freetype-teavm"

dependencies {
    implementation(project(":backends:backend-web"))
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