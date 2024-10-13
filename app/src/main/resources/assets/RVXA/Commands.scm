
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Event commands for VX Ace.

(\# RCOM/CommonCommands)

; LINE 251

(cmd 101 "Start Text" (? ]0 "" " (c.401/102/103/104)") ($ " w/face " ]0) ($ ":" ]1) (? ]2 (" (" (if-eq ]2 1 "no bkg, " "bkg, ") (@ ]3 change_text_options_position) ")")))
(C category 0)
(d "Begins a segment of text.")
(C tag translatable sayCmd)
(p faceName f_face_name)
(C spritesheet 0 Faces/)
(p faceIndex int)
(p backInvisible int_boolean)
(p position change_text_options_position)
(C groupBehavior messagebox 401)

(cmd 401 "Tx." ($ " " ]0))
(C category 0)
(d "Continues the text.")
(C tag translatable sayCmd)
(C commandSiteAllowed false)
(C textArg 0)
(p text textbox_string)

; Choices and input moved to RCOM/CommonCommands

; LINE 305

(cmd 104 "Input Item" ($ " to " ]0 var_id #t))
(C category 0)
(d "Lets the player choose an item... for some reason.")
(p var var_id)

(cmd 105 "Show Scrolling Text (c.405)")
(C category 0)
(d "Begins some scrolling text.")
(p speed int)
(p notFast boolean)
(C groupBehavior messagebox 405)

(cmd 405 "Scrtx." ($ " " ]0))
(C category 0)
(d "Continue some scrolling text.")
(C tag translatable)
(p text textbox_string)

; Comments (108/408) and conditional branch (111/411/412) and loops (112/113/413) and EEP 115 in CommonCommands
; CCE 117, LBL 118, JMP 119 in CommonCommands

; They still rely on indent for stuff,
; so this is just as unreliable as before.

; 121/122/123 (Control Switch/Set Variable/Control Selfswitch) in CommonCommands.
; 124 (Timer) in CommonCommands.

; operate_value has 3 parameters: Negate, const/var (int_boolean), operand (int_or_var)

(cmd 125 "Gold" ($ " += " : operate_value_0) " Add/Remove")
(C category 2)
(d "Give or take gold from the player.")
(p negate int_boolean)
(p amountIsVar int_boolean)
(P 1 amountVar var_id)
(v 0 amount int_default_100)

(cmd 126 "Item" ($ " " ]0 item_id #t) ($ " += " : operate_value_1) " Add/Remove")
(C category 2)
(d "Give or take items from the player.")
(p item item_id)
(p negate int_boolean)
(p amountIsVar int_boolean)
(P 2 amountVar var_id)
(v 0 amount int_default_1)

(cmd 127 "Weapon" ($ " " ]0 weapon_id #t) ($ " += " : operate_value_1) " Add/Remove")
(C category 2)
(d "Give or take weapons from the player.")
(X weapon_add_remove_parameters)

(cmd 128 "Armour" ($ " " ]0 armour_id #t) ($ " += " : operate_value_1) " Add/Remove")
(C category 2)
(d "Give or take armour from the player.")
(X armour_add_remove_parameters)

; 129 Add/Remove Actor -> RCOM/CommonCommands

(cmd 132 "Set Battle BGM" ($ " to " ]0))
(d "Changes the battle music.")
(p bgm RPG::BGM)

(cmd 133 "Set Battle End ME" ($ " to " ]0))
(d "Changes the musical snippet for the end of the battle.")
(p me RPG::ME)

; Enable/Disable 134/135/136 -> RCOM/CommonCommands

(cmd 137 "Set Formation Accessible" ($ " to " ]0))
(d "Controls if the player can control their formation.")
(p canFormation int_boolean)

(cmd 138 "Change Window Colour" ($ " to " ]0))
(d "Changes the colour of system windows.")
(p tone Tone)

