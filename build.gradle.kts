import java.util.*

plugins {
    `java-library`
    id("minestom.publishing-conventions")
    id("minestom.native-conventions")
    alias(libs.plugins.blossom)
    signing
}

var baseVersion by extra("1.4.0")
var snapshot by extra("")

group = "net.onelitefeather.microtus"


version = "%s%s".format(Locale.ROOT, baseVersion, snapshot)

allprojects {
    group = "net.onelitefeather.microtus"
    version = "%s%s".format(Locale.ROOT, baseVersion, snapshot)
    description = "Lightweight and multi-threaded Minecraft server implementation"
}

sourceSets {
    main {
        java {
            srcDir(file("src/autogenerated/java"))
        }
        blossom {
            javaSources {
                val gitCommit = System.getenv("GIT_COMMIT")
                val gitBranch = System.getenv("GIT_BRANCH")
                val group = System.getenv("GROUP")
                val artifact = System.getenv("ARTIFACT")
                property("\"&COMMIT\"", if (gitCommit == null) "null" else "\"${gitCommit}\"")
                property("\"&BRANCH\"", if (gitBranch == null) "null" else "\"${gitBranch}\"")
                property("\"&GROUP\"", if (group == null) "null" else "\"${group}\"")
                property("\"&ARTIFACT\"", if (artifact == null) "null" else "\"${artifact}\"")
            }
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {
    jar {
        manifest {
            attributes("Automatic-Module-Name" to "net.minestom.server")
        }
    }
    withType<Javadoc> {
        (options as? StandardJavadocDocletOptions)?.apply {
            encoding = "UTF-8"

            // Custom options
            addBooleanOption("html5", true)
            addStringOption("-release", "21")
            // Links to external javadocs
            links("https://docs.oracle.com/en/java/javase/21/docs/api/")
            links("https://jd.advntr.dev/api/${libs.versions.adventure.get()}/")
        }
    }
    withType<Zip> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    withType<Test> {
        useJUnitPlatform()

        // Viewable packets make tracking harder. Could be re-enabled later.
        jvmArgs("-Dminestom.viewable-packet=false")
        jvmArgs("-Dminestom.inside-test=true")
        minHeapSize = "512m"
        maxHeapSize = "1024m"
    }


}

dependencies {
    // Testing Framework
    testImplementation(project(mapOf("path" to ":testing")))
    // Only here to ensure J9 module support for extensions and our classloaders
    testCompileOnly(libs.mockito.core)


    // Logging
    implementation(libs.bundles.logging)
    // Libraries required for the terminal
    implementation(libs.bundles.terminal)

    // Performance improving libraries
    implementation(libs.caffeine)
    api(libs.fastutil)
    implementation(libs.bundles.flare)

    // Libraries
    api(libs.gson)
    implementation(libs.jcTools)
    // Path finding
    api(libs.hydrazine)

    // Adventure, for user-interface
    api(libs.bundles.adventure)

    // Kotlin Libraries
    api(libs.bundles.kotlin)

    api(libs.maven.resolver)
    api(libs.maven.connector)
    api(libs.maven.transport.http)

    // Minestom Data (From MinestomDataGenerator)
    implementation(libs.minestomData)

    // NBT parsing/manipulation/saving
    api("io.github.jglrxavpok.hephaistos:common:${libs.versions.hephaistos.get()}")
    api("io.github.jglrxavpok.hephaistos:gson:${libs.versions.hephaistos.get()}")

    // BStats
    api(libs.bstats.base)
}

