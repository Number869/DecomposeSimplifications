# Decompose Simplifications
[![](https://jitpack.io/v/Number869/DecomposeSimplifications.svg)](https://jitpack.io/#Number869/DecomposeSimplifications)
# What is this?

This is a set of things that i made for myself that simplify the use of the Decompose library in Compose (Jetpack and Multiplatform). Thought maybe someone else could find it useful, too. 

# How to use this?

Add ```maven { url = uri("https://jitpack.io") }``` to ```dependencyResolutionManagement { repositories { ... } }``` in settings.gradle.kts, then just just add the necessary artifacts. If you use libs.versions.toml, add ```decompose-simplifications = { group = "com.github.Number869.decomposeSimplifications", name = "decompose-simplifications-core or any other module", version = "library version specified at the top of this readme" }``` Check the sample app to see examples.

# Suggestions

Suggestions are welcome. To make one - head over to the issues tab and describe your suggestion. The description must include:
- Bried feature description
- Reasons for the feature to be added
- An example of the hypothetical feature inside the code (with context)
- (Optional) How do you see it being implemented inside the library
Features that *"solve potential problems i might encounter in the future*" are not taken into consideration. 

# PRs

If you have improved the library in any way and want your changes to be included - make the PR into the ```dev```
branch. The PR must include the description of improvements.
