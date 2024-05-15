
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Common Set Variables helpers between RXP and RVXA.

(e set_variables_operation 0 "=" 1 "+=" 2 "-=" 3 "*=" 4 "/=" 5 "%=")

; Setup a default.

(vm
	(define-name set_variables_parameters (= ]3 (@ : set_variables_parameters_ext)
		(0 "int: " (@ ]4))
		(1 "var: " (@ ]4 var_id #t))
		(2 "random: " (@ ]4) " through " (@ ]5))
	))
)

(. set_variables_base)
(] 0 firstVar var_id)
(] 1 lastVar var_id)
(] 2 op set_variables_operation)
(] 3 source set_variables_source)

(. set_variables_int)
(+ set_variables_base)
(] 4 value int)

(. set_variables_var)
(+ set_variables_base)
(] 4 sourceVar var_id)

(. set_variables_random)
(+ set_variables_base)
(] 4 minimum int)
(] 5 maximum int)
