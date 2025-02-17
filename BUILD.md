# R48 Build Instructions

## Step 1: Repositories

You need to have the following repositories checked out in neighbouring directories:

* gabien-common
* gabien-app-r48

Datum gets formal binary releases which will be downloaded and used by default. It is of course possible to download the `datum` repository and install it with Maven or micromvn; be sure to use OpenJDK 8 for this also.

## Step 2: GaBIEn Setup

Please see <https://github.com/20kdc/gabien-common/blob/master/BUILD.md> for initial instructions.

By the end of that guide, you should have, _at minimum,_ established that the `gabien-ready` script runs properly.

## Step 3: IDE Setup

This is pretty standard Maven project IDE setup, though with a slight catch.

Essentially, you need to create a run configuration to run the R48 project with `gabien-javase` in the classpath, running the `gabien.Main` class.

Doing things via IDE also means that with some run configuration adjustment, you skip having to fuse the `gabien-javase` project into the application JAR.

If you don't want to use an IDE, then you'll either need to use the dev release script or just go do your own thing.

## Specific Notes For Releases & Android

Releases & Android builds are taped together with a lot of shell script.

Some people may ask "why aren't you using Gradle?".

Due to their no-compatibility policy, Gradle is not a reliable piece of software. It cannot be used in a project that needs to be picked up to deal with an issue on short notice with years between contact.

The above steps get you most of the way there. `cd releaser ; ./releaser-dev.sh EXAMPLEID` (supply some name for the release) will get you an Android build and attempt to auto-install it.

## Some Specific Kinds Of Error

### Android D8: `Cannot invoke "String.length()" because "<parameter1>" is null`

```
Error in staging/gabien/TextboxImplObject$4.class:
java.lang.NullPointerException: Cannot invoke "String.length()" because "<parameter1>" is null
Compilation failed with an internal error.
Exception in thread "main" java.lang.RuntimeException: com.android.tools.r8.CompilationFailedException: Compilation failed to complete, origin: staging/gabien/TextboxImplObject$4.class
	at com.android.tools.r8.utils.R0.a(R8_8.2.2-dev_53a55043254cc5be8ef500331bba25d1b4ca4bc2cd66c555d4358bf672a1f10a:126)
	at com.android.tools.r8.D8.main(R8_8.2.2-dev_53a55043254cc5be8ef500331bba25d1b4ca4bc2cd66c555d4358bf672a1f10a:5)
```

Ok, so, an explanation of this: OpenJDK 21 _apparently_ compiles files without method parameter names. This triggers a bug in D8, and while the fix may have made its way to the Android Gradle Plugin, it hasn't made its way into build-tools releases yet.

**This sort of thing is why I'm insisting that OpenJDK 8 be used for the main build.**
