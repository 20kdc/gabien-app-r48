
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Common Conditional Branch helpers between RXP and RVXA.

(. conditional_branch_switch)
(vm
	(define-name conditional_branch_switch "Switch " (@ ]1 switch_id #t) " " (@ ]2 int_boolean_switch_not))
)
(] 0 type conditional_branch_types)
(] 1 switch switch_id)
(] 2 invert int_boolean)

(. conditional_branch_variable)
(vm
	(define-name conditional_branch_variable (@ ]1 var_id_encased) " " (@ ]4 comparison_type) " "
		(if-eq ]2 0 (@ ]3) (@ ]3 var_id_encased))
	)
)
(] 0 type conditional_branch_types)
(] 1 var var_id)
(] 4 cond comparison_type)
(] 2 targetIsVar int_boolean)
(+ DA{ ]2 conditional_branch_variable_bn 0 conditional_branch_variable_bn 1 conditional_branch_variable_by })

(. conditional_branch_variable_bn)
(] 3 target int)
(. conditional_branch_variable_by)
(] 3 target var_id)

(. conditional_branch_selfSwitch)
(vm
	(define-name conditional_branch_selfSwitch "Self Switch " (@ ]1 selfswitch_id) " is " (@ ]2 int_boolean_switch_not))
)
(] 0 type conditional_branch_types)
(] 1 switch selfswitch_id)
(] 2 invert int_boolean)

(e conditional_branch_timer_conditional 0 > 1 <)
(. conditional_branch_timer)
(vm
	(define-name conditional_branch_timer "Timer " (@ ]2 conditional_branch_timer_conditional) " " (@ ]1) " seconds")
)
(] 0 type conditional_branch_types)
(] 1 timeSecs int)
(] 2 timerCond conditional_branch_timer_conditional)

; -- Actor def. isn't completely common but some parts are

(. conditional_branch_actor_unknown)
(. conditional_branch_actor_inParty)

(. conditional_branch_actor_name)
(vm
	(define-name conditional_branch_actor_name "named " (@ ]3))
)
(] 3 name string)

(. conditional_branch_actor_skill)
(vm
	(define-name conditional_branch_actor_skill "skill " (@ ]3 skill_id #t))
)
(] 3 skill skill_id)

(. conditional_branch_actor_weapon)
(vm
	(define-name conditional_branch_actor_weapon "weapon " (@ ]3 weapon_id #t))
)
(] 3 weapon weapon_id)

(. conditional_branch_actor_armourIdWorn)
(vm
	(define-name conditional_branch_actor_armourIdWorn "wears armour " (@ ]3 armour_id #t))
)
(] 3 armour armour_id)

(. conditional_branch_actor_hasState)
(vm
	(define-name conditional_branch_actor_hasState "has state " (@ ]3 state_id #t))
)
(] 3 state state_id)

; ---

(. conditional_branch_enemy)
(vm
	(define-name conditional_branch_enemy "Enemy " (@ ]1 troop_enemy_id) " "
		(if-eq ]2 0 "is alive" ("has state " (@ ]3 state_id #t)))
	)
)
(] 0 type conditional_branch_types)
(] 1 enemy troop_enemy_id)
(] 2 checkState int_boolean)
(] 3 state state_id)

(. conditional_branch_character)
(vm
	(define-name conditional_branch_character "Character " (@ ]1 character_id #t) " facing " (@ ]2 direction))
)
(] 0 type conditional_branch_types)
(] 1 event character_id)
(] 2 direction direction)

; NOTE THAT CBR_GOLD_COMPARISON_TYPE IS NOT DEFINED!
; The available comparison types are different between engines.

(. conditional_branch_gold)
(vm
	(define-name conditional_branch_gold "Gold " (@ ]2 cbr_gold_comparison_type) " " (@ ]1))
)
(] 0 type conditional_branch_types)
(] 1 gold int)
(] 2 comparision cbr_gold_comparison_type)

(. conditional_branch_item)
(vm
	(define-name conditional_branch_item "Item: " (@ ]1 item_id #t))
)
(] 0 type conditional_branch_types)
(] 1 itemNum item_id)

(. conditional_branch_weapon)
(vm
	(define-name conditional_branch_weapon "Weapon: " (@ ]1 weapon_id #t))
)
(] 0 type conditional_branch_types)
(] 1 weaponNum weapon_id)

(. conditional_branch_armour)
(vm
	(define-name conditional_branch_armour "Armour: " (@ ]1 armour_id #t))
)
(] 0 type conditional_branch_types)
(] 1 armourNum armour_id)

(. conditional_branch_button)
(vm
	(define-name conditional_branch_button "Button: " (@ ]1))
)
(] 0 type conditional_branch_types)
(] 1 buttonId int)

(. conditional_branch_script)
(vm
	(define-name conditional_branch_script "Script: " (@ ]1))
)
(] 0 type conditional_branch_types)
(] 1 ruby string)

; Vehicle is VXA-only

(. conditional_branch_unknown)
(] 0 type conditional_branch_types)
