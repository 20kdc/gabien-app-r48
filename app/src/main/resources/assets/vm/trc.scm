; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Translation compiler context
(define (tr-dyn-cctx-target ctx) (list-ref ctx 0))
(define (tr-dyn-cctx-focus ctx) (list-ref ctx 1))
(define (tr-dyn-cctx-target-set! ctx v) (list-set! ctx 0 v))
(define (tr-dyn-cctx-focus-set! ctx v) (list-set! ctx 1 v))

(define (tr-dyn-cctx-copy ctx) (append ctx))
(help-set! tr-dyn-cctx-copy "(tr-dyn-cctx-copy) : Copies dynamic translation compiler context.")

; Translation compiler core

; Translation compiler: get element
(define (tr-dyn-dynx-get sym)
	(eval
		(string->symbol (string-append "tr-dynx-" (symbol->string sym)))
	(interaction-environment))
)
; Translation compiler: single element into target
(define (tr-dyn-compiler code ctx) (cond
	; "bleh"
	((string? code)
		(append! (tr-dyn-cctx-target ctx) (list code))
	)
	; "(vm 123)" etc.
	(
		(and
			(list? code)
			(> (list-length code) 0)
			(symbol? (car code))
		)
		(apply
			(tr-dyn-dynx-get (car code))
			(append!
				(list ctx)
				(cdr code)
			)
		)
	)
	(else (error (.. "Unable to recognize code: " code)))
))

(define (tr-dyn-compiler-list code ctx) (for-each
	(lambda (v)
		(tr-dyn-compiler v ctx)
	) code
))
(help-set! tr-dyn-compiler-list "(tr-dyn-compiler-list CODE CTX) : Contributes compiled DTL elements from CODE list.")

(define (tr-dyn-compiler-root code base)
	; setup initial context
	(define ctx (list
		; target
		(list base)
		; focus
		'a0
	))
	; actually eval
	(tr-dyn-compiler-list code ctx)
	; target is now something like (fl1 ...) - eval it
	(eval (tr-dyn-cctx-target ctx) (interaction-environment))
)
(help-set! tr-dyn-compiler-root "(tr-dyn-compiler-root CODE BASE) : Dynamic translation compiler for some base fl* macro.")

(define (tr-dyn-compiler-ff1 code) (tr-dyn-compiler-root code fl1))
(help-set! tr-dyn-compiler-ff1 "(tr-dyn-compiler-ff1 CODE) : Dynamic translation compiler, invoked Javaside.")

(define (tr-dyn-compiler-ff2 code) (tr-dyn-compiler-root code fl2))
(help-set! tr-dyn-compiler-ff2 "(tr-dyn-compiler-ff2 CODE) : Dynamic translation compiler, invoked Javaside.")