; Why are there *THREE* of these in a row?!??
; (EDIT LATER: At least I've removed the custom schema requirement for two of them.)

(cmd 201 (? ]0 (@ : transfer_player_paramassist) "Transfer Player"))
(C category 2)
(d "Teleports the player to someplace.")
(p useVariables int_boolean)
(P 0 mapVar var_id)
(v 0 map map_id)
(P 0 xVar var_id)
(v 0 x int)
(P 0 yVar var_id)
(v 0 y int)
(p playerDir direction_disable)
(p fadeType transfer_player_fadetype)
(x transfer_player_paramassist)

(cmd 202 (? ]0 (@ : transfer_vehicle_paramassist) "Transfer Vehicle"))
(C category 3)
(d "Teleports a vehicle to someplace.")
(p vehicle vehicle_id)
(p useVariables int_boolean)
(P 1 mapVar var_id)
(v 0 map map_id)
(P 1 xVar var_id)
(v 0 x int)
(P 1 yVar var_id)
(v 0 y int)

(cmd 203 (? ]0 (@ : set_event_location_paramassist) "Set Event Location"))
(C category 3)
(d "Teleports an event to someplace.")
(X set_event_location_paramassist)

(cmd 204 "Scroll Map")
(C category 3)
(d "Scrolls the player's view of the map in a specific way.")
(p dir direction)
(p length int)
(p speed int)

(cmd 205 "MoveRoute Set On" ($ " " ]0 character_id))
(C category 3)
(d "Takes control of a character (event or player)'s MoveRoute.")
(p target character_id)
(p route RPG::MoveRoute)

(cmd 505 "NOP (MoveRoute Editing Helper)")
(C category 3)
(d "Totally useless outside of the VX Ace editor itself - displays the command.")
(p command RPG::MoveCommand)

(cmd 206 "Player Gets On/Off Vehicle")
(C category 2)
(d "If the player's character is in the right situation to get on or off a vehicle (standing on airship/facing ship or boat...), the character will do so.")

(cmd 211 "Player Opacity" ($ " to " ]0))
(C category 2)
(d "Controls if the player is completely invisible or not.")
(p opaque int_boolean)

(cmd 212 "Show" ($ " " ]0 character_id) " Anim." ($ " " ]1 animation_id) (? ]2 (" (wait? " (@ ]2) ")")))
(C category 3)
(d "Shows a battle animation, usually useful for reactions.")
(p char character_id)
(p anim animation_id)
(p wait boolean)

(cmd 213 "Show" ($ " " ]0 character_id) " Balloon." ($ " " ]1 balloon_id) (? ]2 (" (wait? " (@ ]2) ")")))
(C category 3)
(d "Shows a reaction balloon, a feature previously implemented with battle animations.")
(p char character_id)
(p b balloon_id)
(p wait boolean)

(cmd 214 "Temp. Erase This Event")
(C category 3)
(d "Temporarily (until next map load?) erases this event from reality.")

(cmd 216 "Player Followers Inv.?" ($ " " ]0))
(C category 2)
(d "Make characters following the player visible or invisible.")
(p invisible int_boolean)

(cmd 217 "Gather Followers")
(C category 2)
(d "Presumably, this makes characters following the player 'merge into the player'.")

(cmd 221 "Fadeout Screen")
(d "And goodbye.")

(cmd 222 "Fadein Screen")
(d "And hello.")

(cmd 223 "Start Tone Change")
(d "Because it wouldn't be Autumn without the Instagram filter to make it obvious. (Yes, I know it's not Autumn. That's the point.)")
(p tone Tone)
(p time int)
(p wait boolean)

(cmd 224 "Flash Screen")
(d "Light up the screen with a temporary burst of colour! Ensure legal liability warnings are posted before use.")
(p colour Color)
(p duration int)
(p wait boolean)

(cmd 225 "Shake Screen")
(d "Shake things. What more to say?")
(p power int)
(p speed int)
(p duration int)
(p wait boolean)

