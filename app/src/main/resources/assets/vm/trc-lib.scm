; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Dynamic translation compiler primitives

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
				(list
					dm-at
					(tr-dyn-cctx-focus ctx)
					path
				)
				interp
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
; ($ APFX PATH [INTERP [PREFIX]]) : if present, display value with prefix
(define (tr-dyni-sat ctx apfx path interp prefix)
	(append!
		(tr-dyn-cctx-target ctx)
		(list
			(list
				let
				(list
					(list
						'tmp$
						(list
							dm-at
							(tr-dyn-cctx-focus ctx)
							path
						)
					)
				)
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
							interp
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
