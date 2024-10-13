; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; RVXA MVM init

(include "RCOM/common")

; ATDBs
(atdb-bind (list
	(atdb-load "RVXA/AutoTiles" "RVXA/AutoTileRules")
	(atdb-load "RVXA/WallAT" "$WallATs$")
	(atdb-load "RVXA/WaterfallAT")
))

(sdb-load-old "RVXA/Schema")
(sdb-load-old "RVXA/SchemaCommandHelpers")
(sdb-load-old "RCOM/SchemaScript")

(cmdb-init "event")
(cmdb-load-old "event" "RVXA/Commands")

(cmdb-init "move")
(cmdb-load-old "move" "RVXA/CommandsMove")

(sdb-load-old "RVXA/SchemaEditing")
(sdb-load-old "RVXA/SchemaFiles")

(sdb-load-old "RVXA/project_config")
