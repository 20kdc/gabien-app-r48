; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(define (define-prefix-core prefix definer entries prefixer)
	(define result (list begin))
	(define hasKey #f)
	(define key #nil)
	(for-each (lambda (value)
		(if hasKey
			(begin
				(append! result (list (list
					definer
					(string->symbol
						(string-append (value->string prefix) (value->string key))
					)
					value
				)))
				(set! hasKey #f)
			)
			(if (list? value)
				; the key is a list, so it's actually a sub-group
				(append! result (list
					(append!
						(list
							prefixer
							(string->symbol
								(string-append (value->string prefix) (value->string (list-ref value 0)))
							)
						)
						(cdr value)
					)
				))
				(begin
					; otherwise, this is really a key
					(set! key value)
					(set! hasKey #t)
				)
			)
		)
	) entries)
	result
)
(help-set! define-prefix-core (string-append
	"(define-prefix-core PREFIX DEFINER ENTRIES PREFIXER) : Implements the define-prefix codegen logic for a given 'define'-alike.\n"
	"PREFIX is the actual prefix symbol as passed into your macro.\n"
	"DEFINER is something 'define-like'.\n"
	"ENTRIES is the entry VA as passed into your macro.\n"
	"PREFIXER is the macro calling this, for recursive invocation.\n"
))

; for sane namespacing
(define-syntax (define-prefix prefix . entries)
	(define-prefix-core prefix define entries define-prefix)
)
(help-set! define-prefix (string-append
	"(define-prefix PREFIX K/V...) : "
	"Defines a prefixed group of values as key/value pairs with a given symbol prefix.\n"
	"Example would be (define-prefix abc def 123 ghi 456). This creates abcdef = 123 and abcghi = 456.\n"
	"If a key is a list, then the first element of that is an additional prefix to append to the first.\n"
	"Therefore, (define-prefix a (b c 1 d 2)) creates abc = 1 and abd = 2."
))

(define-syntax (define-group prefix . entries)
	(append!
		(list define-prefix
			(string->symbol (string-append (value->string prefix) "."))
		)
		entries
	)
)
(help-set! define-group "(define-group PREFIX K/V...) : See define-prefix, but the outer prefix has a dot appended.")

; translation namespacing
(define-syntax (tr-set-quoted! k v)
	(list tr-set! k (list quote v))
)
(help-set! tr-set-quoted! "(tr-set-quoted! K V) : Like tr-set!, but the value is quoted. This is better because a compilation step is performed by tr-set! itself.")

(define-syntax (tr-prefix prefix . entries)
	(define-prefix-core prefix tr-set-quoted! entries tr-prefix)
)
(help-set! tr-prefix (string-append
	"(tr-prefix PREFIX K/V...) : "
	"Sets a prefixed group of dynamic translation pairs as key/value pairs with a given symbol prefix.\n"
	"Essentially like define-prefix, but using tr-set-quoted!\n"
))

(define-syntax (tr-group prefix . entries)
	(append!
		(list tr-prefix
			(string->symbol (string-append (value->string prefix) "."))
		)
		entries
	)
)
(help-set! tr-group "(tr-group PREFIX K/V...) : See tr-prefix, but the outer prefix has a dot appended.")
