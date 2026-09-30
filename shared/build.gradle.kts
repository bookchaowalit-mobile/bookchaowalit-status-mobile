plugins { kotlin("multiplatform") }

kotlin {
    // JVM target: runs the commonTest suite on any machine and is consumable
    // by an Android app module (androidJvm resolves the jvm variant).
    jvm()
    // Apple targets compile only on macOS hosts; Gradle skips them elsewhere.
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "shared"; isStatic = true }
    }
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
