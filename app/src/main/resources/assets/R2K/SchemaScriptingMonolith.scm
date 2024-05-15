
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; ___
; | |
; | |
; --- 1
; the first monolith

(vm
	(define-name call_event_parameters (= ]1
		(@ ]1 callevent_type)
		(0 "Call Common Event " (@ ]2 commonevent_id #t))
		(1 "Call Map Event " (@ ]2 character_id #t) ", Page " (@ ]3))
		(2 "Call Map Event " (@ ]2 var_id_encased) ", Page " (@ ]3 var_id_encased))
	))
)

;  __
;    | |
; --   V

; Set Variables

(e set_variables_target 0 single 1 range 2 indirect)
(e set_variables_op 0 "=" 1 "+=" 2 "-=" 3 "*=" 4 "/=" 5 "%=")
(e set_variables_source 0 constant 1 variable 2 indirectVar 3 random 4 item 5 actor 6 evOrPlayer 7 gameInfo 8 battleEnemy)
(e set_variables_itemprop 0 amount 1 equippedAmount)
(e set_variables_actorprop 0 lvl 1 exp 2 hp 3 sp 4 maxHp 5 maxSp 6 atk 7 def 8 spi 9 agi 10 weaponId 11 shieldId 12 armourId 13 helmetId 14 accessoryId)
(e set_variables_charprop 0 mapID 1 x 2 y 3 direction 4 screenX 5 screenY)
(e set_variables_giprop 0 gold 1 timer1 2 actorsInParty 3 saveCount 4 battleCount 5 winCount 6 defeatCount 7 escapeCount 8 midiPosition 9 timer2)
(e set_variables_enemyprop 0 hp 1 sp 2 maxHp 3 maxSp 4 atk 5 def 6 spi 7 agi)

(. set_variables_base_targ)
(] 2 varTarg var_id)

(. set_variables_base_targ_r)
(] 2 firstTarg var_id)
(] 3 lastTarg var_id)

(. set_variables_base_targ_i)
(] 2 ptrVarTarg var_id)

(. set_variables_base)
(vm
	(define-name set_variables_base (= ]1 ""
		(0 (@ ]2 var_id_encased))
		(1 "Vars[" (@ ]2 var_id #t) " .. " (@ ]3 var_id #t) "]")
		(2 (@ ]2 var_id_indirect))
	) " " (@ ]4 set_variables_op))
)
(] 1 targetType set_variables_target)
(+ DA{ ]1 set_variables_base_targ 1 set_variables_base_targ_r 2 set_variables_base_targ_i })
(] 4 operation set_variables_op)
(] 5 source set_variables_source)

(. set_variables_int)
(vm
	(define-name set_variables_int (@ ]6))
)
(+ set_variables_base)
(] 6 value int)

(. set_variables_var)
(vm
	(define-name set_variables_var (@ ]6 var_id_encased))
)
(+ set_variables_base)
(] 6 var var_id)

(. set_variables_indirect)
(vm
	(define-name set_variables_indirect (@ ]6 var_id_indirect))
)
(+ set_variables_base)
(] 6 ptrVar var_id)

(. set_variables_random)
(vm
	(define-name set_variables_random
		"Random (" (@ ]6) " to " (@ ]7) ")"
	)
)
(+ set_variables_base)
(] 6 low int)
(] 7 high int)

(. set_variables_item)
(vm
	(define-name set_variables_item
		"Item " (@ ]6 item_id #t) "." (@ ]7 set_variables_itemprop)
	)
)
(+ set_variables_base)
(] 6 item item_id)
(] 7 property set_variables_itemprop)

(. set_variables_actor)
(vm
	(define-name set_variables_actor
		"Actor " (@ ]6 actor_id #t) "." (@ ]7 set_variables_actorprop)
	)
)
(+ set_variables_base)
(] 6 actor actor_id)
(] 7 property set_variables_actorprop)

