# R48 Build Instructions

## Step 1: Prerequisites

You are expected to have:

* Apache Maven
* A Java JDK that supports source/target 1.8

## Step 2: Repositories

You need to have the following repositories checked out in neighbouring directories:

* datum
* gabien-common
* gabien-app-r48

## Step 3: `gabien-common` setup

In the gabien-common repository, there are "natives" releases. Grab a `natives-sdk.zip` file, extract it somewhere, and run `sdk-install` (or `sdk-install.cmd` for Windows).

Then run `mvn install` for `gabien-common`. That should also test things.

## Step 4: IDE Setup

This is pretty standard Maven project IDE setup.

Doing things via IDE also means that with some run configuration adjustment, you skip having to fuse the `gabien-javase` project into the application JAR.

If you don't want to use an IDE, then you'll either need to use the dev release script or just go do your own thing.

## Specific Notes For Releases & Android

Releases & Android builds are taped together with a lot of shell script.

Some people may ask "why aren't you using Gradle?".

Due to their no-compatibility policy, Gradle is not a reliable piece of software. It cannot be used in a project that needs to be picked up to deal with an issue on short notice with years between contact.

The above steps get you most of the way there. `releaser/releaser-dev.sh EXAMPLEID` (supply some name for the release) will get you an Android build and attempt to auto-install it.

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

Solution: Stop using OpenJDK 21. It appears to be broken (<https://github.com/slint-ui/slint/issues/4973>, <https://github.com/flutter/flutter/issues/145789>). Use OpenJDK 17 instead.
