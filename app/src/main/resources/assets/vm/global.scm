; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; MiniVM global additions

(include "vm/rxrs")

(define-syntax (assert expr . err)
	; (if-not EXPR (error ERR...))
	(list if-not expr
		(append! (list error) err)
	)
)
(help-set! assert "(assert EXPR ERR...) : Macro to error if a condition is not fulfilled.")

; other stuff
(include "vm/namespacing")

; for quick formatting
(define-syntax (.. . entries)
	(cond
		; optimization: empty becomes ""
		((= (list-length entries) 0) "")
		; optimization: single constant string just becomes that string
		((and
			(= (list-length entries) 1)
			(string? (list-ref entries 0))
		) (list-ref entries 0))
		; everything else
		(else
			(define result (list string-append))
			(for-each (lambda (entry)
				(append! result (list (list value->string entry)))
			) entries)
			result
		)
	)
)
(help-set! .. "(.. V...) : Macro that wraps each parameter in value->string and the whole in string-append.")

; for very quick formatting
(define-syntax (fl1 . entries) (list lambda (quote (a0)) (append (quote (..)) entries)))
(help-set! fl1 "(fl1 V...) : Formatting Lambda 1: equivalent to (lambda (a0) (.. V...))")

(define-syntax (fl2 . entries) (list lambda (quote (a0 a1)) (append (quote (..)) entries)))
(help-set! fl2 "(fl2 V...) : Formatting Lambda 2: equivalent to (lambda (a0 a1) (.. V...))")

(define-syntax (fl3 . entries) (list lambda (quote (a0 a1 a2)) (append (quote (..)) entries)))
(help-set! fl3 "(fl3 V...) : Formatting Lambda 3: equivalent to (lambda (a0 a1 a2) (.. V...))")

(define-syntax (fl4 . entries) (list lambda (quote (a0 a1 a2 a3)) (append (quote (..)) entries)))
(help-set! fl4 "(fl4 V...) : Formatting Lambda 4: equivalent to (lambda (a0 a1 a2 a3) (.. V...))")

(include "vm/dmth")
