
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(e eventType \0 Globefish \1 Isogin \2 Crab \3 Sleep \4 NiceStar \5 AttackStar \6 Dum \7 Carry \8 Juel \9 Ufo)
(e collisionType \0 Null \1 Block \2 Enemy \3 Event)

(: IkachanEvent)
(@ x int)
(@ y int)
(@ scriptId int)
(@ type eventType)
(@ tOX int)
(@ tOY int)
(+ internal_EPGD)
(@ status int)
(@ collisionType collisionType)

(> mapEvents hash int subwindow IkachanEvent)
(: IkachanMap)
(@ events subwindow mapEvents)
; The default palette can't be shipped "just in case"...
(+ table @data . . \3 \160 \120 \1)
(+ table @palette . . \3 \256 \1 \4)

(> File.Map IkachanMap)
