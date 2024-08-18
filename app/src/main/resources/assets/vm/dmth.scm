; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Translation helper universal utility functions

; dm-try-copy!
(define (dm-try-copy! (dst irio) (src any)) (or (eq? src #nil) (eq? dst #nil) (dm-enc! dst src)))
(help-set! dm-try-copy! "(dm-try-copy! DST SRC) : dm-enc! but both DST and SRC must not be #nil (it's intended that SRC is a RORIO)")

; Functions to import/export database entry translations as JSON

(define-syntax (dmth-props-call-sanity tvc jvc)
	(list let
		; ((tv TVC))
		(list (list (quote tv) tvc))
		; suppress null entries in arrays
		(list if-not (quote (dm-eq? tv (dm-key #nil)))
			(list let
				; ((jv JVC))
				(list (list (quote jv) jvc))
				(quote (if-not
					; if there's no JSON key to read/write, don't continue
					(eq? jv #nil)
					(props ex tv jv)
				))
			)
		)
	)
)
(help-set! dmth-props-call-sanity "(dmth-props-call-sanity TVC JVC) : Used on iterators, assumes variables 'ex' 'props' - ensures reasonably valid for translation")

; -- array --
(define (dmth-a-exchange ex target json props)
	(define alen (dm-a-len target))
	(define i 0)
	(if ex (dm-h-init json))
	(while (< i alen)
		(dmth-props-call-sanity
			(dm-a-ref target i)
			(if ex
				(dm-h-add! json (value->string i))
				(dm-h-ref json (value->string i))
			)
		)
		(set! i (+ i 1))
	)
)
(help-set! dmth-a-exchange "(dmth-a-exchange EX TARGET JSON ELM) : Pulls from JSON into array TARGET using (ELM EX TE JE) on each element.")

(define (dmth-a-importer props)
	(lambda (target json) (dmth-a-exchange #f target json props))
)
(help-set! dmth-a-importer "(dmth-a-importer PROPS) : Creates an importer")

(define (dmth-a-exporter props)
	(lambda (target json) (dmth-a-exchange #t target json props))
)
(help-set! dmth-a-exporter "(dmth-a-exporter PROPS) : Creates an exporter")

; -- hash --
(define (dmth-h-exchange ex target json props)
	(if ex (dm-h-init json))
	(for-each (lambda (key)
		(dmth-props-call-sanity
			(dm-h-ref target key)
			(if ex
				(dm-h-add! json
					(value->string (dm-dec key))
				)
				(dm-h-ref json
					(value->string (dm-dec key))
				)
			)
		)
	) (dm-h-keys target))
)
(help-set! dmth-h-exchange "(dmth-h-exchange EX TARGET JSON ELM) : Pulls from JSON into hash TARGET using (ELM EX TE JE) on each element.")

(define (dmth-h-importer props)
	(lambda (target json) (dmth-h-exchange #f target json props))
)
(help-set! dmth-h-importer "(dmth-h-importer PROPS) : Creates an importer")

(define (dmth-h-exporter props)
	(lambda (target json) (dmth-h-exchange #t target json props))
)
(help-set! dmth-h-exporter "(dmth-h-exporter PROPS) : Creates an exporter")

; -- ivars --
(define (dmth-i-exchange ex target json props)
	(if ex (dm-h-init json))
	(for-each (lambda (key)
		(dmth-props-call-sanity
			(dm-i-ref target key)
			(if ex
				(dm-h-add! json key)
				(dm-h-ref json key)
			)
		)
	) (dm-i-keys target))
)
(help-set! dmth-i-exchange "(dmth-i-exchange EX TARGET JSON ELM) : Pulls from JSON into ivars of TARGET using (ELM EX TE JE) on each element.")

(define (dmth-i-importer props)
	(lambda (target json) (dmth-i-exchange #f target json props))
)
(help-set! dmth-i-importer "(dmth-i-importer PROPS) : Creates an importer")

(define (dmth-i-exporter props)
	(lambda (target json) (dmth-i-exchange #t target json props))
)
(help-set! dmth-i-exporter "(dmth-i-exporter PROPS) : Creates an exporter")

; -- stuff --
(define (dmth-prop ex target json prop)
	(if ex
		(if-not (eq? target #nil)
			(dm-enc! (dm-h-add! json prop) target)
		)
		(dm-try-copy! target (dm-h-ref json prop))
	)
)
(help-set! dmth-prop "(dmth-prop EX TARGET JSON PROP) : Generic terminal property import/export for text JSON stuff")

; simple exchanges
(define (dmth-prim ex tv jv)
	(if ex
		(dm-try-copy! jv tv)
		(dm-try-copy! tv jv)
	)
)
