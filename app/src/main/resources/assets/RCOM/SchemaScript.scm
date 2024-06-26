
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(> genericScriptParameters array 0 genericScriptParameter)
(> genericScriptParametersSW subwindow genericScriptParameters)

; Common enums which are always the same
(e direction_ 2 down 4 left 6 right 8 up)
(> direction ui direction_ halfsplit halfsplit valButton 2 ⬇ valButton 4 ⬅ halfsplit valButton 6 ⮕ valButton 8 ⬆)
(e direction_disable 0 noChange 2 down 4 left 6 right 8 up)

; Note: The default code is 115 so that when created,
;        it won't be /immediately be zapped/ by the autocorrect.
;       The default code can't be anything with parameters,
;        since that autocorrect is on the array level,
;        and the autocorrect *is not called on command interiors from the array.
;       Furthermore, the parameters to this class,
;        it being the schema used solely for creation and autocorrection,
;        does not make any assumptions about the parameters.
;       This prevents potential data loss due to overzealous autocorrect.
;       genericScriptParameters could be incomplete, for example,
;        and the autocorrect would happily wipe it over if that were used.
;       So it's OPAQUE, unless a better solution
;        (that doesn't have data-wiping potential) is put in.

(: RPG::EventCommand)
(@ code int= 115)
(@ indent int)
(@ parameters array 0 OPAQUE)

(. EventCommandEditor_Most)
(@ indent indent)
(@ parameters genericScriptParametersSW)

(: RPG::MoveCommand)
(@ code int)
(@ parameters array 0 OPAQUE)

(. MoveCommandEditor_Most)
(@ parameters genericScriptParametersSW)

; Stuff that doesn't go in CommonConditionals but would if it was just called "CommonCommandStuff"

(vm
	(define-name transfer_player_paramassist
		"Transfer player"
		(if-eq ]0 0
			(" to " (@ ]1 map_id #t) "[" (@ ]2) "," (@ ]3) "]")
			(" by vars " (@ ]1 var_id #t) "[" (@ ]2 var_id #t) "," (@ ]3 var_id #t) "]")
		)
		" dir." (@ ]4 direction_disable)
		", fade " (@ ]5 transfer_player_fadetype)
	)
)

; NOTE: The CMDB stuff for this is:
; 201:(? ]0 (@ ]0 transfer_player_paramassist) "Transfer Player")
; p useVars int_boolean
; P 0 mapVar var_id
; v 0 map map_id
; P 0 xVar var_id
; v 0 x int
; P 0 yVar var_id
; v 0 y int
; p playerDir direction_disable
; p fadeType transfer_player_fadetype
; x transfer_player_paramassist

(. transfer_player_paramassist)
(@ indent indent)
(@ parameters DA{ ]0 transfer_player_vars 0 transfer_player_novars })

(. transfer_player_vars)
(] 0 targetVars int_boolean)
(] 1 mapVar var_id)
(] 2 xVar var_id)
(] 3 yVar var_id)
(] 4 playerDir direction_disable)
(] 5 fadeType transfer_player_fadetype)

(. transfer_player_novars)
(] 0 targetVars int_boolean)
(] 1 map map_id)
(] 2 x int)
(] 3 y int)
(] 4 playerDir direction_disable)
(] 5 fadeType transfer_player_fadetype)
(+ mapPositionHelper ]1 ]2 ]3)

; --

(e set_event_location_type 0 ints 1 vars 2 swap)

(. set_event_location_header)
(] 0 event character_id)
(] 4 dir direction_disable)
(] 1 selType set_event_location_type)

(. set_event_location_paramassist)
(vm
	(define-name set_event_location_paramassist
		"Set ev." (@ ]0 character_id #t) " to "
		(= ]1 ""
			(0 "loc." (@ ]2) "," (@ ]3))
			(1 "loc.vars." (@ ]2 var_id #t) "," (@ ]3 var_id #t))
			(2 "swap with " (@ ]2 character_id #t))
		)
		", dir " (@ ]4 direction_disable)
	)
)
(@ indent indent)
(@ parameters DA{ ]1 set_event_location_header 0 set_event_location_a 1 set_event_location_b 2 set_event_location_c })

(. set_event_location_a)
(+ set_event_location_header)
(] 2 x int)
(] 3 y int)

(. set_event_location_b)
(+ set_event_location_header)
(] 2 xVar var_id)
(] 3 yVar var_id)

(. set_event_location_c)
(+ set_event_location_header)
(] 2 targ character_id)

; --

; Now for operate_value stuff.
; This has to go and use Scheme because of the dynamic indexing.

(vm
	(define-tr TrNStr.operate_value_negator (fl1 "negate(" a0 ")"))
	(define-name operate_value_var "v." (@ : var_id #t))
	(define-name operate_value_int (@ :))
	(define-tr TrNStr.operate_value_innards (lambda (base i1 i2)
		(if (equal? (dm-dec (dm-a-ref base i1)) 1)
			(TrName.operate_value_var (dm-a-ref base i2))
			(TrName.operate_value_int (dm-a-ref base i2))
		)
	))
	(define-tr TrNStr.operate_value (lambda (base i0 i1 i2)
		(if (equal? (dm-dec (dm-a-ref base i0)) 1)
			(TrNStr.operate_value_negator (TrNStr.operate_value_innards base i1 i2))
			(TrNStr.operate_value_innards base i1 i2)
		)
	))

	(define-name operate_value_0 (? ]2 (vm (TrNStr.operate_value a0 0 1 2))))
	(define-name operate_value_1 (? ]3 (vm (TrNStr.operate_value a0 1 2 3))))
	(define-name operate_value_2 (? ]4 (vm (TrNStr.operate_value a0 2 3 4))))
	(define-name operate_value_3 (? ]5 (vm (TrNStr.operate_value a0 3 4 5))))
)

; choice_array, used for choice formatting.
; Notably this is solely used as an interpretation.

(vm
	(define-name choice_array
		(? ]0 (
			(@ ]0)
		))
		(? ]1 ( ","
			(@ ]1)
		))
		(? ]2 ( ","
			(@ ]2)
		))
		(? ]3 "...")
	)
)

; This timer thing.

(. onoff_timer_parameters)
(@ indent indent)
(@ parameters DA{ ]0 onoff_timer_stop 0 onoff_timer_start })

(. onoff_timer_start)
(] 0 stop int_boolean)
(] 1 startSecs int)

(. onoff_timer_stop)
(] 0 stop int_boolean)
