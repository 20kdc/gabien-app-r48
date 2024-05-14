# DBLoader

DBLoader _was_ the base underlying format behind the old databases.

It has now been replaced with a shell of itself that pretty much just redirects to Datum, and `IDatabase` has been expanded to make this mostly work.

Here's how _that_ works, as a Datum format:

* The file is a sequence of "commands". Each command is a list starting with the command name.
* The command `obj` as in `(obj 123 "Example Text")` calls `IDatabase.newObj(123, "Example Text")`. This is also known as `123: Example Text` because that's how it used to be done.
* Any other commands must be defined by the user

There are _literally years worth_ of legacy stuff I _could_ be talking about in this document.

Instead I converted everything all at once for once and then axed the old files.

If it looks like everything was automatically converted, that's because it was. Trust me, it's better this way; at least now there's a **hope** of refactoring it all.

The new setup will try to convert whatever you throw in for args into strings. Which... ok, yes, that sounds bad, I'll admit. But for the smaller `DBLoader` uses it should make the migration to `DatumLoader` a lot easier to stage if you can find a syntax that will work before and after whatever refactor one is doing.

Sadly, the big ones (read: SDB) will be a long time in the making.

Having to figure out how to make code like `(> f_pano_name { string imgSelector Graphics/Panoramas/ Panoramas/ })` actually work in a logical, non-stupid fashion is going to be a _battle._

The upside is that the need for the `C datum ""` mechanism is just gone now. It can just be, like, `(vm (Literally Just Directly Writing VM Code Using Datum))`. Or literally anything else. The downside is that this will take effort, and meanwhile the parts where Datum is embedded into DBLoader code are presently the _worst parts_ of the conversion by far.

Maybe by the time you read this I'll have dealt with that. I know at least it won't have done anything 'nice' to the command name database, which will be the worst affected part.

If I haven't, and you're someone taking over from me for whatever reason...

* _Use the unit tests._ They are there for the express purpose of reparsing everything to make sure nothing set on fire.
* Lay `throw new RuntimeException("Don't use this!")` traps when you want to replace a feature, then unit-test until you've flushed it out of the system.
* Assemble Local Test Executive fodder. This is your go-to unit testing check to ensure Schema isn't playing up.