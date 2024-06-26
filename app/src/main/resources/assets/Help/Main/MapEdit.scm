
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(obj 7 "Map Tools")
(. This would not be very much of an editor if maps were not editable.)
(, For general usage, \"Tiles\", and \"Event\ List\", should be enough.)
(, The \'...\' menu contains those things that aren\'t directly tools.)
(, This includes the ability to create map screenshots, and the map resizer.)
(> \8 Tiles)
(> \9 Event List)
(> Help/Main/Toolsets:1 Back to the Map.)

(obj 8 "Tiles")
(i HelpImg/need-image.png)
(. This allows the editing of the map\'s tiles.)
(. The stride of the tileset depends on the window\'s width.)
(. Clicking on a tile in the map will paste it in.)
(. An area of the tileset can be selected, and it will be interpreted based on the window\'s width, when the tiles are pasted in.)
(> \7 Back to the Map Tools.)

(obj 9 "Event List")
(. This is a rather simple tool to use.)
(. Opening it will show circles at the positions of events.)
(. Clicking a tile will report all events at that position, and give the option to create a new event there, delete one of the existing events, or move an event.)
(> \11 Event Editing Details)
(i HelpImg/9.usage.png)
(> \7 Back to the Map Tools.)

(obj 11 "Event Editing Details")
(. Before I note anything else, this depends on the game.)
(. However, all events have two fields in common: @x and @y.)
(. These fields are used to specify the position of the event in the map.)
(. No matter how the specific engine does things, the toolset relies on these fields being in place.)
(.)
(. The general structure of objects should always be kept in mind when editing.)
(> \12 Object Editing Details)
(.)
(. Game-specific documentation files:)
(> \18 RPG Maker \2000/2003 \(R2K[3]\) Events Overview)
(> \14 RGSS \(RXP, RVXA\) Events Overview)
(> \13 Ikachan Events)
(. Assuming there are schemas for all the commands \(RVXA users take note\), you should be able to use them by looking at the Ruby interpreter scripts as documentation.)
(> Help/Main/MapEdit:9 Back to the Event List guide.)

(obj 12 "Object Editing Details")
(. The \"Schema\" system for editing objects is meant to be usable.)
(. However, it does not make things easy.)
(.)
(. Beforehand, a reminder.)
(. Text fields will revert unless you press enter.)
(. The same goes for number fields.)
(.)
(. Now that emphasis is over, here\'s the notes.)
(.)
(. Firstly:)
(. To the left is generally the field name, to the right is generally the field data.)
(. They are split by a line which should be visible between them.)
(. The field name is not usually editable.)
(.)
(. Secondly, the top \(not the title\) bar does many things.)
(. It shows the object you\'re currently using.)
(. You can use \"Insp.\" for raw inspection.)
(. This is usually useless on backends other than \'r48\' \(RGSS\).)
(. \"..\" lets you go back a level, and \"Save\" does what you\'d expect.)
(. The \"Cp.\" and \"Ps.\" buttons allow copying and pasting a part, so long as it\'s relatively valid, from one place to another.)
(. The system will try to correct any obviously wrong details.)
(. The copy and paste applies to the object being viewed, not the currently selected field or any details.)
(.)
(. Thirdly, if an object ends with a bunch of one-letter buttons, this means it can be one of several types, and clicking those buttons resets it with a type.)
(.)
(. I hope this is enough information to work with.)
(.)
(> \11 Event Editing Details)
(> Help/Main/Toolsets:4 System Objects)

(obj 13 "Ikachan Events")
(. There is only one thing to note about Ikachan events:)
(. Ikachan events are best learned by experimentation, as the meanings of, say, the target offset parameters, aren\'t consistent between NPC types.)
(> \11 Event Editing Details)

(obj 14 "RGSS Events (Overview)")
(. RGSS, or the \'Ruby Game Scripting System\', is. in essence a generic \2D game engine that uses Ruby.)
(. Nothing much about it \- it leaves a lot up to the Ruby scripts, short of some tilemap rendering details and such.)
(. In practice, RPG Maker itself follows specific patterns, and for users, these patterns are law.)
(.)
(. Firstly, @name is a \'useless\' field \(relied upon by some custom Ruby scripts as an indicator\).)
(. @id is just a reference to the parent ID and cannot be used.)
(. @x and @y are the position.)
(. @pages is the array of pages \- each page is a possible state for the event to be in.)
(.)
(. A page has, among other things:)
(.)
(. A @condition, which specifies when this page should be shown.)
(. If more than one page is valid, the last page wins.)
(. This is made up of \'valid\' flags, followed by their parameters \- the parameters are ignored if the valid flag is false.)
(. The final condition is the logical AND of all valid parts.)
(.)
(. A @move_route, which specifies the sequence of movements the event performs in this page.)
(> \15 RGSS Events \(Moveroutes\))
(. A @move_type, @move_frequency and @move_speed, which specify how the event moves.)
(. A @through flag, which specifies if the event can be walked through.)
(. @step_anime and @walk_anime flags, which enable/disable step/walk animations.)
(.)
(> \17 A @graphic to show the event in-world.)
(. A @priority_type, which controls how the event shows up when other events overlap it.)
(. A @direction_fix flag \- if true, the event will NOT face towards the player on interaction.)
(.)
(. A @trigger, dictating what causes the code in the @list to execute.)
(> \16 A @list of commands.)
(.)
(> \11 Index Page Of Event Editing Details)

(obj 15 "RGSS Events (Moveroutes)")
(. Like EventCommands, except without indentation, and can\'t do as much.)
(. They can flip switches, rotate towards or away from the player, move about, jump, turn randomly, and all sorts of other fun.)
(. But they are useless for computation.)
(.)
(. A technique I saw once was to continually rotate an actor to achieve an animation.)
(. For this, direction_fix should be true, step_anime and walk_anime should be false.)
(. As for the moveroute itself, it should be set to loop, consisting solely of a turn right and perhaps an insertion point.)
(.)
(> \14 Back to RGSS Events \(Overview\))

(obj 16 "RGSS Events (EventCommands)")
(. EventCommands are the bread and butter of how you actually write any story into a RPG Maker game.)
(. They also change so much between versions that to describe them all in here would be ridiculously complicated.)
(. ...You\'ll just have to guess.)
(. For reference, look at your Interpreter Ruby scripts.)
(.)
(. To change the type, click on the command, then click on the command type at the top.)
(.)
(. A consistent thing to note is that \"MoveRoute\ Editor\ Helper\ NOP\" or such are absolutely ignored in-game.)
(. The \'Insert Point\' is also ignored in-game.)
(.)
(> \14 Back to RGSS Events \(Overview\))

(obj 17 "RGSS Events (Graphics)")
(. Graphics are used to display an Event in the world.)
(. Interestingly, RVXA has less features than RXP in this regard \- it has no support for hue modulation.)
(. \(Which is good, since R48 doesn\'t support viewing hue modulation anyway.\))
(.)
(. Graphics have a @pattern, indirectly indicating the X position within the spritesheet. \(Starts at \0, goes up to \3 on RXP, \2 on RVXA.\))
(. They have a @tile_id, which should be \0 unless you want the graphic to be from the map tileset.)
(. They have a @direction, which indicates the direction of the event, and indirectly indicates the Y position in the spritesheet.)
(. Finally, graphics have a @character_name + hue or index.)
(. The @character_name specifies the image to get the character from, while the other two fields are version-specific.)
(. RXP has the hue, while RVXA has the index \- the index allows putting many characters on the same spritesheet.)
(.)
(. In RXP, the spritesheet is always divided into \16 images, as a grid of \4 images by \4 images, evenly split across the sheet.)
(. The rules I note above about X and Y position apply on that grid, though the direction IDs are divided by \2 and then decremented first.)
(.)
(. In RVXA, the rules are a little complicated, and I don\'t understand them entirely, but part of it has to do with image names.)
(.)
(. Firstly, by default the spritesheet is considered a \12x8 grid of images.)
(. There are \3 patterns, IDs \0 to \2. There are the \4 directions. And there are \8 character indexes, from \0 to \7. \3 * \4 * \8 = \12 * \8.)
(.)
(. From this, follow these rules:)
(.)
(. If the image name does NOT begin with \'!\', then a \4-pixel offset is applied to push the image upwards.)
(.)
(. Disregarding a \'!\' if it\'s there, if the remainder of the image name begins with \'$\', completely different rules apply:)
(. Rather than a \12x8 grid, it is a \3x4 grid, and the character index should be \0.)
(. This allows avoiding wasted space.)
(.)
(. Finally, note that the prefixes are not removed before attempting to load the file.)
(.)
(> \14 Back to RGSS Events \(Overview\))

(obj 18 "RPG Maker 2000/2003 Events Overview")
(. RPG Maker \2000/2003 \(hereon referred to as R2k\) Events are different in some ways, and the same in others, to later RPG Maker events.)
(.)
(. The primary things to note:)
(.)
(. Events are split into pages.)
(. The last page with all matching conditions \"wins\" .)
(.)
(. The @condition controls when the page applies.)
(. The @trigger controls when the code in the event page runs.)
(. The @list is the code of the event.)
(.)
(. The @move_type controls movement first and foremost, like in RGSS.)
(. The @move_route only applies if the @move_type is \"custom\" .)
(. The @layer an event is on affects it\'s collision with the player.)
(. The @block_other_events flag indicates this blocks any other event from entering where this event is \- and this event from entering any other.)
(.)
(> \19 The Graphics panel controls how an event looks.)
(.)
(> \11 Index Page Of Event Editing Details)

(obj 19 "R2k Event Graphics")
(. Event graphics are quite important in R2k, given the limited abilities of tile graphics there.)
(. Event graphics are still not entirely flexible, but they stay stuck on the map, which is useful.)
(.)
(. There are two kinds of event graphics. Tile event graphics and character event graphics.)
(.)
(. If the @character_name is blank, tile event graphics are used. The @character_index + \10000 is the tile ID displayed.)
(. This is why the \10000-10143 tile range is marked \'EV.TileIndexes+10000\'.)
(. Getting a tile index from there and subtracting \10000 gives you a @character_index.)
(. Contrary to the name, event graphics can allow \(for example\) crossing bridges, at least on the belowPlayer layer.)
(. \(The EasyRPG Test Game \2000 demonstrates this. It also has several rendering tests I found useful during R48 development.\))
(.)
(. If the @character_name is not blank, character event graphics are used.)
(.)
(p \4)
(I R2K/char.png \0 \0 \288 \256)
(p \4)
(. Above is a simple example, though the characters are the same character copy & pasted \- an artist I am not.)
(.)
(. Character event graphics use a spritesheet of \288x256, divided into \8 characters.)
(. \(EasyRPG Player-only: If prefixed with a \'$\', the spritesheet can be any size.\))
(. This division, which the character index refers to, is \4 columns, \2 rows, of the form:)
(.)
(. \0123)
(. \4567)
(.)
(. \(Hopefully the above text shows up correctly.\))
(. The characters are in turn divided into \3 columns, \4 rows.)
(. This makes for a total of \12 columns, \16 rows \- each individual sprite being \24x32.)
(.)
(. In any case, each character has \3 patterns, \0, \1 and \2, each being a column.)
(. \1 is considered the \"standing\" pose of a character, and you should typically set the pattern to \1.)
(. Patterns \0 and \2 are the right and left \(relative to the character, i.e. the \'up\' sprites\) leg of the character going forward, respectively.)
(. Note that these pattern values, given RPG Maker \2000/2003\'s design, are standard when using open-source replacement RTPs.)
(. \(I do not believe using non-open-source non-replacement RTPs \- that is, those owned by KADOKAWA, Enterbrain, Degica, or similar \- with R48 is a good idea, nor do I endorse such activity.\))
(. Note that the pattern will continually \"walk\" if @anim_type is set to \"always\" .)
(.)
(. Each character also has \4 directions, this being the row.)
(. The directions are up, right, down, left.)
(. Note that if the @anim_type does NOT say \"noActDirchange\", the character will temporarily change direction to face the player when \'talking\'.)
(. This appears to mean \"interaction\ triggered\ by\ the\ player\ on\ the\ event\ somehow.\")
(. If the @anim_type is \"spin\", the direction will always change. All the time. In a continuous loop.)
(.)
(> \11 Index Page Of Event Editing Details)
(> Help/Main/Entry:21 R2k Backend Quickstart)
