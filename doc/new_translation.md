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

Dynamic strings are represented Java-side by FF0 through FF4, which DynTrSlot can be cast to. All dynamic translation slots go through MVMEnvR48.dynTrBase, and the default string is given then. dynTrDump is an attempt to approximately reverse-engineer all translation strings.

It may end up being necessary to use a dedicated storage system for dynamic translation strings that holds source Datum code for generating the dump file, but this wastes the `define-group` efforts...

OTHER STUFF:This isn't set in stone, but here's the basic ideas:

* Name routines are connected into the MVM context "somehow" in a way that allows for direct definition from MVM.
  
  * Presumably, referring to a name routine ensures the slot for consistency reasons.
  
  * Presumably, FormatSyntax's name routine database simply becomes a cache of "connector" objects implementing find-or-fallback.
  
  * All this done, name routines can start being migrated effectively the moment the necessary DM primitives exist.
    
    * So much R2K code would be cleaned up.

* Other strings are complicated.
  
  * They're represented by FF0 through FF4, which DynTrSlot can be cast to. When setting up a dynamic translation slot, the default behaviour is given.
