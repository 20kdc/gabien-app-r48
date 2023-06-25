# Old CMDB Stuff (or, how not to design a formatting language for internationalization)

This is *old stuff!* It was put here for fun reading as per the notice of deprecation in `doc/IRB_CMDB.txt` and is not current.

This describes some old formats in R48.

## Original Format

This dates back to the earliest versions of R48, and took a while to remove from the codebase.

Note the hardcoding of " to ", any other language would have to substitute these names for extended format-syntax.

```
 Original Format (no prefix)

  A very simple format for cases when you can get away with it.

  Note that '$' is replaced with " <nice looking string>",
   while '!' is replaced with " to <nice looking string>".
   '#' defines a range, for "A through B" commands.
  Both are replaced with nothing if just entering the command name.

  an example: 108: Comment$
  a more advanced example: 103: Input Number!$ digits
   (As this format is crude, this shows as "Input Number digits" in the selection box.)
```

## Built-In Name Routines

Some of these only really made sense when the `[][]` syntax was around anyway:

```

 ----------------------------------

  Name Database Built-in Routines
  These all follow the scheme "lang-<language>-<methodCamelCase>".
  Common is used for routines which aren't targetted at a specific language.

  lang-Common-add:
   Example:
    [lang-Common-add][5 5]
   Splits to words, converts words to integers, then adds them together to get a result.
   Useful for, say, r2k LSD save interpreter stack depth adjustment.

  lang-Common-r2kTsConverter:
   Example:
    [lang-Common-r2kTsConverter]A
   Converts a TDateTime (float) to a human-readable date format.

```

* `lang-Common-add` was replaced with... just MVM.

* `lang-Common-r2kTsConverter` was replaced with a dedicated MVM function.

## Extended Format-Syntax

...is still in use, so it doesn't yet belong here. But some features have been removed!

* FormatSyntax used to be entirely runtime, which lead to some funny bugs (including basically being *the* reason there is a `v1.4-3`).
  
  * These days it's not completely dealt with, but the worst "this will crash" cases have proper exceptions for them.

* In the `[@Class.RPG::AudioFile]` syntax, name routines are now looked up at compilation time. This used to be at runtime.

* The conditional syntaxes `=` and `:`  no longer use `interpretParameter` at all.
  In particular the details on `=` used to read:

```
  The third checks if a parameter, interpreted as '#A' where A is the parameter-character, with prefixes always disabled,
   is equal to a literal text string.
  This should only be used for numbers/enums and booleans, really.
  This can be used as a nicer way to output int_booleans...
  {A=1=(enable)|(disable)}
  ... or as a way to handle "invert" options. (Note the inverted order of the text.)
  {A=1=OFF|ON}
```

*Except for the very small problem that every use of this functionality assumed it never interpreted the parameter, leading to bugs with r2k's camera pan control command...*

* The section on interpretations used to have the ability to perform a name routine on a dynamically constructed string, as that version's closest equivalent to function calls, with the syntax `[my-routine][some extendedformatsyntax]`.

```
  If the character '@' prefixes the interpretation type, the type name is considered to be the full name routine's ID,
   and all the parameters are given to the new name routine (no parameter is postfixed)
  If a parameter is supposed to be postfixed, and that parameter is '[', then the parameter is the formatted text within there (treated as RubyIO string)
   (For example, [add-one-to-integer][[add-one-to-integer][0]] would be a theoretical way to get 2.)
  Note, however, that in order for this to work properly the text goes through the system encoding in the interim.
  Thus, DO NOT use this syntax to handle translation text!!!
```

* It used to be that command names in particular would magically feed their parameter schemas to FormatSyntax. This is no longer the case. This wasn't really directly documented because it was "magic".

```
  @ causes the next outputted value to be prefixed if it's an enumeration.
  This is more-or-less always used as the "command" "@#A" or "@#B" or such, for "output prefixed enumeration".
```

* `interpretParameter` used to prefix `Interp.` when looking up interpretations.
  This was used a handful of times and made the rules even more confusing.

* There used to be a way to check that one parameter is equal to another. This was rarely used, so all uses got migrated to TRC.

```
  The second checks if a parameter is the same as another. (This check is based on RORIO.rubyEquals)
  This can be used for range specification (emulating # in the Original Format, but with flexibility):
   {AB:#A|@#A..@#B}
```

