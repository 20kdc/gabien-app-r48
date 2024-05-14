
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Common CommandsMove stuff between XP and VX

(obj 1 "\"Move South\"")
(d Walk, or maybe glide, in the direction clearly stated.)
(obj 2 "\"Move West\"")
(d Walk, or maybe glide, in the direction clearly stated.)
(obj 3 "\"Move East\"")
(d Walk, or maybe glide, in the direction clearly stated.)
(obj 4 "\"Move North\"")
(d Walk, or maybe glide, in the direction clearly stated.)

(obj 5 "\"Move South-West\"")
(d Walk, or maybe glide, in the direction clearly stated.)
(obj 6 "\"Move South-East\"")
(d Walk, or maybe glide, in the direction clearly stated.)
(obj 7 "\"Move North-West\"")
(d Walk, or maybe glide, in the direction clearly stated.)
(obj 8 "\"Move North-East\"")
(d Walk, or maybe glide, in the direction clearly stated.)

(obj 9 "\"Move randomly\"")
(d Walk, or maybe glide, in a random direction.)

(obj 10 "\"Move towards Player\"")
(d Walk, or maybe glide, in a direction that\'s more or less player-oriented.)

(obj 11 "\"Move away from Player\"")
(d Walk, or maybe glide, in a direction that\'s more or less NOT player-oriented.)

(obj 12 "\"Step forward\"")
(d Walk, or maybe glide, forward.)

(obj 13 "\"Step backward\"")
(d Walk, or maybe glide, backward.)

(obj 14 "\"Jump by\" ($ \" \" ]0) ($ \",\" ]1)")
(d Jump somewhere. Gliding is boring.)
(p ofsX int)
(p ofsY int)

(obj 15 "\"Wait\" ($ \" \" ]0) \" frames\"")
(d Wait some time \(in frames\).)
(p frames int)

(obj 16 "\"Turn South\"")
(d Turn to the direction indicated.)
(obj 17 "\"Turn West\"")
(d Turn to the direction indicated.)
(obj 18 "\"Turn East\"")
(d Turn to the direction indicated.)
(obj 19 "\"Turn North\"")
(d Turn to the direction indicated.)

(obj 20 "\"Turn right (clockwise)\"")
(d Turn clockwise, \90 degrees.)
(obj 21 "\"Turn left (anti-clockwise)\"")
(d Turn anti-clockwise, \90 degrees.)

(obj 22 "\"Turn 180-degrees around\"")
(d Turn to the opposite direction.)
(obj 23 "\"Turn left or right (random)\"")
(d Turn clockwise or anti-clockwise by \90 degrees.)

(obj 24 "\"Turn to random direction\"")
(d Turn to a random cardinal direction.)
(obj 25 "\"Turn towards player\"")
(d Turn to face the player.)
(obj 26 "\"Turn away from player\"")
(d Turn to avoid facing the player.)

(obj 27 "\"Switch\" ($ \" \" ]0 switch_id) \" on\"")
(d Turns a switch on.)
(p switch switch_id)
(obj 28 "\"Switch\" ($ \" \" ]0 switch_id) \" off\"")
(d Turns a switch off.)
(p switch switch_id)

(obj 29 "\"Set speed\" ($ \": \" ]0)")
(d Changes the @move_speed.)
(p newSpeed int)
(obj 30 "\"Set freq.\" ($ \": \" ]0)")
(d Changes the @move_freq.)
(p newFreq int)

(obj 31 "\"Enable walk animation\"")
(d ...)
(obj 32 "\"Disable walk animation\"")
(d ...)
(obj 33 "\"Enable step animation\"")
(d ...)
(obj 34 "\"Disable step animation\"")
(d ...)
(obj 35 "\"Enable fixed direction\"")
(d ...)
(obj 36 "\"Disable fixed direction\"")
(d ...)

(obj 37 "\"Disable collision\"")
(d Disables collision, allowing the character to walk through walls.)
(obj 38 "\"Enable collision\"")
(d Enables collision, preventing the character from walking through walls.)

; ...

(obj 45 "\"Ruby\" ($ \" \" ]0)")
(d Executes some Ruby code.)
(p code string)
