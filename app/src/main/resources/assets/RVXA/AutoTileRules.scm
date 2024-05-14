
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; For information on the format, see the R2KXPCOM version of this file.

; We shouldn't use these:

(C disable \47 \48 \49)

; Just some basic rules for tiles 0, 2 and 7.
; 1. Tile 0 always means that the orthogonal directions it's pointing at aren't connected.
;    (Diagonal directions are ignored, for obvious enough reasons.)
; 2. Tile 2 means that the orthogonal directions *are* connected,
;     and furthermore, due to the "notch" it is certain the diagonal is NOT connected.
; 3. Tile 7 means both orthogonals and diagonal are connected.

(obj 0 "TF1364")
(obj 1 "TF1364")
(obj 2 "TF1364")
(obj 3 "TF1364")

(obj 20 "T13F0")
(obj 21 "T14F2")
(obj 22 "T36F5")
(obj 23 "T46F7")

; .ANums
; 012
; 3 4
; 567

; .Tiles
; Cr Cut.
; 0.2
; 345
; 678
; 9**

; .CIs
; 01
; 23

(obj 30 "TF13")
(obj 33 "T476F")

(obj 51 "TF14")
(obj 52 "T356F")

; .ANums
; 012
; 3 4
; 567

; .Tiles
; Cr Cut.
; 0.2
; 345
; 678
; 9**

; .CIs
; 01
; 23

(obj 92 "TF36")
(obj 91 "T124F")

(obj 113 "TF46")
(obj 110 "T301F")


; VX-Ace Straight-Line Rules

; .ANums
; 012
; 3 4
; 567

; .Tiles
; Cr Cut.
; 0.2
; 3X5
; XXX
; 9X*

; .CIs
; 01
; 23

; Format is CCI.

(obj 90 "TF3")
(obj 93 "TF6")

(obj 111 "TF4")
(obj 112 "TF6")

(obj 31 "TF1")
(obj 32 "TF3")

(obj 50 "TF1")
(obj 53 "TF4")
