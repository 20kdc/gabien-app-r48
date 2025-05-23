
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Defining event command special schemas. (RVXA Version)
; Yes, this was seriously the best plan I could think of other than outright hardcoding.

; RPG Maker VX Ace Command Helpers.

; This will suck. Again.

(> int_default_1 int= 1)
(> int_default_10 int= 10)
(> int_default_50 int= 50)
(> int_default_100 int= 100)
(> int_default_200 int= 200)

(> textbox_string stringConfigLen zR48ProjectConfig@textbox_width)

(e conditional_branch_types 0 switch 1 variable 2 selfSwitch 3 timer 4 actor 5 enemy 6 ev/char 7 gold 8 item 9 weapon 10 armour 11 button 12 script 13 vehicle)

(e comparison_type 0 == 1 >= 2 <= 3 > 4 < 5 !=)

(i RCOM/CommonConditionals)

; This is going to suck (even more)

(e conditional_branch_actor_types 0 inParty 1 name= 2 class 3 skill? 4 weapon= 5 armourIdWorn? 6 hasState?)

; This is the VXA specific actor field

(. conditional_branch_actor_class)
(vm
	(define-name conditional_branch_actor_class "class " (@ ]3 class_id #t))
)
(] 3 class class_id)

(. conditional_branch_actor)
(vm
	(define-name conditional_branch_actor
		"Actor " (@ ]1 actor_id #t) " "
		(= ]2 ((@ ]2 conditional_branch_actor_types) " (...)")
			(0 "in party")
			(1 (@ : conditional_branch_actor_name))
			(2 (@ : conditional_branch_actor_class))
			(3 (@ : conditional_branch_actor_skill))
			(4 (@ : conditional_branch_actor_weapon))
			(5 (@ : conditional_branch_actor_armourIdWorn))
			(6 (@ : conditional_branch_actor_hasState))
		)
	)
)
(] 0 type conditional_branch_types)
(] 1 actor actor_id)
(] 2 subCondition conditional_branch_actor_types)
(+ DA{ ]2 conditional_branch_actor_unknown 0 conditional_branch_actor_inParty 1 conditional_branch_actor_name 2 conditional_branch_actor_class 3 conditional_branch_actor_skill 4 conditional_branch_actor_weapon 5 conditional_branch_actor_armourIdWorn 6 conditional_branch_actor_hasState })

(e cbr_gold_comparison_type 0 >= 1 <= 2 <)

(. conditional_branch_vehicle)
(vm
	(define-name conditional_branch_vehicle "In Vehicle: " (@ ]1 vehicle_id))
)
(] 0 type conditional_branch_types)
(] 1 vehicleId vehicle_id)

(. conditional_branch_parameters)
(vm
	(define-name conditional_branch_parameters
		" " (= ]0 ((@ ]0 conditional_branch_types) " (...)")
			(0 (@ : conditional_branch_switch))
			(1 (@ : conditional_branch_variable))
			(2 (@ : conditional_branch_selfSwitch))
			(3 (@ : conditional_branch_timer))
			(4 (@ : conditional_branch_actor))
			(5 (@ : conditional_branch_enemy))
			(6 (@ : conditional_branch_character))
			(7 (@ : conditional_branch_gold))
			(8 (@ : conditional_branch_item))
			(9 (@ : conditional_branch_weapon))
			(10 (@ : conditional_branch_armour))
			(11 (@ : conditional_branch_button))
			(12 (@ : conditional_branch_script))
			(13 (@ : conditional_branch_vehicle))
		)
	)
)
(@ indent indent)
(@ parameters DA{ ]0 conditional_branch_unknown 0 conditional_branch_switch 1 conditional_branch_variable 2 conditional_branch_selfSwitch 3 conditional_branch_timer 4 conditional_branch_actor 5 conditional_branch_enemy 6 conditional_branch_character 7 conditional_branch_gold 8 conditional_branch_item 9 conditional_branch_weapon 10 conditional_branch_armour 11 conditional_branch_button 12 conditional_branch_script 13 conditional_branch_vehicle })

; And Now For Even More Suck (tm)
; Set Variables. I hope I didn't make a mistake, II.

(e set_variables_source 0 int 1 var 2 random 3 gameData 4 script)

; Notably, the majority of the Set Variables command's pattern is in the Commands.txt file.
; Furthermore, CommonSV handles the int/var/random cases, and lets the parent schema control any further cases using set_variables_parameters_ext.
; It used to be that CommonSV defined this by default, but that now counts as a dodgy translation conflict.

(vm
	(define-name set_variables_parameters_ext (@ ]3 set_variables_source))
)

(i RCOM/CommonSV)

(. set_variables_script)
(+ set_variables_base)
(] 4 ruby string)

(. set_variables_parameters)
(@ indent indent)
(@ parameters DA{ ]3 set_variables_base 0 set_variables_int 1 set_variables_var 2 set_variables_random 3 set_variables_gamedata 4 set_variables_script })

