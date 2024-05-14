
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; For further documentation on this, see IRB_ATRules.txt

; We shouldn't use these.

(C disable \47 \48 \49)

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

; Just some basic rules for tiles 0, 2 and 7.
; 1. Tile 0 always means that the orthogonal directions it's pointing at aren't connected.
;    (Diagonal directions are ignored, for obvious enough reasons.)
; 2. Tile 2 means that the orthogonal directions *are* connected,
;     and furthermore, due to the "notch" it is certain the diagonal is NOT connected.
; 3. Tile 7 means both orthogonals and diagonal are connected.

(obj 0 "TF13")
(obj 1 "TF14")
(obj 2 "TF36")
(obj 3 "TF46")

(obj 20 "T13F0")
(obj 21 "T14F2")
(obj 22 "T36F5")
(obj 23 "T46F7")

(obj 70 "T103F")
(obj 71 "T124F")
(obj 72 "T356F")
(obj 73 "T476F")

; The next two rule sets are the main rules to handle tiles 3,4,5,6,8,9,10,11.
;  The rules on orthogonal (4, 6, 8, 10) tiles are:
;  1. The 2 outward-facing corners of an orthogonal tile cannot connect in the direction it "faces".
;  2. The 2 outward-facing corners of an orthogonal tile indicate connection *directly adjacent to this corner*.

;     To emphasize:
;     1234
;     5678

;     Let's just assume this is a block of AT corners, the left AT being a left cap,
;      and the right AT being a right cap.
;     In this case, 2, 3, 6 and 7 meet the criteria.
;     Working backwards to reach the rules:
;      In order for 2 to be valid, there must be an AT to it's right. To it's left is irrelevant to 2.
;      In order for 3 to be valid, there must be an AT to it's left. To it's right is irrelevant to 3.
;       (Rule 1.)
;      Furthermore, there cannot be anything connecting above 2 for 2 to be valid,
;       or anything connecting above 3 for 3 to be valid. (Rule 2.)
;      The same applies for 6 and 7.

; The rules on diagonal tiles (3, 5, 9, 11), for the outward-facing corner,
;  are the rules for 0.
; You can see from examination that 030 matches 000, and that 051 matches 001.
; For the inward-facing corner on those diagonal tiles, they must be swamped -
;  the rules are the same as those for tile 7, only in a different order.

; With the default AutoTiles.txt file, no other rules are needed as far as I can tell -
;  these rules resolve perfectly well to the correct tiles.

(obj 30 "TF13")
(obj 33 "T476F")

(obj 40 "T3F1")
(obj 41 "T4F1")

(obj 51 "TF14")
(obj 52 "T356F")

(obj 60 "T1F3")
(obj 62 "T6F3")

; ---

(obj 81 "T1F4")
(obj 83 "T6F4")

(obj 92 "TF36")
(obj 91 "T124F")

(obj 102 "T3F6")
(obj 103 "T4F6")

(obj 113 "TF46")
(obj 110 "T301F")
