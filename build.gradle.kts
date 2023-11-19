plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.dokka)
}


tasks.register<Copy>("setUpGitHooks") {
    group = "help"
    from("$rootDir/.hooks")
    into("$rootDir/.git/hooks")
}
