# R48 Translation System, Second Edition

R48 translation has always been divided into two groups, *static* and *dynamic*.

*Static* translation is translation of components of R48 itself.

*Dynamic* translation is translation of schemas and so forth.

## Static

Static translation used to be handled via a gettext-style `TXDB.get()`, but it turns out that gettext-style handling of format strings, even R48's enhanced format strings, is awful.

Now, static translation is handled as a set of "pages" (see `TrPage`) which are initialized on program start or language change in the Launcher.

These pages can contain the following kinds of value in fields:

* Other `TrPage` instances to load (from a translator's perspective there is no hierarchial namespacing here as it wouldn't make sense).

* Strings

* 1, 2, 3, or 4-parameter MVM functions returning String

These values are grabbed from Launcher REPL context via globals such as: `TrExample.someValue`.

There are utility macros defined, `fl1` through `fl4`, and `define-group`, allowing for code such as:

```
(define-group TrExample
    marble "Marble"
    version (fl1 "Version " a0 " of Marble")
)
```

However, any Launcher REPL functionality may be used.

*The access available from static translation is intentionally limited to not include App. Where existing code used FormatSyntax in ways that required App, the FormatSyntax value interpretation system has been used. Therefore, static translation is not capable of anything the Launcher REPL can't do. In particular the static translator cannot directly read or write objects. Values are copied to fields after VM startup. This has several effects, but the main one is that if a translation field is supposed to be an immutable string, it stays that way.*

Access to statically translated fields from Java is best achieved through the `T` semiglobal, or `app.t`, or `ilg.t`. All of these are `TrRoot`.

## Dynamic

Abstract problems that this needed to/needs to solve:

* All dynamic translation slots need to be savable to a file for user editing. This mechanism should be either lossless or effectively lossless.
  
  * Essentially, if you want to translate a given "profile" of R48, you copy the English static translation files, and save out the launcher and schema dynamic translation files, and start editing.

* Dynamic translations need to work without App present for the launcher dynamics.

* Dynamic translations should still be reasonably quick to write.

* Dynamic translations are supposed to ultimately replace FormatSyntax.

This leads me to believe the following is required:

* Dynamic translations are arbitrary Datum data. *Not MVM. Datum.*
  
  * To actually implement this, dynamic translation *values* live inside DynTrSlot, not MVMSlot. The DynTrSlot objects will live in the MVM namespace where the DynTrSlot values used to live.
    
    * This solution also cleans up the "how does MVMSDB get a DynTrSlot if the MVMSlot just points to the value" problem by not doing that nonsense.

* Dynamic translations are compiled into MVM by *MVM-side code* that differs between Launcher and App.
  
  * The MVM-side code can add whatever particular syntax it wants, and it has no effect on the general MVM namespace or R48 Java code.
  
  * It may be of use to be able to specify the *kind* of compilation desired, but this task could also just be put into "stuff MVM has to worry about".
  
  * This compilation occurs within the fold of SchemaParseTest. This means any compilation errors become test errors, which is good.
  
  * The in-MVM compiler can implement all the usual "fuzzy" safety expected from FormatSyntax.

Dynamic strings are represented Java-side by FF0 through FF4, which DynTrSlot can be cast to. All dynamic translation slots go through MVMEnvR48.dynTrBase, and the default is given then. Ideally, some way to make the FormatSyntax parameters stuff a *compile-time* problem would be good, because this prepares things for when FormatSyntax goes away.

## TRC DSL Overview

*The TRC DSL is meant to complement use of Scheme in R48 translations, reducing effort for simple cases while still allowing access to more powerful tools when necessary.*

The cost of this is that TRC only really *works* for IRIOs, but the advantage is that this makes it much easier to port the rather large amount of IRIO-centric translated name routines.

TRC looks like: `"Name: " (@ @name)`

Or if written inline in SDB: `"\"Name: \" (@ @name)"`

### When Is Something TRC?

TRC is used when all of these apply:

* Ancient legacy code that should be gone by v1.5's release isn't involved.

* The translation involves parameters.

* The translation is setup during/after schema load.
  
  * Dynamic translations that involve parameters can be attempted in the launcher, but will always error as TRC is not loaded.
    
    * TRC can't be loaded because of missing primitives.

* The translation is expected to be passed IRIOs, with one "primary" IRIO.

In terms of when TRC appears in code: `define-tr` isn't TRC (accepts arbitrary Scheme values), while `define-name` is TRC.

### How To Use It

`vm/trc.scm` implements the basic syntax of TRC.

It helps to keep in mind two Scheme-level utilties in R48:

* The `..` macro, which stringifies and concatenates the results of all statements within.

* The `flX`  (where X is some number) macro series, which defines a lambda where the body is wrapped in `..`, and defines arguments in the pattern `a0`, `a1` etc.

TRC is essentially a DSL within Scheme, where a list of TRC operations is translated to an equivalent Scheme lambda.

Dynamic translations have a *focus.* The focus exists so that multiple arguments can be passed without requiring individual addressing for each operation. The default focus is always `a0`, the first argument. `with` starts a block with a different focus, and any other operation tends to read the focus.

`vm` simply escalates to Scheme. The inverse is possible; dynamic translation entries can be called from Scheme as regular functions.

Finally, there's the rest of the actual operations themselves. See `vm/trc-lib.scm` for these.

You can test TRC expressions in the REPL using `(tr-dyn-fmt OBJ CODE)`.

Some stuff pulled from `IRB_CMDB.txt`:

```

 Datum Name Format

  This has yet to be fully finalized, but the basic idea is a clean nameroutine but not carrying the magical schema detection.
  vm/trc-lib.scm is where new commands for this are defined.

  Essentially, the command name is represented in S-expression format as a list of elements to be formatted together.
  Of the functions that exist (defined in vm/trc-lib.scm):

   + (vm CODE...)
     Injects D/MVM code into the result directly. Almost certainly unnecessary as TRC can be extended.
   + (with FOCUS CODE...)
     Changes the "focus", i.e. what PATHs are relative to, for the given CODE...
   + (@ PATH [INTERP [PREFIX-ENUMS]])
     If the PATH exists, formats (possibly modified by INTERP and PREFIX-ENUMS).
     Otherwise will always do nothing.
   + ($ PFX PATH [INTERP [PREFIX-ENUMS]]) :
     If the PATH exists, formats (possibly modified by INTERP and PREFIX-ENUMS), prepending PFX.
     Otherwise will always do nothing.
   + (? PATH TRUE [FALSE])
     If the PATH exists, see TRUE, otherwise FALSE.
   + (= PATH DEF (VAL RES)...)
     Compares various values against PATH, picking the first that matches or DEF on failure. (A missing value is considered null.)
```