(. set_variables_event)
(vm
	(define-name set_variables_event
		"Actor " (@ ]6 character_id #t) "." (@ ]7 set_variables_charprop)
	)
)
(+ set_variables_base)
(] 6 char character_id)
(] 7 property set_variables_charprop)

(. set_variables_gameinfo)
(vm
	(define-name set_variables_gameinfo
		"GameInfo: " (@ ]6 set_variables_giprop)
	)
)
(+ set_variables_base)
(] 6 property set_variables_giprop)

(. set_variables_battle)
(vm
	(define-name set_variables_battle
		"EnemyIdx: " (@ ]6) " -> " (@ ]7 set_variables_enemyprop)
	)
)
(+ set_variables_base)
(] 6 enemy int)
(] 7 property set_variables_enemyprop)

(. set_variables_parameters)
(vm
	(define-name set_variables_parameters
		(@ : set_variables_base) " " (= ]5 ""
			(0 (@ : set_variables_int))
			(1 (@ : set_variables_var))
			(2 (@ : set_variables_indirect))
			(3 (@ : set_variables_random))
			(4 (@ : set_variables_item))
			(5 (@ : set_variables_actor))
			(6 (@ : set_variables_event))
			(7 (@ : set_variables_gameinfo))
			(8 (@ : set_variables_battle))
		)
	)
)
(@ indent indent)
(@ parameters DA{ ]5 set_variables_base 0 set_variables_int 1 set_variables_var 2 set_variables_indirect 3 set_variables_random 4 set_variables_item 5 set_variables_actor 6 set_variables_event 7 set_variables_gameinfo 8 set_variables_battle })

; ___ ___
; |   | /
; --- |_

; Conditional Branch

(e cbranch_type 0 switch 1 varCompare 2 timer1 3 goldCompare 4 itemCompare 5 actorInfo 6 charDir 7 usingVehicle 8 initByActKey 9 bgmLooped 10 timer2 11 rpgMaker2003v1.11.Cherry.other)
(e cbranch_gele 0 >= 1 <=)
; borrowed by battle varcomp
(e cbranch_varcomp_e 0 == 1 >= 2 <= 3 > 4 < 5 !=)
(e cbranch_cherry_e 0 hasSave 1 testPlay 2 atbWait 3 fullScreen)
(e cbranch_itemcomp_e 0 partyHasItem 1 partyDoesntHaveItem)
(e cbranch_actorprop_e 0 inParty 1 name= 2 level>= 3 hp>= 4 knowsSkill 5 hasEquipped 6 hasState)

(. cbranch_base)
(vm
	(define-name cbranch_base (@ ]1 cbranch_type))
)
(] 0 _ string)
(] 1 type cbranch_type)

(. cbranch_switch)
(vm
	(define-name cbranch_switch "Switch " (@ ]2 switch_id #t) " " (@ ]3 int_boolean_switch_not))
)
(+ cbranch_base)
(] 2 switch switch_id)
(] 3 invert int_boolean)

(> cbranch_varcomp DA{ ]3 cbranch_varcomp_var 0 cbranch_varcomp_raw })
(vm
	(define-name cbranch_varcomp
		(@ ]2 var_id_encased) " "
		(@ ]5 cbranch_varcomp_e #f) " "
		(if-ne ]3 0 (@ ]4 var_id_encased) (@ ]4))
	)
)

(. cbranch_varcomp_var)
(+ cbranch_base)
(] 2 varA var_id)
(] 5 comparison cbranch_varcomp_e)
(] 3 bIsVar int_boolean)
(] 4 varB var_id)

(. cbranch_varcomp_raw)
(+ cbranch_base)
(] 2 varA var_id)
(] 5 comparison cbranch_varcomp_e)
(] 3 bIsVar int_boolean)
(] 4 valB int)

