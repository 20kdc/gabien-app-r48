
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; GI-M Command List

(obj 10830 "(\"Teleport Player By Vars\"(? ]3(\" \"(@ ]1 var_id #t)\", \"(@ ]2 var_id #t)\", \"(@ ]3 var_id #t))))")
(C category \3)
(d Like Teleport Player, except more complicated.)
(p _ string)
(p mapVar var_id)
(p xVar var_id)
(p yVar var_id)

(obj 10710 "\"Begin Encounter\"")
(C category \3)
(d Begin a battle between the player and an enemy!)
(p _ string)
(X begin_encounter_parameters)
(C commandIndentConditionalOF \4 \2 \5 \1)
(C groupBehavior condition @parameters]4 \2 form \20710 Victory \20711 Escape \20712 Defeat \20713)
(C groupBehavior condition @parameters]5 \1 form \20710 Victory \20711 Escape \20712 Defeat \20713)

(obj 20710 "\"... Victory:\"")
(C category \3)
(d The code called in an Encounter when the player wins.)
(p _ string)
(C groupBehavior expectHead \10710 \20710 \20711 \20712)
(K \10710)
(i \-1)
(I \1)
(obj 20711 "\"... Escape:\"")
(C category \3)
(d The code called in an Encounter when the player escapes.)
(p _ string)
(C groupBehavior expectHead \10710 \20710 \20711 \20712)
(K \10710)
(i \-1)
(I \1)
(obj 20712 "\"... Defeat:\"")
(C category \3)
(d The code called in an Encounter when the player is defeated.)
(p _ string)
(C groupBehavior expectHead \10710 \20710 \20711 \20712)
(K \10710)
(i \-1)
(I \1)

(obj 20713 "\"End Encounter\"")
(C category \3)
(d Ends an Encounter Block.)
(p _ string)
(C groupBehavior expectHead \10710 \20710 \20711 \20712)
(i \-1)
(l)

(obj 10720 "(\"Show Shop\"(? ]3(\": \"(@ ]1 show_shop_bstype)\", message set \"(@ ]2 show_shop_type))))")
(C category \0)
(d Shows a shop. Note that this only acts as a block if hasInnerCode is enabled.)
(p _ string)
(X show_shop_parameters)
(C commandIndentConditionalIB \3)
(C groupBehavior condition !@parameters]3 \0 form \20720 Transaction \20721 Refuse\ Transaction \20722)

(obj 20720 "\"... Transaction:\"")
(C category \0)
(d Begins the code for when the player does business of some form with the Shop.)
(p _ string)
(C groupBehavior expectHead \10720 \20720 \20721)
(K \10720)
(i \-1)
(I \1)

(obj 20721 "\"... Refuse Transaction:\"")
(C category \0)
(d Begins the code for when the player chooses not to do business with the Shop.)
(p _ string)
(C groupBehavior expectHead \10720 \20720 \20721)
(K \10720)
(i \-1)
(I \1)

(obj 20722 "\"End Shop\"")
(C category \0)
(d Ends a Shop Block.)
(p _ string)
(C groupBehavior expectHead \10720 \20720 \20721)
(i \-1)
(l)

(obj 10730 "(\"Show Inn\"(? ]3(\" typeB: \"(@ ]1)\", price: \"(@ ]2)\" \"(if-eq ]3 0()(\" (result branch follows)\")))))")
(C category \0)
(d Shows an Inn\'s screen. Note that this only acts as a block if hasInnerCode is enabled.)
(p _ string)
(p innType2 int_boolean)
(p price int)
(p hasInnerCode int_boolean)
(C commandIndentConditionalIB \3)
(C groupBehavior condition !@parameters]3 \0 form \20730 Stay \20731 Refuse\ Stay \20732)

(obj 20730 "\"... Stay:\"")
(C category \0)
(d Begins the code for when the player chooses to stay at the Inn.)
(p _ string)
(C groupBehavior expectHead \10730 \20730 \20731)
(K \10730)
(i \-1)
(I \1)

(obj 20731 "\"... Refuse Stay:\"")
(C category \0)
(d Begins the code for when the player chooses not to stay at the Inn.)
(p _ string)
(C groupBehavior expectHead \10730 \20730 \20731)
(K \10730)
(i \-1)
(I \1)

(obj 20732 "\"End Inn\"")
(C category \0)
(d Ends an Inn Block.)
(p _ string)
(C groupBehavior expectHead \10730 \20730 \20731)
(i \-1)
(l)

