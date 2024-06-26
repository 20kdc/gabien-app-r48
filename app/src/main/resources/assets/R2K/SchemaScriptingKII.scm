
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; |
; |/
; |\ II

; Key input information

(. key_input_information_parameters)
(@ indent indent)
(@ parameters key_input_information_base)

(. key_input_information_base)
(] \0 _ string)
(] \1 var var_id)
(] \2 wait int_boolean)
(] \4 checkAction int_boolean)
(] \5 checkCancel int_boolean)
(+ lengthAdjust \2k\ <\ 1.50 \10)
(+ lengthAdjust \2k\ >=\ 1.50 \11)
(+ lengthAdjust \2k3 \15)
(+ DA{ :length key_input_information_2ku150 \11 key_input_information_2k150 \15 key_input_information_2k3 })

(. key_input_information_2ku150)
; unsure if this works for other versions
(] \3 checkAllDirections int_boolean)

(. key_input_information_2k150)
(] \3 unknown int_boolean)
(] \6 checkShift int_boolean)
(] \7 checkDown int_boolean)
(] \8 checkLeft int_boolean)
(] \9 checkRight int_boolean)
(] \10 checkUp int_boolean)

(. key_input_information_2k3)
(] \3 unknown int_boolean)
(] \6 checkNumber int_boolean)
(] \7 checkMaths int_boolean)
(] \8 recordUseTimerB int_boolean)
(] \9 recordTime int_boolean)
(] \10 checkShift int_boolean)
(] \11 checkDown int_boolean)
(] \12 checkLeft int_boolean)
(] \13 checkRight int_boolean)
(] \14 checkUp int_boolean)