(. cbranch_timer1)
(vm
	(define-name cbranch_timer1 "Timer1 " (@ ]3 cbranch_gele) " " (@ ]2) " Seconds")
)
(+ cbranch_base)
(] 3 comparison cbranch_gele)
(] 2 valueSeconds int)

(. cbranch_goldcomp)
(vm
	(define-name cbranch_goldcomp "Gold " (@ ]3 cbranch_gele) " " (@ ]2))
)
(+ cbranch_base)
(] 3 comparison cbranch_gele)
(] 2 valueGold int)

(. cbranch_itemcomp)
(vm
	(define-name cbranch_itemcomp (@ ]3 cbranch_itemcomp_e) " " (@ ]2 item_id #t))
)
(+ cbranch_base)
(] 3 comparison cbranch_itemcomp_e)
(] 2 item item_id)

(. cbranch_actorprop_base)
(vm
	(define-name cbranch_actorprop "Actor (...)")
)
(+ cbranch_base)
(] 2 actor actor_id)
(] 3 actorProp cbranch_actorprop_e)

(. cbranch_actorprop_name)
(+ cbranch_actorprop_base)
(] 0 name string)

(. cbranch_actorprop_int)
(+ cbranch_actorprop_base)
(] 4 value int)

(. cbranch_actorprop_skill)
(+ cbranch_actorprop_base)
(] 4 value skill_id)

(. cbranch_actorprop_item)
(+ cbranch_actorprop_base)
(] 4 value item_id)

(. cbranch_actorprop_state)
(+ cbranch_actorprop_base)
(] 4 value state_id)


(> cbranch_actorprop DA{ ]3 cbranch_actorprop_base 1 cbranch_actorprop_name 2 cbranch_actorprop_int 3 cbranch_actorprop_int 4 cbranch_actorprop_skill 5 cbranch_actorprop_item 6 cbranch_actorprop_state })

(. cbranch_chardir)
(vm
	(define-name cbranch_chardir "Char.Dir. (...)")
)
(+ cbranch_base)
(] 2 char character_id)
(] 3 direction sprite_direction)

(. cbranch_vehicle)
(vm
	(define-name cbranch_vehicle "Using: " (@ ]2 scripting_vehicletype))
)
(+ cbranch_base)
(] 2 vehicle scripting_vehicletype)

(. cbranch_timer2)
(vm
	(define-name cbranch_timer2 "Timer2 " (@ ]3 cbranch_gele) " " (@ ]2) " Seconds")
)
(+ cbranch_base)
(] 3 comparison cbranch_gele)
(] 2 valueSeconds int)

(. cbranch_cherry)
(vm
	(define-name cbranch_cherry "Cherry2K3-E: " (@ ]2 cbranch_cherry_e))
)
(+ cbranch_base)
(] 2 property cbranch_cherry_e)

(. cbranch_parameters)
(vm
	(define-name cbranch_parameters
		"Conditional: " (= ]1 ""
			(0 (@ : cbranch_switch))
			(1 (@ : cbranch_varcomp))
			(2 (@ : cbranch_timer1))
			(3 (@ : cbranch_goldcomp))
			(4 (@ : cbranch_itemcomp))
			(5 (@ : cbranch_actorprop))
			(6 (@ : cbranch_chardir))
			(7 (@ : cbranch_vehicle))
			(8 (@ : cbranch_base))
			(9 (@ : cbranch_base))
			(10 (@ : cbranch_timer2))
			(11 (@ : cbranch_cherry))
		)
	)
)
(@ indent indent)
(@ parameters DA{ ]1 cbranch_base 0 cbranch_switch 1 cbranch_varcomp 2 cbranch_timer1 3 cbranch_goldcomp 4 cbranch_itemcomp 5 cbranch_actorprop 6 cbranch_chardir 7 cbranch_vehicle 8 cbranch_base 9 cbranch_base 10 cbranch_timer2 11 cbranch_cherry })

; --- /-
;  |  | |
;  |  _/

