# gabien-app-r48

An editor for RPG Maker 2000/2003, RPG Maker XP, RPG Maker VX Ace, and Ikachan. Also a sticky note program.

Support for these was gained by the following:

RPG Maker 2000/2003: Reading the format documentation in EasyRPG's (liblcf)[https://github.com/EasyRPG/liblcf]
 (I have checked, this use is fine and doesn't require a PD-breaking MIT header, which would have resulted in me splitting the repository),
 some guesswork, and checking the results against OneShot (legacy), Ib v1.07 and the EasyRPG Test Game.

RPG Maker XP: Developed by examining OneShot (remake) and Ruby Quest : Undertow. The rendering code may thus be incomplete.

RPG Maker VX Ace: Same, plus a table given by Ancurio for the wall ATs, but examined games were Rave Story 2 (note - there are two versions out there. The real one is Polar's, and it's VX Ace-based.) and Crysalis.

Ikachan: Ancient knowledge passed down from our elders.

Sticky Note Program: This is really just an ancient testbed for the simpler elements of R48, don't use seriously ;)

## 'I wanted to examine a game, but when installing the game I didn't switch locale, and R48 is failing to load images'

This hasn't been reported to me yet, but I know someone is going to ask, and I want something to point them at.

If everything is well, the filenames will be in the native language of the game (as in, *not* a corrupted version of your native language)

Otherwise, reinstall the game with the correct locale. I hear there's a tool on Windows called AppLocale for this?

When using Wine, the correct way to do this to my knowledge is something along the lines of:

    LANG=ja_JP.UTF-8 wine shiftjisappinstaller.exe

(Yes, despite the "UTF-8" it will properly generate the SHIFT-JIS.)

Fix your game install, then use the correct encoding in the R48 launcher menu. This will fix the issue.

## Licensing and disclaimer:

This is released into the public domain.

No warranty is provided, implied or otherwise.

To re-iterate the previous sentence, any usage of this program is entirely your responsibility.

## Build instructions:

    1. Clone the common, JavaSE and target application repositories.
    2. Build the common, JavaSE and application repositories in that order.
    3. Run the application repository.

If using IDEA, run the idea generation on all three repositories seperately,
 then open IDEA on the application repository and fix up the dependencies.

Do something similar with your Eclipse workspace if you use Eclipse,
 only now you don't have to be careful as to open the application first,
 or repeat some of the steps for every application being edited.

(This is as simple a system as I could work out.)
