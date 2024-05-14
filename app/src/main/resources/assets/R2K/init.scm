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
(sdb-load-old "R2K/SchemaGeneral")

; Event scripts
(sdb-load-old "R2K/SchemaScripting")
(sdb-load-old "R2K/SchemaScriptingKII")
(sdb-load-old "R2K/SchemaScriptingSBGM")
(sdb-load-old "R2K/SchemaScriptingMonolith")
(sdb-load-old "R2K/SchemaScriptingMonolith2")
(sdb-load-old "R2K/SchemaScriptingMonolith3")

(cmdb-init "event")
(cmdb-load-old "event" "R2K/Commands")

(cmdb-init "move")
(cmdb-load-old "move" "R2K/CommandsMove")

(sdb-load-old "R2K/SchemaScriptingFooter")

; The main files
(sdb-load-old "R2K/SchemaLMU")
(sdb-load-old "R2K/SchemaLMT")
(sdb-load-old "R2K/SchemaLDB")
(sdb-load-old "R2K/SchemaLDBTerms")
(sdb-load-old "R2K/SchemaLSD")