; Timer Options
; This can be 4 or 5 array elements in size. *sigh*
; Not visible: the additional SDB/AESE hackery required to make this happen

(e timer_operation_type 0 setTime 1 setFlagsAndStart 2 stop)

(. timer_operation_parameters)
(@ indent indent)
(@ parameters DA{ ]1 timer_operation_base 0 timer_operation_set 1 timer_operation_start })

(. timer_operation_base)
(] 0 _ string)
(+ ]?6 timerId Timer1 timer_id)
(] 1 operation timer_operation_type)

(. timer_operation_set)
(+ timer_operation_base)
(] 2 secondsIsVar int_boolean)
(+ DA{ ]2 timer_operation_set_var 0 timer_operation_set_int })

(. timer_operation_set_var)
(] 3 secondsVar var_id)

(. timer_operation_set_int)
(] 3 secondsVal int)

(. timer_operation_start)
(+ timer_operation_base)
(] 4 visible int_boolean)
(] 5 battle int_boolean)

;     __
; ___ |_
; ||| |_

; Move Event
; This one involves some hackery in the EventCommand decoder,
;  to give the schema something useful.
; Rules are:
; If, and only if, the code is 11330, moveCommands is considered.
; Otherwise it basically doesn't exist.

(. move_event_parameters)
(@ indent indent)
(@ parameters move_event_array)
; This bit really really relies on schema stuff doing it's job.
; Ok, so what's going on here is that @move_commands is completely ignored in MOST CASES.
; The exception is THIS ONE COMMAND.
; To facilitate verification of the added metadata, "+optP @move_commands MoveListEditor" exists in SchemaScripting / RPG::EventCommand.
; But it's not actually visible to the user because that's the checking-only version of the schema.
; And then when THIS schema is being used instead, @move_commands is actually forcefully brought into existence.
; Serialization-wise, @move_commands does not exist.
(@ move_commands MoveListEditor)

(. move_event_array)
(] 0 _ string)
(] 1 char character_id)
(] 2 moveFreq eventpage_movefreq)
(] 3 repeat int_boolean)
(] 4 skippable int_boolean)

; ||| /
; ---/--

; Wait (YES REALLY.)

; dummy enum to give user illusion of sensibility.
; in the case of framecount 0, but no button Id,
;  it will say:
;   WaitForCondition
;   doesn't exist, default: None
; in the case of framecount 0, w/ button ID:
;   WaitForCondition
;   Act
; in the case of framecount nonzero, but button ID:
;   8 frames
;   <wfcCondType doesn't show up>
; in the case of framecount nonzero, with no button ID:
;   8 frames
;   <wfcCondType doesn't show up>
; this hopefully mirrors what user is "supposed" to see,
;  without potentially breaking any rules.

(. wait_illogical_parameters)
(@ indent indent)
(@ parameters wait_array)

; This bit of code's a bit odd.
; It encounters all sorts of weirdness that has to be resolved.
; There are 4 combinations of the 2 lengths and secTenthCount that are all valid.
; Including, importantly, the combination where there's 3 elements but the third is ignored.
; R48 has to avoid messing with this, avoid getting it wrong, and maintain a sane UI,
;  so "initButton" was invented to force specific settings in juuust the right way.

; Previously, a paragraph existed here about having to use FormatSyntax because reasons.
; Now it's all Datum!

(vm
	(define (r2k-waitForAction obj) (and
		(eq? (dm-a-len obj) 3)
		(eq? (dm-dec (dm-a-ref obj 1)) 0)
	))
	(define (r2k-waitForAction-not obj) (not (r2k-waitForAction obj)))
)

(. wait_array)
(] 0 _ string)
(+ initButton "Wait For X/10 Seconds" r2k-waitForAction-not wait_array_subroutine_2)
(+ initButton "Wait For Action (2k3)" r2k-waitForAction wait_array_subroutine_3)
; This reimplements the same control logic as a disambiguator for the UI/checking,
;  which has to be distinct from the actual initializers because the initializers force stuff
;  that can't be forced safely on existing data.
(+ DA{ :length wait_array_vis_2 3 DA{ ]1 wait_array_vis_2 0 wait_array_vis_3 } })

