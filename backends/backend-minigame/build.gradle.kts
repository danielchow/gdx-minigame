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