(cmd 230 "Wait" (? ]0 (" " (@ ]0) " frames")))
(d "Wait some amount of frames.")
(p timeFrames int)

(cmd 231 "Set Picture" ($ " " ]0) ($ " to " ]1))
(d "Create and show a picture on the screen.")
(p id int)
(p image f_picture_name)
(p centred int_boolean)
(p useVars int_boolean)
(P 3 xVar var_id)
(v 0 x int)
(P 3 yVar var_id)
(v 0 y int)
(p scaleX scale)
(p scaleY scale)
(p opacity opacity)
(p blend blend_type)

(cmd 232 "Move Picture" ($ " " ]0))
(d "Move a previously shown picture around the screen.")
(p id int)
; what on earth
; This is actually NIL.
(p _ OPAQUE)
(p centred int_boolean)
(p useVars int_boolean)
(P 3 xVar var_id)
(v 0 x int)
(P 3 yVar var_id)
(v 0 y int)
(p scaleX scale)
(p scaleY scale)
(p opacity opacity)
(p blend blend_type)
(p durationFrames int)
(p wait boolean)

(cmd 233 "Picture" ($ " " ]0) " Rotation Speed" ($ " to " ]1))
(d "Control the rotation speed of a picture, in half-degrees per frame.")
(p id int)
(p speedHDF int)

(cmd 234 "Picture" ($ " " ]0) " Tone Change")
(d "Change the tone of a picture, just in case you hadn't had enough of those Instagram filters. (What do you mean, this joke is getting old fast)")
(p id int)
(p tone Tone)
(p duration int)
(p wait boolean)

(cmd 235 "Erase Picture" ($ " " ]0))
(d "Get rid of a picture.")
(p id int)

(cmd 236 "Set Weather" ($ " to " ]0 weather_type))
(d "Change the weather! This explains why it's never right according to the forecasters.")
(p type weather_type)
(p power int)
(p duration int)
(p wait boolean)

(cmd 241 "Play BGM" ($ " " ]0))
(d "Immediately play a music track.")
(p bgm RPG::BGM)

(cmd 242 "Fadeout BGM over" ($ " " ]0) " seconds")
(d "Fade out the current music track over some amount of time.")
(p seconds int)

(cmd 243 "Save BGM")
(d "Note down the current music track so you can play something else without worrying about what you were playing before.")

(cmd 244 "Resume BGM")
(d "Play the last noted-down music track (see 243)")

(cmd 245 "Play BGS" ($ " " ]0))
(d "Begin to play a background sound, generally meaning 'streams and environmental stuff'")
(p bgs RPG::BGS)

(cmd 246 "Fadeout BGS over" ($ " " ]0) " seconds")
(d "Fade out the background sound over some amount of time, just to help make things silent for The Curb-Stomping.")
(p seconds int)

(cmd 249 "Play ME" ($ " " ]0))
(d "Play a... musical effect.")
(p me RPG::ME)

(cmd 250 "Play SE" ($ " " ]0))
(d "Play a sound effect.")
(p se RPG::SE)

(cmd 251 "Stop SE")
(d "Stop any playing sound effect.")

(cmd 261 "Play Movie" ($ " " ]0))
(d "Play a movie! (May break for any reason at any time.)")
(p movie string)

(cmd 281 "Map Name Inv.?" ($ " " ]0))
(C category 3)
(d "Control if the map name is visible or invisible.")
(p invisible int_boolean)

(cmd 282 "Switch Tileset" ($ " to " ]0))
(C category 3)
(d "Change the current tileset used for the map. I would suggest making sure they are at least reasonably 'compatible' first.")
(p tileset tileset_id)

(cmd 283 "Switch Battleback" ($ " to " ]0) ($ " " ]1))
(d "Change the backgrounds used for battle purposes.")
(p back1 string)
(p back2 string)

(cmd 284 "Switch Parallax" ($ " to " ]0))
(C category 3)
(d "Change the parallax settings.")
(p parallax f_parallax_name)
(p loop_x boolean)
(p loop_y boolean)
(p sx int)
(p sy int)

