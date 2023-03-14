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
