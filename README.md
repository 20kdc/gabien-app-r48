# gabien-app-r48

## section 1 : about R48

An editor compatible with RPG Maker 2000/2003, EasyRPG Player,
 RPG Maker XP, RPG Maker VX Ace, and Ikachan. Also a sticky note program.

In *RPG Maker 2000/2003 mode, it allows creating games from scratch*.
For a runtime, you can use [EasyRPG Player](https://easyrpg.org/) (a project not related to R48, serving as an open-source replacement RPG_RT).

As this does not use any assets or executables owned by the makers of RPG Maker, this is an independent open-source free game creation system.

So long as one avoids using RPG Maker assets (original or modified - if the official RTP's license applies, you must not use it), abides by the license of EasyRPG Player and dependencies (which applies to EasyRPG Player's executable and related DLLs), the games made this way are releasable (potentially commercially. Yes, copies of GPL'd software can be sold, even on Steam, so long as you abide by the GPL's rules while doing it. Little Cat Feet's 2016 remake of OneShot, to my knowledge made with RPG Maker XP and MKXP (with their changes to MKXP released), should be a good example of how to do this.)

## section 2 : why

Varying reasons over time.

The reason for RPG Maker 2000/2003 support is so a complete game creation program for EasyRPG Player (a reimplementation of 2000/2003's engine) exists.

So, by using R48 with EasyRPG Player, you get a *free, open-source, game creation suite*, with the features that EasyRPG Player supports which RPG_RT does not, such as 32-bit PNG support and Ogg Vorbis support.

## section 3 : quickstart

To start using R48 for independent game development:

    1. Download a release
    2. Run it (it's a JAR - you may need to get Java. R48 is built and tested using OpenJDK 8.)
    3. Select the UTF-8 encoding, and accept creating a skeleton project.
    4. Download EasyRPG Player so you have a way to run your game, put it in the game directory, make sure to include license details and be sure to mark to indicate they apply to EasyRPG Player and not your game.
    5. (Optional) Create the RPG_RT.ini file for your game. Example below.

    [RPG_RT]
    GameTitle=My Game Title
    [EasyRPG]
    Encoding=65001

## section 4 : build

See [./BUILD.md](./BUILD.md) for this.

## section 5 : support

GitHub Issues are used for feature requests and bug reports.

I'm `@20kdc:matrix.org` on Matrix right now but that might change.

Support over Discord is currently only available by finding me (`20kdc`).

Upon crash, R48 ought to give you the information and data you need to continue, but no guarantees.
If you have a lot of unsaved data, it may simply freeze while writing tons of data into the crash file.
It should not be interrupted during this process.

### 'I wanted to examine a game, but when installing the game I didn't switch locale, and R48 is failing to load images'

This hasn't been reported to me yet, but I know someone is going to ask, and I want something to point them at.

If everything is well, the filenames will be in the native language of the game (as in, *not* a corrupted version of your native language)

Otherwise, reinstall the game with the correct locale. I hear there's a tool on Windows called AppLocale for this?

When using Wine, the correct way to do this to my knowledge is something along the lines of:

    LANG=ja_JP.UTF-8 wine shiftjisappinstaller.exe

(Yes, despite the "UTF-8" it will properly generate the SHIFT-JIS.)

Fix your game install, then use the correct encoding in the R48 launcher menu. This will fix the issue.

## section 6 : license and disclaimer

    gabien-app-r48 - Editing program for various formats
    Written starting in 2016 by contributors (see CREDITS.txt)
    To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
    You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

Note that said copy of the CC0 is in 'COPYING.txt'.

## section 7 : credits

Development by everybody in CREDITS.txt (which is pasted into the version.txt of official builds after v0.7.1)

Support for various target engines was gained by the following:

RPG Maker 2000/2003: Reading the format documentation in EasyRPG's [liblcf](https://github.com/EasyRPG/liblcf)
 (I have checked, this use is fine and doesn't require a PD-breaking MIT header, which would have resulted in me splitting the repository),
 some guesswork, and checking the results against several games to ensure compatibility.

RPG Maker XP: Support was developed by examining OneShot (remake) and Ruby Quest : Undertow. The rendering code may thus be incomplete.

RPG Maker VX Ace: Examining Rave Story 2 (PolarStar's version), and Crysalis, plus a table given by Ancurio for the wall ATs.
Wall AT editing support is slightly inaccurate due to a seemingly inconsistent ruleset, but manual tiling is available should the results be an issue.

Ikachan: Previous experience with modding Ikachan.

Sticky Note Program: This is really just an ancient testbed for the simpler elements of R48, so it's entirely defined by R48.

If you're wondering why this is at the bottom, it's because someone suggested an order for the README sections, and didn't put "credits" in it.

Unit testing is done using JUnit, as the test code isn't packaged anyway.
