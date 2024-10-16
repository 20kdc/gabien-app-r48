
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; [Interpreter Part 3]

(cmd 101 "Say" ($ " " ]0))
(C category 0)
(d "Begins showing a message.")
(C tag translatable sayCmd)
(C textArg 0)
(p text textbox_string)
(C groupBehavior messagebox 401)

(cmd 401 "Say (cont.)" ($ " " ]0))
(C category 0)
(d "Continues a message.")
(C tag translatable sayCmd)
(C commandSiteAllowed false)
(C textArg 0)
(p text textbox_string)

; Choices and input moved to RCOM/CommonCommands

(cmd 104 "Change Text Options" ($ ": " ]0 change_text_options_position) (? ]1 ", " (if-eq ]1 0 "visible bkg." "invisible bkg.")))
(C category 0)
(d "Changes the settings used to display text.")
(p position change_text_options_position)
(p frameTransparent int_boolean)

(cmd 105 "Button Input" ($ " to " ]0 var_id #t))
(C category 5)
(d "Stores a pressed button in a variable.")
(p varId var_id)

(cmd 106 "Wait" (? ]0 (" " (@ ]0) " frames")))
(C category 5)
(d "Waits some amount of time.")
(p time int)

; conditional branch (111/411/412) and loops (112/113/413) and EEP 115 in CommonCommands

; [A perfectly ordinary gap.]

(cmd 116 "Temp. Erase Calling Event")
(C category 3)
(d "Erases the running event until next map load.")

; CCE 117, LBL 118, JMP 119 in CommonCommands