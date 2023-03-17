; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; MiniVM app context additions

; Translation compiler
; (vm EXPR) : regular D/MVM code
; anything else is returned as-is
(define (tr-dyn-compiler code) (cond
	(
		(and
			(list? code)
			(> (list-length code) 1)
			(eq? (car code) 'vm)
		)
		(eval (list-ref code 1) (interaction-environment))
	)
	(else code)
))
(help-set! tr-dyn-compiler "(tr-dyn-compiler CODE) : Dynamic translation compiler, invoked Javaside.")

(define-syntax (fmt-at base path) (list dm-fmt (list dm-at base path)))
(help-set! fmt-at "(fmt-at BASE PATH) : (dm-fmt (dm-at BASE PATH))")
