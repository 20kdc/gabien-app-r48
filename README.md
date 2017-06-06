# gabien-app-r48

An editor for RPG Maker 2000/2003 (support incomplete), RPG Maker XP, RPG Maker VX Ace, and Ikachan. Also a sticky note program.

Support for these was gained by the following:

RPG Maker 2000/2003 (incomplete): Reading the format documentation in EasyRPG's (liblcf)[https://github.com/EasyRPG/liblcf]
 (I have checked, this use is fine and doesn't require a PD-breaking MIT header, which would have resulted in me splitting the repository),
 some guesswork, and checking the results against OneShot (legacy), Ib v1.07 and the EasyRPG Test Game.

RPG Maker XP: Developed by examining OneShot (remake) and Ruby Quest : Undertow. The rendering code may thus be incomplete.

RPG Maker VX Ace: Same, plus a table given by Ancurio for the wall ATs, but examined games were Rave Story 2 (note - there are two versions out there. The real one is Polar's, and it's VX Ace-based.) and Crysalis.

Ikachan: Ancient knowledge passed down from our elders.

Sticky Note Program: This is really just an ancient testbed for the simpler elements of R48, don't use seriously ;)

## Licensing and disclaimer:

This is released into the public domain.

No warranty is provided, implied or otherwise.

To re-iterate the previous sentence, any usage of this program is entirely your responsibility.

Instructions:

    1. Clone the common, JavaSE and target application repositories.
    2. Build the common, JavaSE and application repositories in that order.
    3. Run the application repository.

If using IDEA, run the idea generation on all three repositories seperately,
 then open IDEA on the application repository and fix up the dependencies.

Do something similar with your Eclipse workspace if you use Eclipse,
 only now you don't have to be careful as to open the application first,
 or repeat some of the steps for every application being edited.

(This is as simple a system as I could work out.)
