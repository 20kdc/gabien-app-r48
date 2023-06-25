; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Common macros/etc.

; (rcom-idname CSYM ENG): Defines a name for the given Ruby class that shows the @id and @name
(define-syntax (rcom-idname csym eng)
	(list
		define-name
		(string->symbol (.. "Class." (symbol->string csym)))
		eng
		" "
		'(@ @id)
		": "
		'(@ @name)
	)
)