(cmd 285 "Get Info On Location")
(C category 3)
(d "Note down a specific attribute about a given tile in a variable.")
(p resultVar var_id)
(p infoType get_location_info_type)
(p posVars int_boolean)
(P 2 xVar var_id)
(v 0 x int)
(P 2 yVar var_id)
(v 0 y int)

(cmd 301 "Start Battle (c.601/602/603/604)")
(d "Begin a battle.")
(C category 4)
(p type battle_processing_type)
(P 0 _ int)
(v 0 troop troop_id)
(v 1 troopVar var_id)
(p canEscape boolean)
(p losingDoesNotGameover boolean)

; Battle branches (601/602/603/604) in CommonCommands

(cmd 302 "Shop Processing (c.605)")
(d "The start of a shop. This describes a single item - 605 describes an additional item, repeat as desired.")
(p itemType shop_item_type)
(P 0 thing int)
(v 0 item item_id)
(v 1 weapon weapon_id)
(v 2 armour armour_id)
(p itemPriceEx int_boolean)
(P 2 priceEx int)
(v 0 _ int)
(p purchaseOnly boolean)
(C groupBehavior messagebox 605)

(cmd 605 "Additional Shop Item")
(d "An additional shop item.")
(p itemType shop_item_type)
(P 0 thing int)
(v 0 item item_id)
(v 1 weapon weapon_id)
(v 2 armour armour_id)
(p itemPriceEx int_boolean)
(P 2 priceEx int)
(v 0 _ int)

; 303 Name Actor in RCOM/CommonCommands

(cmd 311 "Actor" ($ " " : iterate_actor_var) " HP +=" ($ " " : operate_value_2))
(d "Heal or harm a party member.")
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)
(p negateMod int_boolean)
(p modIsVar int_boolean)
(P 3 modVar var_id)
(v 0 mod int_default_100)
(p canKill boolean)

(cmd 312 "Actor" ($ " " : iterate_actor_var) " MP +=" ($ " " : operate_value_2))
(d "Heal or harm a party member's magical talents.")
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)
(p negateMod int_boolean)
(p modIsVar int_boolean)
(P 3 modVar var_id)
(v 0 mod int_default_10)

(cmd 313 (? ]2 (if-eq ]2 0 "Add" "Remove") "Change") " Actor" ($ " " : iterate_actor_var #t) " State" ($ " " ]3 state_id #t))
(d "Heal or harm a party member by giving them a state.")
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)
(p remove int_boolean)
(p state state_id)

(cmd 314 "Full recover on actor" ($ " " : iterate_actor_var #t))
(d "Heal a party member to full health.")
(C category 2)
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)

(cmd 315 "Actor" ($ " " : iterate_actor_var) " EXP +=" ($ " " : operate_value_2))
(d "Give a party member free EXP!")
(C category 2)
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)
(p negateMod int_boolean)
(p modIsVar int_boolean)
(P 3 modVar var_id)
(v 0 mod int_default_100)
(p showLevelUp boolean)

(cmd 316 "Actor" ($ " " : iterate_actor_var) " LVL +=" ($ " " : operate_value_2))
(d "Give a party member free levels!")
(C category 2)
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)
(p negateMod int_boolean)
(p modIsVar int_boolean)
(P 3 modVar var_id)
(v 0 mod int_default_1)
(p showLevelUp boolean)

(cmd 317 "Change Actor" ($ " " : iterate_actor_var) " Parameter" ($ " " ]2 cap_type) ($ " by " : operate_value_3))
(d "Modify a party member in confusing battle-related ways.")
(C category 2)
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)
(p paramIdx parameter_id)
(p negateMod int_boolean)
(p modIsVar int_boolean)
(P 4 modVar var_id)
(v 0 mod int_default_10)

