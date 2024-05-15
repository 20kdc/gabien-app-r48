
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Defining event command special schemas. (RXP Version)
; Yes, this was seriously the best plan I could think of other than outright hardcoding.

; looking back in September 2017, arrayDAM probably existed originally for this.
; And now I'm replacing it with the cleaner DA{}.
; Additional note, March 13th 2018 - DA{} should now be 'fully capable'.

(> string_array subwindow array 0 string)

(> int_default_1 int= 1)
(> int_default_10 int= 10)
(> int_default_50 int= 50)
(> int_default_100 int= 100)
(> int_default_200 int= 200)

(e cap_type 0 MaxHP 1 MaxSP 2 Str. 3 Dex. 4 Agi. 5 Int.)

; Conditional Branch

(e conditional_branch_types 0 switch 1 variable 2 selfSwitch 3 timer 4 actor 5 enemy 6 event 7 gold 8 item 9 weapon 10 armour 11 button 12 script)

(i RCOM/CommonConditionals)

; This is going to suck.

(e conditional_branch_actor_types 0 inParty 1 name= 2 skill? 3 weapon= 4 armourIdWorn? 5 hasState?)

(. conditional_branch_actor)
(vm
	(define-name conditional_branch_actor
		"Actor " (@ ]1 actor_id #t) " "
		(= ]2 ((@ ]2 conditional_branch_actor_types) " (...)")
			(0 "in party")
			(1 (@ : conditional_branch_actor_name))
			(2 (@ : conditional_branch_actor_skill))
			(3 (@ : conditional_branch_actor_weapon))
			(4 (@ : conditional_branch_actor_armourIdWorn))
			(5 (@ : conditional_branch_actor_hasState))
		)
	)
)
(] 0 type conditional_branch_types)
(] 1 actor actor_id)
(] 2 subCondition conditional_branch_actor_types)
(+ DA{ ]2 conditional_branch_actor_unknown 0 conditional_branch_actor_inParty 1 conditional_branch_actor_name 2 conditional_branch_actor_skill 3 conditional_branch_actor_weapon 4 conditional_branch_actor_armourIdWorn 5 conditional_branch_actor_hasState })

(e cbr_gold_comparison_type 0 >= 1 <)

(. conditional_branch_parameters)
(vm
	(define-name conditional_branch_parameters
		": " (= ]0 ((@ ]0 conditional_branch_types) " (...)")
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
		)
	)
)
(@ indent indent)
(@ parameters DA{ ]0 conditional_branch_unknown 0 conditional_branch_switch 1 conditional_branch_variable 2 conditional_branch_selfSwitch 3 conditional_branch_timer 4 conditional_branch_actor 5 conditional_branch_enemy 6 conditional_branch_character 7 conditional_branch_gold 8 conditional_branch_item 9 conditional_branch_weapon 10 conditional_branch_armour 11 conditional_branch_button 12 conditional_branch_script })

; Set Variables. I hope I didn't make a mistake.

(e set_variables_source 0 int 1 var 2 random 3 itemCount 4 actorVar 5 enemyVar 6 eventVar 7 otherVar)

(e set_variables_source_actor_var 0 level 1 EXP 2 HP 3 SP 4 MaxHP 5 MaxSP 6 strength 7 dex. 8 agility 9 intel. 10 atk 11 physicalDef 12 magicDef 13 evasion)
(e set_variables_source_enemy_var 0 HP 1 SP 2 MaxHP 3 MaxSP 4 strength 5 dexterity 6 agility 7 intelligence 8 attackPow 9 physicalDefense 10 magicDefense 11 evasionMod)

; "when you step on a yellow tile, you get electrocuted!" <- this is what terrain tags are probably for. Ouch.
(e set_variables_source_char_var 0 x 1 y 2 direction 3 screenX 4 screenY 5 terrainTag)
(e set_variables_source_other_var 0 mapID 1 partySize 2 gold 3 steps 4 playTime 5 timer 6 saveCount)

; Notably, the majority of the Set Variables command's pattern is in the Commands.txt file.
; Furthermore, CommonSV handles the int/var/random cases, and lets the parent schema control any further cases using set_variables_parameters_ext.
; It used to be that CommonSV defined this by default, but that now counts as a dodgy translation conflict.

(vm
	(define-name set_variables_parameters_ext (@ ]3 set_variables_source) (= ]3 ""
		(3 ": " (@ ]4 item_id #t))
		(4 ": " (@ ]4 actor_id #t) " " (@ ]5 set_variables_source_actor_var))
		(5 ": " (@ ]4 troop_enemy_id) " " (@ ]5 set_variables_source_enemy_var))
		(6 ": " (@ ]4 character_id #t) " " (@ ]5 set_variables_source_char_var))
		(7 ": " (@ ]4 set_variables_source_other_var))
	))
)

(i RCOM/CommonSV)

(. set_variables_itemCount)
(+ set_variables_base)
(] 4 itemId item_id)

(. set_variables_actorVar)
(+ set_variables_base)
(] 4 actorId actor_id)
(] 5 actorVar set_variables_source_actor_var)

(. set_variables_enemyVar)
(+ set_variables_base)
(] 4 enemyId troop_enemy_id)
(] 5 enemyVar set_variables_source_enemy_var)

(. set_variables_characterVar)
(+ set_variables_base)
(] 4 eventId character_id)
(] 5 eventVar set_variables_source_char_var)

(. set_variables_otherVar)
(+ set_variables_base)
(] 4 charVar set_variables_source_other_var)

(. set_variables_parameters)
(@ indent indent)
(@ parameters DA{ ]3 set_variables_base 0 set_variables_int 1 set_variables_var 2 set_variables_random 3 set_variables_itemCount 4 set_variables_actorVar 5 set_variables_enemyVar 6 set_variables_characterVar 7 set_variables_otherVar })

; Change Map Settings

(e cms_type 0 panorama 1 fog 2 battleback)

(. change_map_settings_parameters_panorama)
(] 0 type cms_type)
(] 1 name f_pano_name)
(] 2 hue hue)

(. change_map_settings_parameters_fog)
(] 0 type cms_type)
(] 1 name f_fog_name)
(] 2 hue hue)
(] 3 opacity opacity)
(] 4 blendType blend_type)
(] 5 zoom int= 100)
(] 6 sx int)
(] 7 sy int)

(. change_map_settings_parameters_battleback)
(] 0 type cms_type)
(] 1 name f_bb_name)

(. change_map_settings_parameters)
(@ indent indent)
(@ parameters DA{ ]0 change_map_settings_parameters_fog 0 change_map_settings_parameters_panorama 1 change_map_settings_parameters_fog 2 change_map_settings_parameters_battleback })

; Shop items.

(e shop_item_type 0 item 1 weapon 2 armour)

(. shop_items_parameters_unknown)
(] 0 type shop_item_type)
(] 1 item int)

(. shop_items_parameters_item)
(] 0 type shop_item_type)
(] 1 item item_id)

(. shop_items_parameters_weapon)
(] 0 type shop_item_type)
(] 1 weapon weapon_id)

(. shop_items_parameters_armour)
(] 0 type shop_item_type)
(] 1 armour armour_id)

(. shop_items_parameters)
(@ indent indent)
(@ parameters DA{ ]0 shop_items_parameters_unknown 0 shop_items_parameters_item 1 shop_items_parameters_weapon 2 shop_items_parameters_armour })

; Actor equip.

(e actor_equip_type 0 Weapon 1 Armour0 2 Armour2 3 Armour3 4 Armour4)

; Transfer Player

(e transfer_player_fadetype 0 dark 1 none)
