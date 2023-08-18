; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Common macros/etc.

(define-name var_id_encased "Var[" (@ : var_id #t) "]")
(define-name var_id_indirect "Var[Var[" (@ : var_id #t) "]]")
(define-name int_boolean_switch     (if-ne : 0 "ON" "OFF"))
(define-name int_boolean_switch_not (if-eq : 0 "ON" "OFF"))

(cmdb-add-tag "translatable" "Translatable")
