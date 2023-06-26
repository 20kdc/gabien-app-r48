; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; R2K MVM init

; The R2k/R2k3 Schema. It acts as both, it's your job to figure out what gets ignored or errors if on 2k.
; Things postfixed with _2k3 were marked as such in the file format information I got.
; _2KO means '2k only'
; _112 means '2k3 1.12'
; _EPL means 'EasyRPG Player'

; VM stuff
(include "R2KXPCOM/common")
(include "R2K/vm/general")
(include "R2K/vm/itemtext")

; General stuff
(sdb-load-old "R2K/SchemaGeneral.txt")

; Event scripts
(sdb-load-old "R2K/SchemaScripting.txt")
(sdb-load-old "R2K/SchemaScriptingKII.txt")
(sdb-load-old "R2K/SchemaScriptingSBGM.txt")
(sdb-load-old "R2K/SchemaScriptingMonolith.txt")
(sdb-load-old "R2K/SchemaScriptingMonolith2.txt")
(sdb-load-old "R2K/SchemaScriptingMonolith3.txt")

(cmdb-init "event")
(cmdb-load-old "event" "R2K/Commands.txt")

(cmdb-init "move")
(cmdb-load-old "move" "R2K/CommandsMove.txt")

(sdb-load-old "R2K/SchemaScriptingFooter.txt")

; The main files
(sdb-load-old "R2K/SchemaLMU.txt")
(sdb-load-old "R2K/SchemaLMT.txt")
(sdb-load-old "R2K/SchemaLDB.txt")
(sdb-load-old "R2K/SchemaLDBTerms.txt")
(sdb-load-old "R2K/SchemaLSD.txt")
