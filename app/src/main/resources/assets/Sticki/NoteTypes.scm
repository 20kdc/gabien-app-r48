
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(cmd 0 (@ ]0))
(d "A comment in the plan.")
(p text string)

(cmd 980 "Anti-Mass Spectrometer Test Fire" (? ]0 (": " (@ ]0))) (? ]1 (" on " (@ ]1))) (? ]2 (" and " (@ ]2))))
(d "Test samples using Anti-Mass Spectrometer testtextteststext")
(X ams_setting_host)

(cmd 985 "Enable big scary thing " (if-eq ]0 #t "now." "later."))
(d "This is a description.")
(p bigScaryThing boolean)

(cmd 990 "Need" ($ " to " ]0))
(d "Indicate that at this point you need to (non-optional)...")
(p text string)

(cmd 991 "Want" ($ " to " ]0))
(d "Indicate that at this point you want to (optional)...")
(p text string)

(cmd 992 "Nb." ($ " " ]0))
(d "A comment.")
(p text string)
(C groupBehavior messagebox 992)

(cmd 993 "Consider" ($ " " ]0) " (")
(d "Begins a consideration block.")
(p text string)
(i 0)
(I 1)
(C groupBehavior form 982 Add\ Alternative 994)

(cmd 982 ") Alternatively Via (")
(d "Suggests an alternative method of consideration.")
(i \-1)
(I 1)
(C groupBehavior expectHead 982 993)

(cmd 994 ")")
(d "Ends a consideration block.")
(i \-1)
(I 0)
(C groupBehavior expectHead 982 993)

(cmd 995 "Long-term Plan" (? ]1 (" " (@ ]0) " " (@ ]1 ltp_type))))
(d "This is a sub-plan which can run concurrently with the current plan, or be waited on.")
(p name string)
(p planType ltp_type)
(p plan Plan)

(cmd 996 "Long Note")
(d "This is a long note - it is stored via a zlib-deflated blob.")
(p desc string)
(p contents zlibBlobEditor)

(cmd 997 "Table" ($ " " ]0))
(d "This is a map of strings to other strings.")
(p of string)
(p table NoteTable)

(cmd 998 "Test JSONlike Disambiguation")
(d "Test for JSON-like disambiguation (Hash As Object) and other such stuff.")
(p object JLDV)