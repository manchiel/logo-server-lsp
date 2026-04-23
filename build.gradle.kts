plugins {
    id("java")
    id("antlr")
    id("application")
}

group = "org.logo.lsp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    add("antlr", "org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.23.1")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.23.1")


    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-listener", "-package", "org.logo.lsp.parser")
    outputDirectory = layout.buildDirectory.dir("generated-src/antlr/main/org/logo/lsp/parser").get().asFile
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "org.logo.lsp.server.LogoLanguageServerLauncher"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-src/antlr/main"))
        }
    }
}
application {
    mainClass.set("org.logo.lsp.server.LogoLanguageServerLauncher")
}
tasks.test {
    useJUnitPlatform()
}