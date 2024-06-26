
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; ___
; | |
; | |
; --- 3
; the third monolith
; for battle commands only

(. show_battle_animation_parameters)
(@ indent indent)
(@ parameters show_battle_animation_array)

(E show_battle_animation_ti PartyIndex \-1 allMembers)

(. show_battle_animation_array)
(] \0 _ string)
(+ ]?4 targetPlayerParty_2k3 false int_boolean)
(] \2 targetIndex show_battle_animation_ti)
(] \1 animation animation_id)
(] \3 wait int_boolean)

; The Battle Conditional Branch.
; My final enemy.
; The last scourge that prevents ascension to the Database Mines,
;  where I will spend my days in ignorance of this system.
; I shall defeat you! For the citizens of Kittainia!
; (dramatic enough?)

(. battle_cbranch_parameters)
(@ indent indent)
(@ parameters battle_cbranch_array)

; pretty sure all battle commands are 2k3 but don't quote me on that
(e battle_cbranch_type \0 switch \1 varCompare \2 actorCanAct \3 enemyCanAct \4 targettedEnemyIs \5 actorLastUsedCommand_2k3)

(. battle_cbranch_array)
(] \0 _ string)
(] \1 type battle_cbranch_type)
(+ DA{ ]1 battle_cbranch_nx \0 battle_cbranch_switch \1 battle_cbranch_varcomp \2 battle_cbranch_aca \3 battle_cbranch_enemyidx \4 battle_cbranch_enemyidx \5 battle_cbranch_aluc })

(. battle_cbranch_nx)

(. battle_cbranch_switch)
(] \2 switch switch_id)
(] \3 invert int_boolean)

(. battle_cbranch_varcomp)
(] \2 var var_id)
(] \5 compareOp cbranch_varcomp_e)
(] \3 targetIsVar int_boolean)
(+ DA{ ]3 battle_cbranch_varcomp_v \0 battle_cbranch_varcomp_c })

(. battle_cbranch_varcomp_v)
(] \4 targetVar var_id)

(. battle_cbranch_varcomp_c)
(] \4 target int)

(. battle_cbranch_aca)
(] \2 actor actor_id)

(. battle_cbranch_enemyidx)
(] \2 enemyId int)

(. battle_cbranch_aluc)
(] \2 actor actor_id)
(] \3 command battlecommand_id)
