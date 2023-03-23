; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; RVXA MVM init

(sdb-load-old "RVXA/Schema.txt")
(sdb-load-old "RVXA/SchemaCommandHelpers.txt")
(sdb-load-old "RCOM/SchemaScript.txt")

(cmdb-init "event")
(cmdb-load-old "event" "RVXA/Commands.txt")

(cmdb-init "move")
(cmdb-load-old "move" "RVXA/CommandsMove.txt")

(sdb-load-old "RVXA/SchemaEditing.txt")
(sdb-load-old "RVXA/SchemaFiles.txt")