(. wait_array_vis_2)
(] 1 secTenthCount int_default_10)

(. wait_array_vis_3)
(] 1 _ int)
(] 2 keyIndexMaybe_2k3 int)

; This 'subroutine' ensures the result is WFt...
(. wait_array_subroutine_2)
(+ hide array 2 OPAQUE)
(] 1 secTenthCount int_default_10)

; This 'subroutine' ensures the result is WFA
(. wait_array_subroutine_3)
(] 1 _ int= 0)
(] 2 keyIndexMaybe_2k3 int)

; /- +-
; |  +-
; - +-

; Change Equipment

(e change_equipment_slot_id 0 weapon 1 shield 2 armour 3 helmet 4 accessory)

(. change_equipment_parameters)
(@ indent indent)
(@ parameters DA{ ]3 change_equipment_base 0 change_equipment_add 1 change_equipment_remove })
(. change_equipment_base)
(] 0 _ string)
(] 1 targetMode get_actors_mode)
(+ DA{ ]1 ]2 _ int 1 ]2 actor actor_id 2 ]2 actorVar var_id })
(] 3 remove int_boolean)

(. change_equipment_add)
(+ change_equipment_base)
(] 4 itemIsVar int_boolean)
(+ DA{ ]4 ]5 itemVar var_id 0 ]5 item item_id })

(. change_equipment_remove)
(+ change_equipment_base)
(] 4 slot change_equipment_slot_id)

; -+- +-
;  |  +-
;  |  +-

; Teleport Event

; also used by teleport_player_2k3_escapehatch
(e teleport_event_dir 0 ignored 1 up 2 right 3 down 4 left)

(. teleport_event_parameters)
(@ indent indent)
(@ parameters DA{ ]2 teleport_event 0 teleport_event_novars })

(. teleport_event)
(] 0 _ string)
(] 1 target character_id)
(] 2 useVars int_boolean)
(] 3 xVar var_id)
(] 4 yVar var_id)

(. teleport_event_novars)
(] 0 _ string)
(] 1 target character_id)
(] 2 useVars int_boolean)
(] 3 x int)
(] 4 y int)
(+ ]?5 dir2k3 ignored teleport_event_dir)
(+ mapPositionHelper . ]3 ]4)

; -+- -+-
;  |   |
;  |   |

; Teleport Target

(. ar_teleport_target_parameters)
(@ indent indent)
(@ parameters DA{ ]1 ar_teleport_target_remove 0 ar_teleport_target_add })

(. ar_teleport_target_remove)
(] 0 _ string)
(] 1 remove int_boolean)
(] 2 map map_id)

(. ar_teleport_target_add)
(] 0 _ string)
(] 1 remove int_boolean)
(] 2 map map_id)
(] 3 x int)
(] 4 y int)
(] 5 requireSwitch int_boolean)
(+ DA{ ]5 ]6 switch switch_id 0 ]6 _ int })

; --- ---
; --  ^ .
; -   ---

; Flash Screen

(e flashshake_screen_mode 0 temporary 1 begin 2 end)

(. flash_screen_parameters)
(@ indent indent)
(@ parameters flash_screen_array)

(. flash_screen_array)
(] 0 _ string)
; See GI_M 11320 for details
(] 1 red% int= 200)
(] 2 green% int= 200)
(] 3 blue% int= 200)
(] 4 saturation% int= 100)
(+ r2kTonePicker ]1 ]2 ]3 ]4)
(] 5 timeSecs/10 int)
(] 6 wait int_boolean)
(+ ]?7 mode2k3 temporary flashshake_screen_mode)

; --- ---
; ^ . ^ .
; --- ---

; Shake Screen

