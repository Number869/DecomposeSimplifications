plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.binary.compatibility.validator)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm("desktop")

//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64(),
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "lib"
//            isStatic = true
//        }
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(project(":core"))
                implementation(libs.decompose)
                implementation(libs.decompose.extensions)
            }
        }
        val androidMain by getting {
            dependencies {
            }
        }
//        val iosX64Main by getting
//        val iosArm64Main by getting
//        val iosSimulatorArm64Main by getting
//        val iosMain by creating {
//            dependsOn(commonMain)
//            iosX64Main.dependsOn(this)
//            iosArm64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
//        }
        val desktopMain by getting {
            dependencies {
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.myapplication.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

apiValidation {
    ignoredPackages.add("com.arkivanov.decompose")
}

mavenPublishing {
//    publishToMavenCentral(SonatypeHost.DEFAULT)
    // or when publishing to https://s01.oss.sonatype.org
//    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
//    signAllPublications()
    coordinates("com.number869", "decompose-simplifications-optionalExtensions", "0.0.1")

    pom {
        name.set(project.name)

        url.set("https://github.com/Number869/DecomposeSimplifications")
        licenses {
            license {
                name.set("GPLv3")
                url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                distribution.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
            }
        }
        developers {
            developer {
                id.set("Number869")
                name.set("Number 869")
                url.set("https://github.com/Number869/")
            }
        }
        scm {
            url.set("https://github.com/Number869/DecomposeSimplifications")
            connection.set("scm:git:git://github.com/Number869/DecomposeSimplifications.git")
            developerConnection.set("scm:git:ssh://git@github.com/Number869/DecomposeSimplifications.git")
        }
    }
}
