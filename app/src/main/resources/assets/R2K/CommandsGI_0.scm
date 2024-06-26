
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; The R2k Command List.
; To my knowledge, a complete list for GI-0 (but need to go through this again)

; AS THIS IS R2K, THE p _ string PARAMETER (or something like it for stuff that does have text) MUST ALWAYS EXIST!
; Even for X-codes!
; Yes, the system *should* recover via LcfEventCommandEditor_Most if you screw up.
; You shouldn't be relying on it.

(C digitCount 5)

(cmd 0 ">> insert point")
(C category 5)
(d "A placeholder that allows for insertion.")
(p _ string)
(L list)
(L strict)

(cmd 10 ">> E.O.B. insert point")
(C category 5)
(d "A placeholder that allows for insertion.")
(p _ string)
(L block)
(L strict)

(cmd 12410 "//*" ($ " " ]0))
(C category 5)
(d "A comment.")
(p text string)
(C groupBehavior messagebox 22410)

(cmd 22410 "//." ($ " " ]0))
(C category 5)
(d "A continuation of a comment.")
(p text string)

(cmd 10110 "Say" ($ " " ]0))
(C category 0)
(d "Begins a message to show to the player.")
(C tag translatable sayCmd)
(C textArg 0)
(p text textbox_string)
(C groupBehavior messagebox 20110)

(cmd 20110 "Say (cont.)" ($ " " ]0))
(C category 0)
(d "Continues a 10110 'Say' - should only be used from there.")
(C tag translatable sayCmd)
(C commandSiteAllowed false)
(C textArg 0)
(p text textbox_string)

; Need to automate this later.

(cmd 10140 ("Show Choices" (? ]1 (" " (if-eq ]1 0 ("no cancel") ("cancel choice: " (@ ]1 choice_begin_magic)))) (" (20140/20141 follows)"))))
(C category 0)
(d "Shows a set of choices to the player. (editorChoices is for editor display purposes only.)")
(p editorChoices string)
(p "0|cancelIndex+1" choice_begin_magic)
(C groupBehavior form 20140 Choice 20141)
(I 1)

(cmd 20140 ("... On Choice:" (? ]1 (" " (if-eq ]1 4 ("Cancel") ((@ ]0))) " (index " (@ ]1 choice_index_magic_2 #t) ")"))))
(C category 0)
(C tag translatable)
(d "A choice. If the player selects this choice, the code between this and the next choice runs. If the text is blank, this is a 'cancel' choice (and should be last)")
(p text string)
(p choiceIndex choice_index_magic)
(C groupBehavior r2k_choice)
(C groupBehavior expectHead 10140 20140)
(K 10140)
(i \-1)
(I 1)

(cmd 20141 "End Choices")
(C category 0)
(d "The end of a set of choices.")
(p _ string)
(C groupBehavior expectHead 10140 20140)
(i \-1)
(l)

(cmd 10150 "Input Number" ($ " to " ]2 var_id_encased) (? ]1 (" (" (@ ]1) " digits)")))
(C category 0)
(d "Shows a numeric input to the player, with a set amount of digits, and stores the result in a variable.")
(p _ string)
(p digits int)
(p var var_id)

(cmd 10120 ("Message Options" (? ]4 (" " (if-eq ]1 0 ("opaque") ("transparent")) "," (@ ]2 message_options_position) "," (if-eq ]3 0 ("fixed") ("unfixed")) ",events" (if-eq ]4 0 (" don't")) " continue"))))
(C category 0)
(d "Sets if the text box is transparent, where it is, if it will move to avoid hiding the player, and if events can move about (I think).")
(p _ string)
(p transparent int_boolean)
(p position message_options_position)
(p positionLocked int_boolean)
(p continueEvents int_boolean)

(cmd 10130 ("Change Face" (? ]3 (" to " (@ ]0) ":" (@ ]1) ", " (if-eq ]2 0 ("left") ("right")) " " (if-eq ]3 0 () (", flipped"))))))
(C category 0)
(d "Change the face in the text box to a given face, including some details.")
(p faceImage f_faceset_name)
(C spritesheet 0 FaceSet/)
(p faceIndex int)
(p faceAtRight int_boolean)
(p faceFlipped int_boolean)

; Control Switches follows. Uhoh.

