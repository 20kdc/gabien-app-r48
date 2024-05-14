
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; R2k Passability data include file

;  FL 1down 2left 4right 8up
;  PX up right down left


; using 'l 0 0' disables counter

;  F- AX AY BX BY X- Y- W- H-
(p \64 \16 \16 \16 \24 \24 \24 \8 \5)
(l)

;  F- AX AY BX BY X- Y- W- H-
(p \8 \0 \0 \0 \8 \12 \2 \8 \8)
(p \1 \16 \0 \16 \8 \12 \22 \8 \8)
(p \2 \24 \0 \24 \8 \2 \12 \8 \8)
(p \4 \8 \0 \8 \8 \22 \12 \8 \8)

(p \16 \0 \16 \0 \24 \0 \24 \8 \8)

(p \32 \16 \20 \16 \28 \24 \28 \8 \4)

; Allow room for the flags
(X \32 \32)
; No hex
(z)

(x \15)