(. shake_screen_parameters)
(@ indent indent)
(@ parameters shake_screen_array)

(. shake_screen_array)
(] 0 _ string)
(] 1 strength int)
(] 2 speed int)
(] 3 time int)
(] 4 wait int_boolean)
(+ ]?5 mode2k3 temporary flashshake_screen_mode)

; --- ---
; ^-. ^-.
; --- ---

; Show Shop, not to be confused with Shake Screen

(e show_shop_bstype 0 buysAndSells 1 buys 2 sells)
(e show_shop_type 0 shopA 1 shopB 2 shopC)

(. show_shop_parameters)
(@ indent indent)
(@ parameters show_shop_array)

(. show_shop_array)
(] 0 _ string)
(] 1 buySell show_shop_bstype)
(] 2 shopType show_shop_type)
(] 3 hasInnerCode int_boolean)
; Always zero in EasyRPG Test Game - 2000
(] 4 unknown int)
(+ arrayIxN 5 0 item_id)

; -+- --
;  |  |-/
;  |  |

; Teleport Player

(. teleport_player_2k3_escapehatch)
(@ indent indent)
(@ parameters teleport_player_2k3_escapehatch_array)

(. teleport_player_2k3_escapehatch_array)
(] 0 _ string)
(] 1 map map_id)
(] 2 x int)
(] 3 y int)
(+ mapPositionHelper ]1 ]2 ]3)
(+ ]?4 direction2k3 ignored teleport_event_dir)

; BE
; Begin Encounter

(e begin_encounter_battletype 0 fromMap 1 fromString 2 byTerrainId)
(e begin_encounter_battlemode 0 normal 1 initiative 2 surround 3 backAttack 4 pincerAttack)
(e begin_encounter_escape 0 refuse 1 endEvent 2 executeEscapeCode)
(e begin_encounter_defeat 0 gameOver 1 executeDefeatCode)

(. begin_encounter_parameters)
(@ indent indent)
(@ parameters DA{ ]1 begin_encounter_vars 0 begin_encounter_novars })

(. begin_encounter_vars)
(] 1 troopIsVar int_boolean)
(] 2 troopVar var_id)
(+ begin_encounter_general)

(. begin_encounter_novars)
(] 1 troopIsVar int_boolean)
(] 2 troop troop_id)
(+ begin_encounter_general)

(. begin_encounter_general)
(] 3 setupType begin_encounter_battletype)
(] 4 escapeMode begin_encounter_escape)
(] 5 defeatMode begin_encounter_defeat)
(] 6 playerAttacksFirst int_boolean)
(+ ]?7 battleMode_2k3 normal begin_encounter_battlemode)
(+ DA{ ]3 begin_encounter_0 1 begin_encounter_1 2 begin_encounter_2 })

(. begin_encounter_0)
(] 0 _ string)

(. begin_encounter_1)
(] 0 bkg f_battleback_name)
; this means nothing?!?!?!
(+ ]?8 formation_2k3 none int)

(. begin_encounter_2)
(] 0 _ string)
(] 9 terrainId terrain_id)

; TV
; teleport vehicle

(. teleport_vehicle_parameters)
(@ indent indent)
(@ parameters DA{ ]2 teleport_vehicle_vars 0 teleport_vehicle_novars })

(. teleport_vehicle_vars)
(] 0 _ string)
(] 1 vehicle scripting_vehicletype_wp)
(] 2 positionVars int_boolean)
(] 3 map var_id)
(] 4 x var_id)
(] 5 y var_id)
(+ ]?6 direction2k3 ignored teleport_event_dir)

(. teleport_vehicle_novars)
(] 0 _ string)
(] 1 vehicle scripting_vehicletype_wp)
(] 2 positionVars int_boolean)
(] 3 map map_id)
(] 4 x int)
(] 5 y int)
(+ mapPositionHelper ]3 ]4 ]5)
(+ ]?6 direction2k3 ignored teleport_event_dir)