(cmd 10210 ("Control Switch" (? ]4 ((if-eq ]1 1 ("es " (@ ]2 switch_id #t) " .. " (@ ]3 switch_id #t)) ((if-eq ]1 2 (" Var(" (@ ]2 var_id #t) ")") (" " (@ ]2 switch_id #t))))) " = " (@ ]4 control_switches_change_mode)) ("es"))))
(C category 1)
(d "Control switches in various ways. Single alters one switch. Range alters a group of switches. Indirect alters one switch, numbered by a variable.")
(p _ string)
(p type control_switches_type)
(P 1 target? int)
(v 0 target switch_id)
(v 1 rangeFirst switch_id)
(v 2 targIndVar var_id)
(P 1 _ int)
(v 1 rangeLast switch_id)
(p changeMode control_switches_change_mode)

(cmd 10220 "Set " (? ]0 (@ : set_variables_parameters) "Variables"))
(C category 1)
(d "Modify a variable or range of variables in various ways dependent on a given value.")
(p _ string)
(X set_variables_parameters)

(cmd 10230 "Timer Operation")
(C category 1)
(d "Alters the settings on either timer ID 0 (if unspecified) or a given timer. Can set the timer, change its visibility + if it counts in battle, or stop it.")
(p _ string)
(X timer_operation_parameters)

(cmd 10310 ((? ]1 ((if-eq ]1 0 ("Give") ("Take From"))) ("Give/Take From")) " Player:" (? ]3 (" " (if-eq ]2 0 ((@ ]3 int)) ("Var(" (@ ]3 var_id #t) ")")))) " Gold"))
(C category 2)
(d "Adds or removes gold by a constant amount or the value in a variable.")
(p _ string)
(p takeGold int_boolean)
(p goldIsVar int_boolean)
(P 2 goldVar var_id)
(v 0 gold int_default_100)

(cmd 10320 ((? ]5 ((if-eq ]1 0 ("Give") ("Take From"))) ("Give/Take From")) " Player:" (? ]5 (" " (if-eq ]4 0 ((@ ]5 int #t)) ("Var(" (@ ]5 var_id #t) ")")) " Of")) " Item" (? ]5 (" " (if-eq ]2 0 ((@ ]3 item_id #t)) ("Var(" (@ ]3 var_id #t) ")"))))))
(C category 2)
(d "Adds or removes the amount of an item (selected by ID or the value in a variable) by a constant amount or the value in a variable.")
(p _ string)
(p remove int_boolean)
(p itemIsVar int_boolean)
(P 2 itemVar var_id)
(v 0 item item_id)
(p countIsVar int_boolean)
(P 4 countVar var_id)
(v 0 count int_default_1)

(cmd 10330 "Change Party Member")
(C category 2)
(d "Adds or removes a party member, selected by ID or the value in a variable.")
(p _ string)
(p remove int_boolean)
(p actorIsVar int_boolean)
(P 2 actorVar var_id)
(v 0 actor actor_id)

(cmd 10410 "Change EXP")
(C category 2)
(d "Modifies the amount of experience a given set of party members has.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p removeExp int_boolean)
(p expIsVar int_boolean)
(P 4 expVar var_id)
(v 0 exp int_default_100)

(p showLvlUp int_boolean)

(cmd 10420 "Change Exp.Level")
(C category 2)
(d "Modifies the levels a given set of party members have.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p removeLvl int_boolean)
(p lvlIsVar int_boolean)
(P 4 lvlVar var_id)
(v 0 lvl int_default_1)

(p showLvlUp int_boolean)

(cmd 10430 "Change Battle Parameters")
(C category 2)
(d "Modifies the parameters a given set of party members have.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p removePL int_boolean)
(p targetParam change_battle_parameter)
(p plIsVar int_boolean)
(P 5 plVar var_id)
(v 0 pl int_default_10)

(cmd 10440 "Change Skills")
(C category 2)
(d "Adds or removes skills a given set of party members have.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p removeSkill int_boolean)
(p skillIsVar int_boolean)
(P 4 skillVar var_id)
(v 0 skill skill_id)

(cmd 10450 "Change Equipment")
(C category 2)
(d "Equips a given piece of equipment to, or unequips all equipment from a given set of party members")
(p _ string)
(X change_equipment_parameters)

(cmd 10460 "Change HP")
(C category 2)
(d "Heals or hurts a given set of party members. Can trigger Game Over if allowed to.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

; would be 'negate' but this is more obvious
(p hurtTarget int_boolean)
(p amountIsVar int_boolean)
(P 4 amountVar var_id)
(v 0 amount int_default_100)

(p allowDeath int_boolean)

(cmd 10470 "Change SP")
(C category 2)
(d "Gives or takes SP from a given set of party members.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p takeSp int_boolean)
(p amountIsVar int_boolean)
(P 4 amountVar var_id)
(v 0 amount int_default_10)

(cmd 10480 "Add/Remove State")
(C category 2)
(d "Adds to or removes a State from a given set of party members.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p remove int_boolean)
(p state state_id)

(cmd 10490 "Full Heal + Recharge")
(C category 2)
(d "Fills HP and SP to maximum possible for a given set of party members.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(cmd 10500 "Simulate Attack")
(C category 4)
(d "Causes an attack of damage atk - (((actor.def * defMod) / 400) + ((actor.spi * spiMod) / 800)), with the result being varied by *up to, in either direction* (variance * 5) / 100.")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p atk int)
(p defMod int)
(p spiMod int)
(p variance int)
(p storeResultDamage int_boolean)
(p resultDamageVar var_id)

(cmd 11410 ("Wait " (? ]1 ((? ]2 ((if-eq ]1 0 ("For Action") ("For " (@ ]1) "/10 Seconds"))) ("For " (@ ]1) "/10 Seconds"))) ("For..."))))
(d "Wait for some amount of time. If 0, waits a single frame, which may be useful for more precise timing. On 2k3, this can be set to wait until the ACT key is pressed.")
(p _ string)
(X wait_illogical_parameters)

(cmd 11510 "Play BGM" ($ " " ]0))
(d "Plays some background music.")
(p bgm f_music_name)
(p fadeTime int)
(p volume int_default_100)
(p tempo int_default_100)
(p balance int_default_50)
(x play_bgm_parameters)

(cmd 11520 "Fade Out BGM")
(d "Fades out the background music.")
(p _ string)
(p fadeTime int)

; note: this, and its schema, is shared between MC/35 and EC/11550

(cmd 11550 "Play Sound" ($ " " ]0))
(d "Plays a sound effect.")
(p sound f_sound_name)
(p volume int_default_100)
(p tempo int_default_100)
(p balance int_default_50)
(x play_sound_parameters)

(cmd 12310 "End Event Processing")
(d "Jump to the end of the code, thus, stop running it.")
(p _ string)

; The comments you were expecting have been moved up.

(cmd 12420 "Game Over")
(d "Show the game over screen, and such.")
(p _ string)

(cmd 10610 ((? ]1 ("Change " (@ ]1 actor_id #t) "'s Name: " (@ ]0)) ("Change Actor Name"))))
(C category 2)
(C tag translatable)
(d "Change the name of a party member.")
(p name string)
(p actor actor_id)

(cmd 10620 ((? ]1 ("Change " (@ ]1 actor_id #t) "'s Title: " (@ ]0)) ("Change Actor Title"))))
(C category 2)
(C tag translatable)
(d "Change the title of a party member.")
(p title string)
(p actor actor_id)

(cmd 10630 "Change Actor G/I/T")
(C category 2)
(d "Change how a party member appears in battle.")
(p graphic f_charset_name)
(p actor actor_id)
(C spritesheet 0 CharSet/)
(p index int)
(p transparent int_boolean)

(cmd 10640 ((? ]1 ("Change " (@ ]1 actor_id #t) "'s Face: " (@ ]0) ":" (@ ]2)) ("Change Actor Face"))))
(C category 2)
(d "Change the face of a party member.")
(p faceset f_faceset_name)
(p actor actor_id)
(C spritesheet 0 FaceSet/)
(p index int)

(cmd 10650 "Change Vehicle Appearance")
(C category 3)
(d "Change the appearance of a vehicle.")
(p graphic f_charset_name)
(p vehicle scripting_vehicletype)
(C spritesheet 0 CharSet/)
(p index int)

(cmd 10660 "Change System BGM")
(d "Change a System BGM.")
(p bgm f_music_name)
(p bgmId change_sys_bgm_id)
(p fadeTime int)
(p volume int_default_100)
(p tempo int_default_100)
(p balance int_default_50)
(x change_system_bgm_parameters)

; NT. OS uses below for hax, so it gets priority on the "should we name this right today" roulette wheel

(cmd 10670 ("Change Sys.SFX" (? ]4 (" " (@ ]1 change_sys_sfx_id #t) " to " (@ ]0)))))
(d "Change a sound effect used by the system.")
(p sfx f_sound_name)
(p sfxId change_sys_sfx_id)
(p volume int_default_100)
(p tempo int_default_100)
(p balance int_default_50)
(x change_system_sfx_parameters)

(cmd 10680 "Change Sys.GFX")
(d "Change the current set of graphics used by the system.")
(p systemGfx f_system_name)
(p useTiling int_boolean)
(p font change_sys_gfx_font)

(cmd 10690 "Change Transition")
(d "Change a transition used by the system.")
(p _ string)
(p situation change_transition_situation)
(p transition change_transition_type)

(cmd 10820 "Store Player Location")
(d "Stores the player's location.")
(p _ string)
(p mapVar var_id)
(p xVar var_id)
(p yVar var_id)

(cmd 10850 "Teleport Vehicle")
(C category 3)
(d "Teleports a vehicle.")
(p _ string)
(X teleport_vehicle_parameters)

(cmd 10860 "Teleport Event")
(C category 3)
(d "Teleports an event. (Can set direction on 2k3.)")
(p _ string)
(X teleport_event_parameters)

(cmd 10870 "Swap Events")
(C category 3)
(d "Swaps the positions of two events / the event and the player.")
(p _ string)
(p eventA character_id)
(p eventB character_id)

(cmd 10910 "Store Terrain Tag")
(C category 3)
(d "Stores the terrain tag of a given point on the map. These come from Tileset/@terrain_id_data.")
(p _ string)
(p useVars int_boolean)
(P 1 xVar var_id)
(v 0 x int)
(P 1 yVar var_id)
(v 0 y int)
(p resultVar var_id)

(cmd 10920 "Store Event ID")
(C category 3)
(d "Stores the event ID of whatever event is on a given point on the map, or 0 if none exists there.")
(p _ string)
(p useVars int_boolean)
(P 1 xVar var_id)
(v 0 x int)
(P 1 yVar var_id)
(v 0 y int)
(p resultVar var_id)

(cmd 11010 "Hide Map")
(C category 3)
(d "Hides the map.")
(p _ string)
(p transition transition_type)

(cmd 11020 "Show Map")
(C category 3)
(d "Shows the map.")
(p _ string)
(p transition transition_type)

(cmd 11030 "Tint Screen")
(d "Tints the screen with given parameters.")
(p _ string)
(p red% percent)
(p green% percent)
(p blue% percent)
(C r2kTonePicker 1 2 3 4)
(p saturation% percent)
(p fadeSecs/10 int)
(p waitForFade int_boolean)

(cmd 11040 "Flash Screen")
(d "Flashes the screen with a temporary tint. Please ensure the correct warnings are attached when using this.")
(p _ string)
(X flash_screen_parameters)

(cmd 11050 "Shake Screen")
(d "Shakes the screen.")
(p _ string)
(X shake_screen_parameters)

(cmd 11070 "Weather Control")
(C category 3)
(d "Controls the weather. Your forecast today: Rain. Which is good, because the clouds will protect you from the burning, uncaring sun.")
(p _ string)
(p weather weather_control_type)
(p strength weather_control_strength)

(cmd 11310 ((? ]1 ((if-eq ]1 1 ("Show Player") ("Hide Player"))) ("Change Player Visibility"))))
(C category 3)
(d "Show or hide the player on the map.")
(p _ string)
(p visible int_boolean)

(cmd 11330 ("Move Event" (? ]1 (" " (@ ]1 character_id #t)))))
(C category 3)
(d "Sends a list of MoveCommands to an event.")
(p _ string)
(X move_event_parameters)

(cmd 11530 "Copy Current BGM")
(d "Copies the current BGM for future use.")
(p _ string)

(cmd 11540 "Use Copied BGM")
(d "Uses a previously-made BGM copy.")
(p _ string)

(cmd 11610 "Key Input Information")
(d "Gets (and possibly waits for) a keypress, returning the result in a variable. Has a gazillion options varying by version.")
(p _ string)
; Injects a help window, does opt.param stuff
(X key_input_information_parameters)

(cmd 11710 "Set Map Tileset")
(C category 3)
(d "Sets the map's tileset. Hopefully you've designed the map and tilesets with this in mind.")
(p _ string)
(p tilesetId tileset_id)

(cmd 11720 "Change Parallax Setup")
(C category 3)
(d "Change the parallax on the map.")
(p parallax f_parallax_name)
(p scrollH int_boolean)
(p scrollV int_boolean)
(p scrollHAuto int_boolean)
(p scrollHSpeed int)
(p scrollVAuto int_boolean)
(p scrollVSpeed int)

(cmd 11740 "Change Encounter Rate")
(C category 3)
(d "Change the encounter rate on the map.")
(p _ string)
(p newRate int)

(cmd 11750 "Substitute Tiles")
(C category 3)
(d "Change all tiles of one ID for another. Note that these IDs are *relative* to the tile plane. That is, 5000 for the Lower Layer, 10000 for the Upper Layer.")
(p _ string)
(p upperLayer int_boolean)
(p tileA int)
(p tileB int)

(cmd 11810 "Add/Remove Teleport Target")
(C category 2)
(d "Adds a teleport target to, or removes a teleport target from, the menu.")
(p _ string)
(X ar_teleport_target_parameters)

(cmd 11820 "Enable/Disable Teleport")
(C category 2)
(d "Enable or disable the teleportation menu.")
(p _ string)
(p enable int_boolean)

(cmd 11830 "Set Escape Target")
(C category 2)
(d "Set the target position to escape to for a given map.")
(p _ string)
(p map map_id)
(p x int)
(p y int)
(p useSwitch int_boolean)
(P 4 switch switch_id)
(v 0 _ int)

(cmd 11840 ((? ]1 ((if-eq ]1 0 ("Disable") ("Enable"))) ("Enable/Disable")) " Escape"))
(d "Enables or disables escaping.")
(p _ string)
(p enable int_boolean)

(cmd 11930 ((? ]1 ((if-eq ]1 0 ("Disable") ("Enable"))) ("Enable/Disable")) " Save"))
(d "Enables or disables saving.")
(p _ string)
(p enable int_boolean)

(cmd 11960 ((? ]1 ((if-eq ]1 0 ("Disable") ("Enable"))) ("Enable/Disable")) " Main Menu"))
(d "Enables or disables the ability to return to the main menu.")
(p _ string)
(p enable int_boolean)

(cmd 12010 ((? ]1 ((@ : cbranch_parameters)) ("Conditional Branch"))))
(C category 1)
(d "Runs the following code if a condition is true. Otherwise, runs the code at the Else Branch inside, or if one does not exist, goes to the end of the branch.")
(p _ string)
(C groupBehavior form 22010 Else 22011)
(X cbranch_parameters)
(I 1)

(cmd 22010 "Else Branch")
(C category 1)
(d "Indicates that if the containing branch was false, the following code should be executed. (Otherwise, the previous code will be executed, and this will not.)")
(p _ string)
(C groupBehavior expectHead 12010)
(i \-1)
(I 1)
(l)

(cmd 22011 "End Branch")
(C category 1)
(d "Indicates the end of a conditional branch.")
(p _ string)
(C groupBehavior expectHead 22010 12010)
(i \-1)
(l)

(cmd 12110 "Label" ($ " " ]1))
(C category 1)
(d "A label. See Jump To Label.")
(p _ string)
(p labelId int)

(cmd 12120 "Jump To Label" ($ " " ]1))
(C category 1)
(d "Jump to a label.")
(p _ string)
(p labelId int)

(cmd 12210 "Start Loop")
(C category 1)
(d "Begins a loop, which will run forever unless broken.")
(p _ string)
(C groupBehavior form 22210)
(I 1)

(cmd 12220 "Break Loop")
(C category 1)
(d "Leaves the loop.")
(p _ string)

(cmd 22210 "End Loop")
(C category 1)
(d "Indicates the end of a loop - will go back to the start.")
(p _ string)
(C groupBehavior expectHead 12210)
(i \-1)
(l)

(cmd 12320 "Temp. Erase This Event")
(C category 3)
(d "Attempts to temporarily erase this event (if this is, in fact, a map event)")
(p _ string)

(cmd 12330 (? ]1 (@ : call_event_parameters) "Call Event"))
(C category 1)
(d "Calls a page in another event, or calls a common event.")
(p _ string)
(p callType callevent_type)
(P 1 unk. int)

(v 0 commonEv commonevent_id)
(v 1 char character_id)
(v 2 charVar var_id)

(P 1 unk. int)

(v 0 _ int)
(v 1 page int)
(v 2 pageVar var_id)

(cmd 12510 "Return To Title")
(d "Returns to the title screen.")
(p _ string)

(cmd 1008 "Change Party Member Class (2K3 Only)")
(C category 2)
(d "Changes a party member's class. (EasyRPG only supports Single Actor at time of writing - the GetActors usage is only a suspicion)")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p class class_id)
(p downToLevel1 int_boolean)
(p skillEffect change_actor_class_skillfx)
(p statsEffect change_actor_class_statsfx)
(p showClassChange int_boolean)

(cmd 1009 "Add/Remove Party Member Battle Command (2K3 Only)")
(C category 2)
(d "Adds or removes a battle command from a party member. (EasyRPG only supports Single Actor at time of writing - the GetActors usage is only a suspicion)")
(p _ string)
(p targetMode get_actors_mode)
(P 1 _ int)
(v 1 target actor_id)
(v 2 targetVar var_id)

(p command battlecommand_id)
(p add int_boolean)

(cmd 5002 "Shutdown RPG_RT (2K3-E Only)")
(d "Goodnight.")
(p _ string)

(cmd 5004 "Toggle Fullscreen (2K3-E Only)")
(d "Allows you to fry the player's graphics card.")
(p _ string)
