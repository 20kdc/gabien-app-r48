
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(T he help file, though it could easily serve as a Twine-like engine if you so chose.)

(obj 0 "Index")
(h)
(, R48 Usage Manual)
(,)
(. Welcome to the R48 usage manual.)
(, R48\'s interface is essentially a miniature desktop.)
(p \4)
(I HelpImg/0.demo.png)
(p \4)
(. It\'s split into the tab bar, with a global \'save\' button, and the tab contents.)
(, Here, no tab is active, so it\'s a field of blue squares.)
(, Tabs can be turned into floating windows and back with the window-like-button on them.)
(, Closable tabs/windows will have a red button on them.)
(.)
(. R48 expects text or number field edits to be confirmed by pressing enter.)
(, Not pressing enter cancels the edit.)
(.)
(. The tab names should describe what they do, but in any case, for further detail, see the below pages:)
(> Help/Main/Toolsets:1 Map)
(> Help/Main/Toolsets:2 MapInfos)
(> Help/Main/Toolsets:4 Database Objects)
(> Help/Main/Toolsets:6 System Tools)
(.)
(. Before you continue, if you are just starting out and trying to make something to run on EasyRPG with this, read the following.)
(> \21 R2k Backend Quickstart)
(. While if you just want to use this to increase your gold in a \2k/2k3 game \(you know who you are\)...)
(> \23 R2k Savefiles Quickstart)
(.)
(. Note that to ensure your work is saved, remember to press the button at the top-right to save your work.)
(, Furthermore, keep backups. :\))
(> CREDITS Credits)

(obj 21 "R2k Backend Quickstart")
(. RPG Maker \2000/2003 is, as you probably know, emulated by the \(unrelated to me\) open-source EasyRPG Player project.)
(. As RPG Maker \2000/2003 does not have any Core Scripts to deal with, it means that with a full editor, games can be created from scratch.)
(. Step \1. Create a blank folder.)
(. Step \2. In the folder, create the folders \"ChipSet\" and \"System\" . \(capitalization is important.\))
(. Step \3. Extract assets/R2K/templatetileset.png from this JAR \(it\'s a ZIP file\) and put it in ChipSet. Do not rename it.)
(. Step \4. Extract assets/R2K/System.png from this JAR \(it\'s a ZIP file\) and put it in System. Do not rename it.)
(. Step \5. Copy the program JAR into the folder and run it, or alternatively \(if you understand how to use Root Path\) simply point it there.)
(. Step \6. Once you select RPG Maker \2000/2003, there should be \3 modified files, and the screen showing up should contain a grid flashing between different colours.)
(. Step \7. Save all modified files, and begin work. You will need a knowledge of how RPG Maker \2000/2003 games are structured to continue, but it should work.)
(.)
(. Particularly Common Operations:)
(> Help/Main/Toolsets:2 To manage maps, check the MapInfos tab.)
(. The start location of the player and vehicles is stored in Database Objects / RPG_RT.lmt.)
(.)
(. General Structure:)
(. RPG_RT.ldb and RPG_RT.lmt: Data files used to run the game.)
(. RPG_RT.ini: Optional and not handled by R48. Starts with the line \"[RPG_RT]\", and ought to contain \"GameTitle=your\ title\ here\" .)
(. Backdrop: Battle background images. \320x240. Non-transparent?)
(. Battle: Animation images. \480x480, \96x96 per cel.)
(. Battle2: RPG Maker \2003 larger animation images. \640x640, \128x128 per cel.)
(. BattleCharSet: Player party images. \(This is probably \2003-only.\) \144x384, \48x48 per image, one character per row.)
(. BattleWeapon: Weapons used for attack animations. \(This is probably \2003-only.\))
(> \19 CharSet: Event, player and vehicle images. \288x256.)
(. ChipSet: Tilesets. The best way to understand this is probably to look at the template and experiment. \480x256.)
(. FaceSet: Faces. \192x192 total, \48x48 for each face.)
(. Frame: Don\'t use this, really. Just use a Picture. \320x240.)
(. GameOver: \320x240 images for Game Over.)
(. Monster: Enemy images. Can be any size.)
(. Music: Music, in various formats.)
(. Panorama: For the \"parallax\" fields. Non-transparent? \(Accurate parallax emulation in an editor is impossible due to it being gameplay-dependant\))
(. Picture: Arbitrary images to be shown on screen using specific commands.)
(. Sound: Sound effects for various things.)
(> \22 System: Alters the general appearance of system frames and text. \160x80.)
(. System2: For things used in RPG Maker \2003. \80x96.)
(. Title: Title screen images. \320x240.)
(> \0 Back to Index)

(obj 22 "R2k Backend 'System' Explaination")
(. The \"System\" images are \160x80, and follow a complex layout.)
(. The bottom \32 pixels are \16x16 colour swabs for text rendering.)
(. The \4th is used for disabled items, the \5th is used to mean \"critical\", and the \6th means dead.)
(. The \10th, meanwhile, is used for healing actions.)
(. The next highest \16 pixels are broken unevenly into \16 pieces.)
(. Horizontally, these are:)
(. \16 pixels: Main Background Swab)
(. \16 pixels: Main Shadow Swab)
(. \8 pixels times \10: Digits \0 to \9)
(. \8 pixels: \':\')
(. \8 pixels: Unused \(or maybe space\))
(. \16 pixels: Shadow Large)
(. \16 pixels: Shadow Small)
(. The remaining \32 vertical pixels are broken into \4.)
(. \32 pixels: Window Frame Swab \(can be stretched or tiled depending on RPG::System settings\))
(. \32 pixels: Frame \(outer \8 pixel border\) and arrows \(inner \16x16 pixels block split in \2, up arrow being upper\))
(. \32 pixels: Selection Frame, animation frame \1)
(. \32 pixels: Selection Frame, animation frame \2)
(. \32 pixels: This is further split into \4 \8-pixel wide frames of \4 \8-pixel high change indicators.)
(. From the top of the indicators, these are \"positive\", \"same\", \"negative\", and a \"E\" \(Equipped?\).)
(> \21 Back To Quickstart)

(obj 23 "R2k Savefiles Quickstart")
(. There are many RPG Maker \2000/2003 games out there. That\'s a fact at this point.)
(. And there are quite a few people, \(maybe including you, as you\'re reading this!\) who want to edit savefiles in these games.)
(. Unfortunately, a RPG Maker \2000/2003 savefile is rather complicated! But that won\'t be an issue.)
(. The Saves panel lets you load or create savefiles.)
(. Creating a savefile is experimental, but should bring you to the start of the game, allowing you to inject items you shouldn\'t have, adjust actors, etc. from the very start.)
(. Savefiles are shown in the Map panel.)
(. Most of this simply acts as a display, though \- the details are in \'...\' \-> \'Properties\'.)
(. Let\'s start with the basic outline...)
(. @title : Defines how this savefile appears in the Load menu.)
(. @system : Defines various RPG_RT.ldb@system details.)
(. @actors : Contains experience, skills, battle commands \(if applicable\) of actors.)
(. @party : Contains party members, the items list, gold, timers, and some generic stats.)
(. @party_pos \(accessible as the \"Party\" event on-world\): The current player position and details.)
(. @boat_pos \(if party\'s on the same map, accessible as \"Boat\" \): The boat\'s position & details.)
(. @ship_pos \(if party\'s on the same map, accessible as \"Ship\" \): The ship\'s position & details.)
(. @airship_pos \(if party\'s on the same map, accessible as \"Airship\" \): The airship\'s position & details.)
(. @target : Used by the Teleport event. Note that it seems a fake target is sometimes added by RPG_RT.)
(. @map_info : Contains details from the MapInfo entry regarding this map, tile remaps, events, and parallax info.)
(. @screen : Contains information on global screen effects. Some of this is uncertain \(contact EasyRPG with your findings, they\'ll tell me\).)
(. @pictures : Maps Picture IDs to their information.)
(. @main_interpreter : The Interpreter used for non-parallel event code.)
(. @common_events : The Interpreters of common events.)
(. Reset Events & Version : Using this will delete all events from the savefile and add in the map\'s events and set the save-counts.)
(. Using it and then removing a particular event *should* allow you to bypass event obstacles, though note that some events may stop working entirely simply by it\'s use.)
(. If you simply want to reliably teleport the player, use the next button:)
(. Try To Get RPG_RT To Reset The Map \(not shown\) : Tries to get RPG_RT to reset the map by setting this as an older save and getting rid of event info.)
(. This is incredibly useful if you intend to teleport yourself across maps and *don\'t* wish to disturb events at your destination.)
(. Note that all events will be converted to ghosts.)
(.)
(. Regarding what I mean by a \"ghost\" here, a \"ghost\" is what I\'m calling events which will appear in the map that aren\'t in the save.)
(. R48 detects when ghosts are likely to occur and show them \- and lets you put them in the savefile!)
(. But this is done in the same way as Reset Events, so... possibly unreliable.)

(obj 800 "Help Page On Help Page Allocations")
(. \0: The first page.)
(. \1-799: Documentation, not referred to by code.)
(. \800-899: These pages are for self-documentation.)
(. \900-999: These pages are explicitly allocated, and could be referred to from inside code.)
(.)
(> \801 The Help Format)
(> \802 Additional Information)

(obj 801 "The Help Format")
(. The help format is a simple markup format.)
(. It is meant for easy, fast layout.)
(. It uses the normal DB format, divided into sections with the usual numbered-object-creation syntax.)
(. For example, \"801:\ The\ Help\ Format\" begins page \801.)
(. Pages should, but do not have to be, ordered.)
(. The following \'commands\' exist:)
(. \'.\': Create a label. Automatic word-break is always on, but line-break also occurs.)
(. \'h\': Like \'.\', but centred.)
(. \',\': Adds a line break and more text to the previous label.)
(. \'p\': Generate some amount \(given as a number\) of blank space.)
(.)
(. The following two commands draw images.)
(. They take either one parameter \(the image filename\) or five parameters \(image filename, X, Y, width, height.\))
(. The five-parameter version is useful if a cropping of an image should be used.)
(.)
(. \'i\': Draw a image to the right side of the page, with text continuing on the left \(Mediawiki-style but with no \'undertext\'.\))
(. \'I\': Draw a centred image, useful if you want to draw attention to it / give it a margin.)
(.)
(> \800 back)

(obj 998 "Internal Tool Documentation 'RMAnimEditor'")
(. Sometimes, you just need to edit a battle animation.)
(, And you can\'t do that very easily from the standard interface, though it is possible.)
(, RMAnimEditor was written for making things easier, but it\'s kind of weird and doesn\'t render all cases.)
(.)
(. Notably, this only covers the @frames section.)
(, The rest, including timings, must be edited manually.)
(, Also keep in mind that the frame numbers displayed are \1-based \- timings use a \0-based system, so subtract \1 to get the timing frame.)
(.)
(. So, without further ado, the layout:)
(.)
(. At the top is the Timeline Bar.)
(, It allows going between frames \(\'<\', \'>\', \'Play\'\).)
(, It allows copying and pasting them \(\'C\', \'P\'\) \- pasting overwrites the current frame, it doesn\'t insert.)
(, And it allows inserting a copy of the current frame as the next frame \(\'+\'\), and removing the current frame \(\'-\'\).)
(.)
(. In the lower-left is the actual Animation View.)
(, A darkened cross on the background is the \"zero\ point\" \- coordinates \0, \0 relative to the animation\'s point on-screen.)
(, Right-button-drags across the Animation View will shift it\'s camera.)
(, Left-button-drags will change the X/Y position of the currently selected cell.)
(.)
(. In the lower right is the Cell View.)
(, This has two of it\'s own panels \- at the top, it has the properties for the selected cell \(or nothing if no cell is selected\) \- at the bottom, it has the cell list.)
(, Animations in RPG Maker \2000/2003 do not have keyframes, but R48 simulates keyframed animation anyway.)
(, As such, each property has a \'keyframe\' button. If orange, this is a keyframe\; pressing the button will \'delete\' it, re-interpolating as necessary.)
(, A frame is considered a keyframe if it marks a change in the linear progression of the animation.)
(.)
(. In particular, changing a property will cause the frames *before and after* to gain keyframe status if they don\'t have it already.)
(, This is because the frames *before and after* still retain the old value of the property.)
(.)
(. This serves as a hopefully functional toolset for modifying animations.)
