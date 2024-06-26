
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(> NoteTable subwindow hash string string)

(s ltp_type concurrent waitOn)

(vm
	(define-name ltp_type
		(= :
			("Unknown " (@ :))
			(concurrent "Concurrent")
			(waitOn "Wait On...")
		)
	)
)

(. ams_setting_host)
(@ indent indent)
(@ parameters ams_setting)

; Complex array type for testing KII mechanics
; Notably this does "iffy" stuff like use a DA when no array exists, for testing purposes -
;  because reducing the amount of ways a developer can end up having an "oversight" that screws up the whole thing...
; That's important.

(. ams_setting)
(+ lengthAdjust Single \2)
(+ lengthAdjust Range \3)
(+ DA{ :length ams_setting_unk \2 ams_setting_single \3 ams_setting_range })
(. ams_setting_unk)
(] \0 testID string)
(. ams_setting_single)
(] \0 testID string)
(] \1 target int)
(. ams_setting_range)
(] \0 testID string)
(] \1 first int)
(] \2 last int)

; JSONDisamb.

(obj 0 "Cat")
(@ meow boolean)

(obj 1 "Mouse")
(@ squeak boolean)

(obj 2 "Dog")
(@ woof boolean)

(: JLDV)
(@ a1 JLDV1)
(@ a2 JLDV2)
(@ a3 JLDV3)
(@ a4 JLDV4)

(: JLDV1)
(@ test_cmdbuf_light test_cmdbuf_type)
(+ flushCommandBuffer @test_cmdbuf_light test_cmdbuf_type)

(C md $Woof!\ Bark!)
(@ dog boolean)
(C md $Meow!\ Purr... I\ have\ a\ custom\ name!)
(@ cat boolean)
(C md $Squeek!)
(@ mouse boolean)

(: JLDV2)
(@ test_cmdbuf_heavy test_cmdbuf2_type)
(+ flushCommandBufferStr @test_cmdbuf_heavy test_cmdbuf2_type)

(. JLDV3)
(+ subwindow: Raw hash string string)
(+ hashObject $Core1 $Core2)
(+ path :{$Core1 string)
(+ path :{$Core2 string)

(. JLDV4)
(+ subwindow: Raw hash int string)
(+ hashObject \23)
(+ path :{23 string)

; Footer

(. NoteEditor_Most)
(@ indent indent)
(@ parameters array \0 OPAQUE)

(: Note)
(@ code int)
(@ indent indent)
(@ parameters array \0 OPAQUE)

(> Plan subwindow NoteList)

(> NoteList arrayCS Note NoteEditor_Most notes)
(> File.Notes NoteList)
