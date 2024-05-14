
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Movement commands for VX Ace.

(obj 0 "\"Terminate\"")
(d Do I really need to describe this?)
(L)

(\# RCOM/CommonMove)

(obj 39 "\"Enable transparency\"")
(d I would assume *translucency* is the correct*GACK!*)
(obj 40 "\"Disable transparency\"")
(d ...)

(obj 41 "\"Set graphic\" ($ \" to \" ]0) ($ \" \" ]1)")
(d Change the sprites of the character.)
(p characterName f_char_name)
(C spritesheet \0 Characters/)
(p characterIndex int)

(obj 42 "\"Set opacity\" ($ \" to \" ]0 opacity)")
(d Change the opacity of the character.)
(p opacity opacity)

(obj 43 "\"Change blend_type\" ($ \" to \" ]0 blend_type)")
(d Change the blend type \(how translucency interacts with the environment\) of the character.)
(p blendType blend_type)

(obj 44 "\"Play SE\" ($ \" \" ]0)")
(d Play a sound effect.)
(p se RPG::SE)
