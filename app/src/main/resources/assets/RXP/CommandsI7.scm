
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; [Interpreter Part 7]

(cmd 331 "Change Enemy" ($ " " ]0 iterate_enemy #t) " HP" ($ " by " : operate_value_1) (? ]4 (if-eq ]4 #t " (can kill)" " (never kills)")))
(C category 4)
(p enemyType iterate_enemy)
; operate_value
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_100)
(p canKill boolean)

(cmd 332 "Change Enemy" ($ " " ]0 iterate_enemy #t) " SP" ($ " by " : operate_value_1))
(C category 4)
(p enemyType iterate_enemy)
; operate_value
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_10)

; 333 Add/Remove Enemy State 334 recovery, -> RCOM/CommonCommands

(cmd 335 "Show Enemy" ($ " " ]0 troop_enemy_id #t))
(C category 4)
(p enemy troop_enemy_id)

(cmd 336 "Transform Enemy" ($ " " ]0 troop_enemy_id #t) ($ " into " ]1 enemy_id #t))
(C category 4)
(p changeling troop_enemy_id)
(p transform enemy_id)

(cmd 337 "Show" ($ " " ]0 iterate_battler0) ($ " " ]1 iterate_battler1) " Battle Animation" ($ " " ]2 animation_id))
(C category 4)
(p ib0 iterate_battler0)
(p ib1 iterate_battler1)
(p animId animation_id)

(cmd 338 "Deal" ($ " " ]0 iterate_battler0) ($ " " ]1 iterate_battler1) " damage")
(C category 4)
(p ib0 iterate_battler0)
(p ib1 iterate_battler1)
(p damageIsVar int_boolean)
(P 2 damageVar var_id)
(v 0 damage int)

; NB. Need to figure this whole thing out, and switch to EF-mode.
(cmd 339 "Force Action")
(C category 4)
(p ib0 iterate_battler0)
(p ib1 iterate_battler1)
(p kind enemy_action_kind)
(P 2 act int)
(v 0 actBasic enemy_action_basic)
(v 1 actSkill skill_id)
(v 2 actItem item_id)
(p target force_action_target)
(p runNow int_boolean)

; Screen changes (340,351,352,353,354) and Ruby (355,655) -> RCOM/CommonCommands