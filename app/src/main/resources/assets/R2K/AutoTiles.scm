
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; AutotileDB

;  R2K VERSION.
;  R2K actually uses 50-long AT fields.

;  A | |B
;  --+-+--
;  UL|U|UR
;  --+-+--
;  L |C|R
;  --+-+--
;  LL|D|LR

(obj 0 "Swamped tile")
(d C C)
(D C C)
(obj 1 "UL inner corner")
(d B C)
(D C C)
(obj 2 "UR inner corner")
(d C B)
(D C C)
(obj 3 "Outward branch U")
(d B B)
(D C C)
(obj 4 "LR inner corner")
(d C C)
(D C B)
(obj 5 "Almost-swamped ex. UL/LR")
(d B C)
(D C B)
(obj 6 "Outward branch R")
(d C B)
(D C B)
(obj 7 "Almost-swamped ex. LL")
;   (guesswork via ATP eyeballing)
;   I mean, just look at it.
(d B B)
(D C B)
(obj 8 "LL inner corner")
(d C C)
(D B C)
(obj 9 "Outward branch L")
(d B C)
(D B C)
(obj 10 "Almost-swamped ex. UR/LL")
(d C B)
(D B C)
(obj 11 "Almost-unswamped ex. LR & Ortho")
;   (guesswork via ATP eyeballing)
(d B B)
(D B C)
(obj 12 "Outward branch D")
(d C C)
(D B B)
(obj 13 "Almost-unswamped ex. UR & Ortho")
;   (guesswork via ATP eyeballing)
(d B C)
(D B B)
(obj 14 "Almost-unswamped ex. UR & Ortho")
;   (guesswork via ATP eyeballing)
(d C B)
(D B B)
(obj 15 "Almost-unswamped ex. Ortho")
;   (guesswork via ATP eyeballing)
(d B B)
(D B B)
(obj 16 "Left edge, general case.")
(d L L)
(D L L)
(obj 17 "Left edge, specific case:")
;    .#.
;    .X#
;    .##
;    (so probably "Left edge, UR diagonal missing")
(d L B)
(D L L)
(obj 18 "Left edge, specific case:")
;    .##
;    .X#
;    .#.
;    (so probably "Left edge, LR diagonal missing")
(d L L)
(D L B)
(obj 19 "Left edge, Tjunction")
;   (guesswork via ATP eyeballing)
(d L B)
(D L B)
(obj 20 "Upper edge, general case.")
(d U U)
(D U U)
(obj 21 "Upper edge, specific case:")
;    ...
;    #X#
;    ##.
;    (so probably "Upper edge, LR diagonal missing")
(d U U)
(D U B)
(obj 22 "Upper edge, specific case:")
;    ...
;    #X#
;    .##
;    (so probably "Upper edge, LL diagonal missing")
(d U U)
(D B U)
(obj 23 "Upper edge, Tjunction")
(d U U)
(D B B)
(obj 24 "Right edge, general case.")
(d R R)
(D R R)
(obj 25 "Right edge, specific case:")
;    ##.
;    #X.
;    .#.
;    (so probably "Right edge, LL diagonal missing")
(d R R)
(D B R)
(obj 26 "Right edge, specific case:")
;    .#.
;    #X.
;    ##.
;    (so probably "Right edge, UL diagonal missing")
(d B R)
(D R R)
(obj 27 "Right edge, Tjunction")
;   (guesswork via ATP eyeballing)
(d B R)
(D B R)
(obj 28 "Lower edge, general case.")
;    ###
;    #X#
;    ...
(d D D)
(D D D)
(obj 29 "Lower edge, specific case:")
;    .##
;    #X#
;    ...
;    (so probably "Lower edge, UL diagonal missing")
(d B D)
(D D D)
(obj 30 "Lower edge, specific case:")
;    ##.
;    #X#
;    ...
;    (so probably "Lower edge, UR diagonal missing")
(d D B)
(D D D)
(obj 31 "Lower edge, Tjunction")
;   (guesswork via ATP eyeballing)
(d B B)
(D D D)
(obj 32 "U/D (diagonals are ignored)")
(d L R)
(D L R)
(obj 33 "L/R (diagonals are ignored)")
(d U U)
(D D D)
(obj 34 "'full' UL corner (LR diag, D&R ortho)")
(d UL UL)
(D UL UL)
(obj 35 "UL corner, only lower and right ortho (no diag.)")
(d UL UL)
(D UL B)
(obj 36 "'full' UR corner (LL diag, D&L ortho)")
(d UR UR)
(D UR UR)
(obj 37 "UR corner, only lower and left ortho (no diag.)")
(d UR UR)
(D B UR)
(obj 38 "'full' LR corner (UL diag, U&L ortho)")
(d LR LR)
(D LR LR)
(obj 39 "LR corner, only upper and left ortho (no diag.)")
(d B LR)
(D LR LR)
(obj 40 "'full' LL corner (UR diag, U&R ortho)")
(d LL LL)
(D LL LL)
(obj 41 "LL corner, only upper and right ortho (no diag.)")
(d LL B)
(D LL LL)
(obj 42 "Down orthogonal (diagonals are ignored)")
(d UL UR)
(D UL UR)
(obj 43 "Right orthogonal (diagonals are ignored)")
(d UL UL)
(D LL LL)
(obj 44 "Up orthogonal (diagonals are ignored)")
(d LL LR)
(D LL LR)
(obj 45 "Left orthogonal (diagonals are ignored)")
(d UR UR)
(D LR LR)
(obj 46 "Lone tile type A")
(d UL UR)
(D LL LR)

(obj 47 "R2K Dummy 1")
(d C C)
(D C C)

(obj 48 "R2K Dummy 2")
(d C C)
(D C C)

(obj 49 "Lone tile type B (R2K ADJUSTED)")
(d A A)
(D A A)
