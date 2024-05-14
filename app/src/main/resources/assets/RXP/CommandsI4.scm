
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; [Interpreter Part 4]

; 121/122/123 (Control Switch/Set Variable/Control Selfswitch) in CommonCommands.
; 124 (Timer) in CommonCommands.

(obj 125 "\"Modify Gold\" (? ]0 (\" by \" (@ : operate_value_0)))")
(C category \2)
(d Modifies the amount of gold the party has.)
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P \1 modVar var_id)
(v \0 mod int_default_100)

(obj 126 "\"Modify Item\" (? ]0 (\" \" (@ ]0 item_id #t) \" by \" (@ : operate_value_1)))")
(C category \2)
(d Gives or takes away some amount of a given item from/to the party.)
(p item item_id)
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P \2 modVar var_id)
(v \0 mod int_default_1)

(obj 127 "\"Modify Weapon\" (? ]0 (\" \" (@ ]0 weapon_id #t) \" by \" (@ : operate_value_1)))")
(C category \2)
(d Gives or takes away some amount of a given weapon from/to the party.)
(p weapon weapon_id)
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P \2 modVar var_id)
(v \0 mod int_default_1)

(obj 128 "\"Modify Armour\" (? ]0 (\" \" (@ ]0 armour_id #t) \" by \" (@ : operate_value_1)))")
(C category \2)
(d Gives or takes away some amount of a given armour from/to the party.)
(p armour armour_id)
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P \2 modVar var_id)
(v \0 mod int_default_1)

; 129 Add/Remove Actor -> RCOM/CommonCommands

; Command 130 was skipped?

(obj 131 "\"Change Windowskin\"")
(C category \5)
(d Changes the Windowskin used by the game. This Windowskin is used in stretched mode.)
(p file string)

(obj 132 "\"Change Battle BGM\" ($ \" to \" ]0)")
(C category \5)
(d Changes the battle BGM used by the game.)
; May not actually work in middle of battle.
; Or the variable change is auto-detected...
(p audio rpg_audiofile_bgm)

(obj 133 "\"Change Battle End ME\" ($ \" to \" ]0)")
(C category \5)
(d Changes the battle end musical-effect.)
(p audio rpg_audiofile_me)

; Enable/Disable 134/135/136 -> RCOM/CommonCommands