(obj 10740 "(\"Enter Party Member\"(? ]1(\" \"(@ ]1 actor_id #t)\"'s\"))\" Name\")")
(C category \2)
(d For changing the name of a party member. Because players really don\'t care about Crono\'s parents or their naming decisions.)
(p _ string)
(p target actor_id)
(p useCharset2 int_boolean)
(p showOldName int_boolean)

(obj 10810 "(\"Teleport Player\"(? ]3(\" to \"(@ ]1 internal_mapDictionary #t)\" : \"(@ ]2)\", \"(@ ]3)(? ]4(\" dir \"(@ ]4))))))")
(C category \2)
(d Teleports the player to a given map and position. If in a parallel event, continue running \(orphaned from character\), otherwise, terminate.)
(p _ string)
(p map map_id)
(p x int)
(p y int)
(X teleport_player_2k3_escapehatch)

(obj 10840 "\"Player Enters Or Leaves Vehicle\"")
(C category \2)
(d Enters or leaves an airship occupying the same space as the player, or a ship/boat directly in front of the player. It is extremely picky about this.)
(p _ string)

(obj 11060 "(\"Camera Pan\"(? ]5(\" \"(if-eq ]1 2((@ ]2 direction)\" \"(@ ]3)\" tiles speed \"(@ ]4))((@ ]1 camera_pan_control_mode)))(if-eq ]1 3(\" speed \"(@ ]4)))\" (\"(if-eq ]5 0(\"no \"))\"wait)\")(\" Control\")))")
(C category \3)
(d Controls where the \'camera\' is looking.)
(p _ string)
; Unfortunately for humanity, Pv-Syntax is powerful enough to represent this, which means I'm obligated to do it rather than just X-trap.
(p mode camera_pan_control_mode)
(P \1 _ int)
(v \2 direction direction)
(P \1 _ int)
(v \2 distTiles int)
(P \1 _ int)
(v \2 speed int)
(v \3 speed int)
(P \1 _ int)
(v \2 wait int_boolean)
(v \3 wait int_boolean)

; NOTE: WHEN WRITING PICTURE STUFF:
;  + use Monolith2 for the specials

(obj 11110 "\"Show Picture\"")
(d Adds a picture to the screen. Useful for important flowers.)
(p _ string)
(X show_picture_parameters)

(obj 11120 "\"Move Picture\"")
(d Moves an on-screen picture.)
(p _ string)
(X move_picture_parameters)

(obj 11130 "\"Erase Picture\"")
(d Erases a picture from the screen.)
(p _ string)
(X erase_picture_parameters)

; ---

(obj 11210 "(\"Show Animation\"(? ]4(\" \"(@ ]1 animation_id #t)\" at \"(@ ]2 character_id #t)(if-eq ]3 0()(\" (wait)\"))(if-eq ]4 0()(\" (global)\")))))")
(C category \3)
(d Show an animation on an event/the player.)
(p _ string)
(p anim animation_id)
(p char character_id)
(p wait int_boolean)
(p global int_boolean)

(obj 11320 "(\"Flash Character\"(? ]7(\" \"(@ ]1 character_id #t)\" [\"(@ ]2)\",\"(@ ]3)\",\"(@ ]4)\",\"(@ ]5)\"] for \"(@ ]6)\"/10 seconds\"(if-eq ]7 0()(\" (wait)\")))))")
(C category \3)
(d Make an event/the player flash in a given way for a given length of time \(given in tenths of a second.\))
(p _ string)
(p char character_id)
(p red0-31 int_default_31)
(p green0-31 int_default_31)
(p blue0-31 int_default_31)
(C r2kFETonePicker \2 \3 \4 \5)
(p saturation0-31 int_default_31)
(p timeSecs/10 int_default_1)
(p wait int_boolean)

(obj 11340 "\"Wait Until All MoveRoutes Complete\"")
(C category \3)
(d Exactly what it says on the tin.)
(p _ string)

(obj 11350 "\"Stop All Pending MoveRoutes\"")
(C category \3)
(d I assume there\'s some good reason for this thing\'s existence \- perhaps if you had to chase down a fleeing NPC \- but...)
(p _ string)

(obj 11560 "\"Play Movie\" ($ \" \" ]0)")
(C category \3)
(d Plays a movie, if that\'s possible.)
(p movie f_movie_name)
(p usePosVars int_boolean)
(P \1 xVar var_id)
(v \0 x int)
(P \1 yVar var_id)
(v \0 y int)
(p width int)
(p height int)

(obj 11910 "\"Open Save Menu\"")
(d Opens the Save menu.)
(p _ string)

(obj 11950 "\"Open Main Menu\"")
(d Opens the Main menu, which is, I assume, the menu that shows up when you press the menu key.)
(p _ string)

(obj 5001 "\"Open Load Menu (2K3-E Only)\"")
(d Opens the Load menu.)
(p _ string)

(obj 5003 "\"Toggle ATB Mode (2K3-E Only)\"")
(d Toggles the ATB battle system.)
(p _ string)

(obj 5005 "\"Open Video Options (2K3-E Only)\"")
(d Does absolutely nothing, or, alternatively, opens the video options.)
(p _ string)