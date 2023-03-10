; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Schema terms

(define-group TrSchema
	(enum_
		id "ID."
		int "Integer"
		sym "Symbol"
		code "Code"
	)
	(cmdb_
		unkParamName "UNK."
		defCatName "Commands"
	)
	bFileBrowser (fl1 "Browse " a0 "...")
	bOpenTable "Open Table..."
	selectTileGraphic "Select Tile Graphic..."
	(ppp_
		constant "Constant"
		idVar "From Id Var. (PPP/EasyRPG/2k3 1.12)"
		idNSfx "From Id/Name Suffix Var. Pair (PPP/EasyRPG/2k3 1.12)"
		idVarFN "idVar"
		idFN "id "
		typeFN "type "
		explain "Explain this picture mode..."
		valueVarFN "valueVar "
		isVarFN "isVar "
	)
	aElmInv "(This index isn't valid - did you modify a group from another window?)"
	aElmOpt (fl2 "Field " a0 " doesn't exist (default " a1 ")")
)
