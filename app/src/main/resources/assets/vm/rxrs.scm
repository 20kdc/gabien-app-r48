; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Implementation of some Scheme standard primitives on D/MVM

; all Java classes we care about
(define java.util.List (string->class "java.util.List"))

; obvious operations - lists

(define (list . v) v)
(help-set! list "(list V...) : Creates a list of values.")

(define (list? v) (instance? java.util.List v))
(help-set! list? "(list? V) : Returns true if V is a list.")

(define (car v) (list-ref v 0))
(define (cdr v) (sublist v 1 (list-length v)))

; obvious operations - cadr composites

(define (caar v) (list-ref (list-ref v 0) 0))
(define (caaar v) (list-ref (list-ref (list-ref v 0) 0) 0))
(define (caaaar v) (list-ref (list-ref (list-ref (list-ref v 0) 0) 0) 0))

(define (cdar v) (cdr (list-ref v 0)))
(define (cdaar v) (cdr (list-ref (list-ref v 0) 0)))
(define (cdaaar v) (cdr (list-ref (list-ref (list-ref v 0) 0) 0)))

; obvious operations - booleans

(define (not v) (if v #f #t))

; cond is honestly much easier to understand if implemented as these two components
; one is if with fancy syntax, the other restructures the list into a tree
(define-syntax (cond-branch branch else-code)
	(if
		(eq? 'else (car branch))
		; this branch is an else-guard, so we stop here, compiling just that guard as-is into a begin
		(append! (list begin) (cdr branch))
		; this branch is not an else-guard, sadly, so it needs to become one of two forms based on length
		(if
			(= (list-length branch) 1)
			; guard-only: (if-not GUARD ELSECODE) (uses MVM's if-return-rule)
			(list
				if-not
				; GUARD
				(car branch)
				else-code
			)
			; (if GUARD (begin CONTENT...) ELSECODE)
			(list
				if
				; GUARD
				(car branch)
				; (begin CONTENT...)
				(append! (list begin) (cdr branch))
				else-code
			)
		)
	)
)
(define-syntax (cond . branches)
	(if (= (list-length branches) 0)
		; no branches
		#f
		; we have at least one branch, let's see what to do about it...
		(list cond-branch (car branches) (append! (list cond) (cdr branches)))
	)
)

; conditional short-circuiting logic ops

(define-syntax (and . branches)
	(cond
		((= (list-length branches) 0) #t)
		((= (list-length branches) 1) (car branches))
		(else
			; create (if X (and REMAINDER...)) (uses MVM's if-return-rule)
			(list if (car branches)
				(append! (list and) (cdr branches))
			)
		)
	)
)

(define-syntax (or . branches)
	(cond
		((= (list-length branches) 0) #f)
		((= (list-length branches) 1) (car branches))
		(else
			; create (if-not X (or REMAINDER...)) (uses MVM's if-return-rule)
			(list if-not (car branches)
				(append! (list or) (cdr branches))
			)
		)
	)
)