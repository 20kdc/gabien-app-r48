
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; VX Ace Battle Effects Helpfile
; Will be implemented as a miniature help dialog,
;  for simplicity and flexibility.
; Notably, due to this, these IDs are also recorded in SchemaFiles.txt as the enum battle_effect_code.
; (Given the interesting nature of this system,
;  I don't trust them not to do the same thing,
;   which would explain how "Yanfly" has such flexibility.
;  Looking from the outside-in, this is as much as I can do.)

(obj 11 "Recover HP")
(.)
(. @value1: Decimal of Max.HP recovered.)
(. @value2: Additional HP as a direct number.)
(. @data_id: No meaning.)
(.)

(obj 12 "Recover MP")
(.)
(. @value1: Decimal of Max.MP recovered.)
(. @value2: Additional MP as a direct number.)
(. @data_id: No meaning.)
(.)

(obj 13 "Gain TP")
(.)
(. @value1: TP gained.)
(. @value2: No meaning.)
(. @data_id: No meaning.)
(.)

(obj 21 "Add State(s)")
(.)
(. @value1: Decimal chance of applying state\(s\).)
(. @value2: No meaning.)
(. @data_id: State ID, or \0 to apply the actor\'s usual attack states.)
(.)

(obj 22 "Remove State")
(.)
(. @value1: Decimal chance of removing state\(s\).)
(. @value2: No meaning.)
(. @data_id: State ID to remove on success.)
(.)

(obj 31 "Add Buff")
(.)
(. \(Note: Overlapping buffs last until the last one ends?\))
(. @value1: Turns the buff lasts for.)
(. @value2: No meaning.)
(. @data_id: Parameter ID.)
(.)

(obj 32 "Add Debuff")
(.)
(. \(Note: Overlapping debuffs last until the last one ends?\))
(. @value1: Turns the debuff lasts for.)
(. @value2: No meaning.)
(. @data_id: Parameter ID.)
(.)

(obj 33 "Remove Buffs")
(.)
(. @value1: No meaning.)
(. @value2: No meaning.)
(. @data_id: Parameter ID.)
(.)

(obj 34 "Remove Debuffs")
(.)
(. @value1: No meaning.)
(. @value2: No meaning.)
(. @data_id: Parameter ID.)
(.)

(obj 41 "Special Effect")
(.)
(. @value1: No meaning.)
(. @value2: No meaning.)
(. @data_id: Set to \0 for \'ESCAPE\'. No other meanings by default.)
(.)

(obj 42 "Increase Param.")
(.)
(. @value1: Amount.)
(. @value2: No meaning.)
(. @data_id: Parameter ID.)
(.)

(obj 43 "Learn Skill")
(.)
(. @value1: No meaning.)
(. @value2: No meaning.)
(. @data_id: Skill ID.)
(.)

; Not THAT CEV.

(obj 44 "Run CEV.")
(.)
(. @value1: No meaning.)
(. @value2: No meaning.)
(. @data_id: Common Event Id.)
(.)
