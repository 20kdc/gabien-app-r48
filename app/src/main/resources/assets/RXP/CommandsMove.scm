
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; MoveCommands stuff.

(obj 0 "\">> insert point\"")
(d Mostly harmless.)
(L)

(\# RCOM/CommonMove.txt)

; I am now very glad I reused the EventCommand code for this

(obj 39 "\"Ignore Z Depth On\"")
(d ...)
(obj 40 "\"Ignore Z Depth Off\"")
(d ...)

(obj 41 "\"Set graphic\" ($ \" to \" ]0) ($ \":\" ]3) ($ \", facing \" ]2 direction)")
(d Changes the graphics of this event.)
(p name f_char_name)
(p hue hue)
(p dir direction)
(C xpMoveCommandSetGraphic)
(p pattern rpg_event_page_graphic_pattern)

(obj 42 "\"Set opacity\" ($ \" to \" ]0 opacity)")
(d Changes the opacity of this event.)
(p opacity opacity)

(obj 43 "\"Change blend_type\" ($ \" to \" ]0 blend_type)")
(d Changes the @blend_type of this event.)
(p blendType blend_type)

(obj 44 "\"Play SE\" ($ \" \" ]0)")
(d Plays a sound effect.)
(p audio rpg_audiofile_se)
