; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; RXP MVM init

(include "RCOM/common")

; ATDBs
(atdb-bind (list
	(atdb-load "RXP/AutoTiles" "R2KXPCOM/AutoTileRules")
))

(sdb-load-old "RXP/Schema")

; Everything needed for map editing.
(sdb-load-old "RCOM/SchemaScript")

(sdb-load-old "RXP/SchemaCommandHelpers")

(cmdb-init "event")
(cmdb-load-old "event" "RXP/Commands")

(cmdb-init "move")
(cmdb-load-old "move" "RXP/CommandsMove")

(sdb-load-old "RXP/SchemaEditing")

; Defining 'non-essential' bits & pieces, that the system could conceivably survive without.
; Though in some cases they may be used to attempt to create stand-ins for missing files,
;  and in this case they are required.

(sdb-load-old "RXP/SchemaFiles")

(sdb-load-old "RXP/ProjectConfig")
