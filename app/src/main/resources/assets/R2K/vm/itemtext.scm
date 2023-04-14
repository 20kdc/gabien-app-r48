; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Functions to import/export item database translations as JSON

(define (r2k-rpg-database-items-import-text target json)
	(for-each (lambda (key)
		(define tv (dm-h-ref target key))
		(define jv (dm-h-ref json
			(dm-key (value->string (dm-dec key)))
		))
		(dm-try-copy-at! tv @name jv ":{$name")
		(dm-try-copy-at! tv @description jv ":{$description")
	) (dm-h-keys target))
)

(define (r2k-rpg-database-items-export-text target json)
	(dm-h-init json)
	(for-each (lambda (key)
		(define tv (dm-h-ref target key))
		(define jv (dm-h-add! json
			(dm-key (value->string (dm-dec key)))
		))
		(dm-h-init jv)
		(dm-enc! (dm-add-at! jv ":{$name") (dm-at tv @name))
		(dm-enc! (dm-add-at! jv ":{$description") (dm-at tv @description))
	) (dm-h-keys target))
)
