
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(\# tileAttributes32.png)
(X \48 \48)

(> RXP/TSTables.txt)

;  base
;  F-   I0 I1
(P \0x00 \0 \0)
(P \0x00 \1 \1)

;  F-   I0 I1
(P \0x01 \6 \-1)
(P \0x02 \7 \-1)
(P \0x04 \9 \-1)
(P \0x08 \8 \-1)

;  additional
;  F-   I0 I1
(P \0x40 \-1 \4)
(P \0x80 \-1 \5)

; No hex
(z)

(x \15)
