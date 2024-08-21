; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; MiniVM app context additions

(include "vm/trc")
(include "vm/trc-lib")

(define-syntax (fmt-at base path) (list dm-fmt (list dm-at base path)))
(help-set! fmt-at "(fmt-at BASE PATH) : (dm-fmt (dm-at BASE PATH))")

(define (ui-test-schema schema) (ui-view (root-new schema "TestObject") dp-empty))
(help-set! ui-test-schema "(ui-test-schema SCHEMA) : Show a new disconnected object with this schema.")
