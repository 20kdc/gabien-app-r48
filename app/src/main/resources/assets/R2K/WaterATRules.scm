
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; .ANums
; 012
; 3 4
; 567

; .Tiles
; 0: Base
; 1: +
; 2: O
; 3: |
; 4; -

; .CIs
; 01
; 23

; Format:
; TTC (tile, corner) : T<implied true ANums>F<implied false ANums>

; Disable 47-49: we're not allowed to use them (issue #34)
(C disable \47 \48 \49)

; vertical

(obj 30 "T1F3")
(obj 31 "T1F4")
(obj 32 "T6F3")
(obj 33 "T6F4")

; horizontal

(obj 40 "T3F1")
(obj 41 "T4F1")
(obj 42 "T3F6")
(obj 43 "T4F6")

; corner nubs

(obj 10 "T31F0")
(obj 11 "T14F2")
(obj 12 "T36F5")
(obj 13 "T64F7")

; bases

(obj 0 "T301F")
(obj 1 "T124F")
(obj 2 "T356F")
(obj 3 "T476F")

; full corners

(obj 20 "TF31")
(obj 21 "TF14")
(obj 22 "TF36")
(obj 23 "TF64")
