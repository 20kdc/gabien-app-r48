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

## Extended Format-Syntax

...is still in use, so it doesn't yet belong here. But some features have been removed!

* In the `[@Class.RPG::AudioFile]` syntax, name routines are now looked up at initial language compilation time, which occurs during the `ensureAllExpectationsMet` phase (core has loaded, but translations haven't)