(cmd 318 (? ]2 (if-eq ]2 0 "Add" "Remove") "Change") " Actor" ($ " " : iterate_actor_var) " Skill" ($ " " ]3 skill_id #t))
(d "Add or remove a skill from/to a party member.")
(C category 2)
(p actorIsVar int_boolean)
(P 0 actorVar var_id)
(v 0 actor iterate_actor_id)
(p remove int_boolean)
(p skill skill_id)

(cmd 319 "Change Equipment on actor" ($ " " ]0 actor_id))
(d "Change what a party member has equipped. Note that one of the slots can vary between weapon and armour, so you will have to enter the ID manually.")
(C category 2)
(p actor actor_id)
(p slot equipment_slot)
(p weaponId|armourId equipment_id)

(cmd 320 "Rename Actor" ($ " " ]0 actor_id) ($ " to " ]1))
(d "Rename a party member in a way the player can't abuse.")
(C category 2)
(C tag translatable)
(p actor actor_id)
(p name string)

(cmd 321 "Change Actor" ($ " " ]0 actor_id) " Class" ($ " to " ]1 class_id))
(d "Change a party member's class.")
(C category 2)
(p actor actor_id)
(p class class_id)

(cmd 322 "Change Actor" ($ " " ]0 actor_id) " Sprites")
(d "Change the sprites used by a party member.")
(C category 2)
(p actor actor_id)
(p charName f_char_name)
(C spritesheet 1 Characters/)
(p charIdx int)
(p faceName f_face_name)
(C spritesheet 3 Faces/)
(p faceIdx int)

(cmd 323 "Change Vehicle" ($ " " ]0 vehicle_id) " Graphic")
(d "Change the sprites used by a vehicle.")
(p vehicle vehicle_id)
(p charName f_char_name)
(C spritesheet 1 Characters/)
(p charIdx int)

(cmd 324 "Change" ($ " " ]0 actor_id) " Nickname" ($ " to " ]1))
(d "Change the nickname of a party member. Why they have nicknames, I do not know...")
(C tag translatable)
(p actor actor_id)
(p nickname string)

(cmd 331 "Enemy" ($ " " ]0 iterate_enemy #t) " HP +=" ($ " " : operate_value_1))
(d "Heal or harm an enemy.")
(C category 4)
(p enemy iterate_enemy)
(p negateMod int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_100)
(p canKill boolean)

(cmd 332 "Enemy" ($ " " ]0 iterate_enemy #t) " MP +=" ($ " " : operate_value_1))
(d "Heal or harm an enemy's magical abilities.")
(C category 4)
(p enemy iterate_enemy)
(p negateMod int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_10)

; 333 Add/Remove Enemy State 334 recovery, -> RCOM/CommonCommands

(cmd 335 "Enemy" ($ " " ]0 iterate_enemy #t) " appears")
(d "Make a previously hidden enemy become unhidden.")
(C category 4)
(p enemy iterate_enemy)

(cmd 336 "Transform Enemy" ($ " " ]0 iterate_enemy #t) ($ " into " ]1 enemy_id #t))
(d "Transforms an enemy into another enemy. TODO: Side effects?")
(C category 4)
(p enemy iterate_enemy)
(p transform enemy_id)

(cmd 337 "Enemy" ($ " " ]0 iterate_enemy #t) " uses animation" ($ " " ]1 animation_id #t))
(d "Play an animation on the enemy, presumably before something bad happens to it or the player.")
(C category 4)
(p enemy iterate_enemy)
(p animation animation_id)

(cmd 339 "Force Battle Action")
(d "Force something in the battle to do something. Not very specific.")
(C category 4)
(p userIsEnemy int_boolean)
(P 0 userEnemy iterate_enemy)
(v 0 userActor iterate_actor_id)
(p skill skill_id)
(p target force_action_target)

; Screen changes (340,351,352,353,354) and Ruby (355,655) -> RCOM/CommonCommands

; Completion Notes: COMPLETE (I think)