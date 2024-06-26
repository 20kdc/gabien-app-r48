
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; RPG Maker VX Ace DropItem handling.
; But also Action.
; In general, complicated stuff in the ::Enemy namespace.

(obj 0 "No Drop")
(@ data_id int)

(C defaultCB)
(@ data_id int)

(obj 1 "Item")
(@ data_id item_id)
(obj 2 "Weapons")
(@ data_id weapon_id)
(obj 3 "Armour")
(@ data_id armour_id)

(: RPG::Enemy::DropItem)
(@ kind dropitem_kind)
; Notably the command buffer is global to the file.
; This command clears it, sets up a node with it in mind (Don't use anywhere where vertical size matters!!!),
;  and creates an enum.
; Only missing thing is the candle on the birthday cake it already delivered to you.
; ...knock knock.
(+ flushCommandBuffer @kind dropitem_kind)
(@ denominator int)

(C defaultCB)
(@ condition_param1 typeChanger{ int i float f })
(@ condition_param2 typeChanger{ int i float f })

(obj 0 "noCondition")
(@ condition_param1 typeChanger{ int i float f })
(@ condition_param2 typeChanger{ int i float f })

(obj 1 "turnsPassed")
(@ condition_param1 int)
; typeChanger{ int i float f }
(@ condition_param2 int)
; typeChanger{ int i float f }

(obj 2 "checkHP")
(@ condition_param1 typeChanger{ int i float f })
(@ condition_param2 typeChanger{ int i float f })

(obj 3 "checkMP")
(@ condition_param1 typeChanger{ int i float f })
(@ condition_param2 typeChanger{ int i float f })

(obj 4 "checkState")
(@ condition_param1 state_id)
(@ condition_param2 typeChanger{ int i float f })

(obj 5 "highestPartyLevelAboveOrEq")
(@ condition_param1 typeChanger{ int i float f })
(@ condition_param2 typeChanger{ int i float f })

(obj 6 "switch")
(@ condition_param1 switch_id)
(@ condition_param2 typeChanger{ int i float f })

(: RPG::Enemy::Action)
(@ rating int)
(@ skill_id skill_id)
(@ condition_type condition_type_code)
(+ hwnd @condition_type RVXA/BatActions)
(+ flushCommandBuffer @condition_type condition_type_code)