; Set Variables (Game Data)

(e set_variables_gamedata_type 0 itemCount 1 weaponCount 2 armourCount 3 actorVar 4 enemyVar 5 eventVar 6 partyMember 7 otherVar)

(. set_variables_gamedata_base)
(+ set_variables_base)
(] 4 gamedataType set_variables_gamedata_type)

(> set_variables_gamedata DA{ ]4 set_variables_gamedata_base 0 set_variables_itemCount 1 set_variables_weaponCount 2 set_variables_armourCount 3 set_variables_actorVar 4 set_variables_enemyVar 5 set_variables_eventVar 6 set_variables_partyMember 7 set_variables_otherVar })

(. set_variables_itemCount)
(+ set_variables_gamedata_base)
(] 5 item item_id)

(. set_variables_weaponCount)
(+ set_variables_gamedata_base)
(] 5 item weapon_id)

(. set_variables_armourCount)
(+ set_variables_gamedata_base)
(] 5 armour armour_id)

; "Execution Points." :)

(e set_variables_actorVar_type 0 LV 1 EXP 2 HP 3 MP 4 param.0 5 param.1 6 param.2 7 param.3 8 param.4 9 param.5 10 param.6 11 param.7)
(. set_variables_actorVar)
(+ set_variables_gamedata_base)
(] 5 actor actor_id)
(] 6 actorVar set_variables_actorVar_type)

(e set_variables_enemyVar_type 0 HP 1 MP 2 param.0 3 param.1 4 param.2 5 param.3 6 param.4 7 param.5 8 param.6 9 param.7)
(. set_variables_enemyVar)
(+ set_variables_gamedata_base)
(] 5 enemy troop_enemy_id)
(] 6 enemyVar set_variables_enemyVar_type)

(e set_variables_eventVar_type 0 x 1 y 2 direction 3 screenX 4 screenY)
(. set_variables_eventVar)
(+ set_variables_gamedata_base)
(] 5 event character_id)
(] 6 eventVar set_variables_eventVar_type)

(. set_variables_partyMember)
(+ set_variables_gamedata_base)
(] 5 memberIndex int)

(e set_variables_otherVar_type 0 mapId 1 partySize 2 gold 3 steps 4 playTimeSecs 5 gameTimerSecs 6 saveCount 7 battleCount)
(. set_variables_otherVar)
(] 5 ovType set_variables_otherVar_type)

; --- AND THAT'S FINALLY DONE. Let there never be any more. ---
; -- Yay vehicles --

(vm
	(define-name transfer_vehicle_paramassist
		"Transfer vehicle "
		(@ ]0 vehicle_id)
		(if-eq ]1 0 (
			" to " (@ ]2 map_id #t) "[" (@ ]3) "," (@ ]4) "]"
		) (
			" by vars " (@ ]2 var_id #t) "[" (@ ]3 var_id #t) "," (@ ]4 var_id #t) "]"
		))
	)
)

; -- bits & pieces --

(vm
	(define-name iterate_actor_var
		(if-eq ]0 0 (@ ]1 iterate_actor_id #t) ("v." (@ ]1 var_id #t)))
	)
)

(e transfer_player_fadetype 0 dark 1 light 2 none)

(e change_text_options_position 0 top 1 middle 2 bottom)
(s weather_type none rain storm snow)
(e get_location_info_type 0 terrainTag 1 eventId 2 tileIdL0 3 tileIdL1 4 tileIdL2 5 regionId)
(e battle_processing_type 0 byTroopId 1 byTroopIdVar 2 randomEncounter)
(e shop_item_type 0 item 1 weapon 2 armour)
(e equipment_slot 0 weaponA 1 armourA/weaponB 2 armourB 3 armourC 4 armourD)
(> equipment_id int)
(E force_action_target TargetIdx -2 Last -1 Random)

(. equippable_add_remove_parameters_array)
(] 1 negate int_boolean)
(] 2 amountIsVar int_boolean)
(+ DA{ ]2 equippable_add_remove_parameters_array_amount_var 0 equippable_add_remove_parameters_array_amount })
(+ ]?4 includeEquipped unknown boolean)

(. equippable_add_remove_parameters_array_amount)
(] 3 amount int_default_1)
(. equippable_add_remove_parameters_array_amount_var)
(] 3 amountVar var_id)

(. weapon_add_remove_parameters)
(@ indent indent)
(@ parameters weapon_add_remove_parameters_array)

(. weapon_add_remove_parameters_array)
(] 0 item weapon_id)
(+ equippable_add_remove_parameters_array)

(. armour_add_remove_parameters)
(@ indent indent)
(@ parameters armour_add_remove_parameters_array)

(. armour_add_remove_parameters_array)
(] 0 item armour_id)
(+ equippable_add_remove_parameters_array)
