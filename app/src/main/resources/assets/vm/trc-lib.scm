; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Dynamic translation compiler primitives

; Compiles some generic tr-at business
(define (tr-dyni-path ctx path) (list dm-at (tr-dyn-cctx-focus ctx) path))

; (vm CODE...) : raw VM
(define (tr-dynx-vm ctx . content) (append! (tr-dyn-cctx-target ctx) content))
; (with FOCUS CODE...) : change focus
(define (tr-dynx-with ctx sym . content)
	(set! ctx (tr-dyn-cctx-copy ctx))
	(tr-dyn-cctx-focus-set! ctx sym)
	(tr-dyn-compiler-list ctx content)
)
; (@ PATH [INTERP [PREFIX]]) : display value if present
(define (tr-dyni-at ctx path interp prefix)
	(append!
		(tr-dyn-cctx-target ctx)
		(list
			(list
				dm-fmt
				(tr-dyni-path ctx path)
				; this is so interp can be a symbol
				(if (eq? interp #nil)
					#nil
					(value->string interp)
				)
				prefix
			)
		)
	)
)
(define (tr-dynx-@ ctx path . extra) (cond
	((= (list-length extra) 0) (tr-dyni-at ctx path #nil #f))
	((= (list-length extra) 1) (tr-dyni-at ctx path (list-ref extra 0) #f))
	((= (list-length extra) 2) (tr-dyni-at ctx path (list-ref extra 0) (list-ref extra 1)))
	(else (error "bad arg count to @"))
))
; ($ APFX PATH [INTERP [PREFIX-ENUMS]]) : if present, display value with prefix
(define (tr-dyni-sat ctx apfx path interp prefix)
	(append!
		(tr-dyn-cctx-target ctx)
		(list
			(list
				let
				(list (list
					'tmp$ (tr-dyni-path ctx path)
				))
				(list
					if
					(list
						eq?
						'tmp$
						#nil
					)
					""
					(list
						string-append
						" "
						(list
							dm-fmt
							'tmp$
							; this is so interp can be a symbol
							(if (eq? interp #nil)
								#nil
								(value->string interp)
							)
							prefix
						)
					)
				)
			)
		)
	)
)
(define (tr-dynx-$ ctx apfx path . extra) (cond
	((= (list-length extra) 0) (tr-dyni-sat ctx apfx path #nil #f))
	((= (list-length extra) 1) (tr-dyni-sat ctx apfx path (list-ref extra 0) #f))
	((= (list-length extra) 2) (tr-dyni-sat ctx apfx path (list-ref extra 0) (list-ref extra 1)))
	(else (error "bad arg count to $"))
))
; (? PATH TRUE [FALSE]) : check for presence
(define (tr-dyni-exists ctx path true false)
	(append!
		(tr-dyn-cctx-target ctx)
		(list
			(list if
				(list eq? (tr-dyni-path ctx path) #nil)
				(tr-dyn-compiler-expr false ctx)
				(tr-dyn-compiler-expr true ctx)
			)
		)
	)
)
(define (tr-dynx-? ctx path true . extra) (cond
	((= (list-length extra) 0) (tr-dyni-exists ctx path true ""))
	((= (list-length extra) 1) (tr-dyni-exists ctx path true (list-ref extra 0)))
	(else (error "bad arg count to ?"))
))
; (= PATH DEF (VAL RES)...) : check for equality
; responsible for "(VAL RES)..." into a usable set of cond clauses
(define (tr-dyni-eqci ctx extra)
	(define clauses (list))
	(for-each (lambda (v)
		(assert (list? v) "= clauses must be lists")
		(assert (= (list-length v) 2) "= clauses must be two-element lists")
		(append! clauses (list
			(list
				; condition
				(list equal? 'tmp$ (list quote (list-ref v 0)))
				; clause contents
				(tr-dyn-compiler-expr (list-ref v 1) ctx)
			)
		))
	) extra)
	clauses
)
(define (tr-dynx-= ctx path def . extra)
	(append!
		(tr-dyn-cctx-target ctx)
		; (let (tmp$ PATH) COND)
		(list
			(list
				let
				(list (list
					'tmp$ (list dm-decode (tr-dyni-path ctx path))
				))
				; (cond ... (else DEF))
				(append!
					(list cond)
					(tr-dyni-eqci ctx extra)
					(list
						; (else DEF)
						(list 'else (tr-dyn-compiler-expr def ctx))
					)
				)
			)
		)
	)
)
