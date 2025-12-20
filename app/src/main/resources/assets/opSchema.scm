; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(. opcfg_ctx_array_bounds_inner)
(+ optP @ctx_array_start int)
(+ optP @ctx_array_end int)

(. opcfg_ctx_array_bounds)
(+ hide opcfg_ctx_array_bounds_inner)

(: R48::OpCfg::r48core_test_operator)
(@ test1 int)
(+ opcfg_ctx_array_bounds_inner)

(e R48::OpCfg::r48core_rmtextmanip.mode
	0 "Align Left"
	1 "Align Centre"
	2 "Align Right"
)

(: R48::OpCfg::r48core_rmtextmanip)
(@ mode R48::OpCfg::r48core_rmtextmanip.mode)
(@ ignore_first int_boolean)
(+ optP @wrap_to_limit int= 50)
(+ opcfg_ctx_array_bounds)
