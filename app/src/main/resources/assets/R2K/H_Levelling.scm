
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(obj 0 "Levelling")
; EasyRPG had the formula, but... my goodness, is it long.
(. RM2000: inflate starts out at \(1.5 + \(exp_mul * \0.01\)\), base is exp. for each level, add \(correction + base\), then perform base *= inflate.)
(. Then change inflate to \(\(\(LVL * \0.002\) + \0.802\) * \(inflate \- \1\)\) + \1, and run for the next level until done. Levels are \1 to LVL. My goodness this is long.)
(. RM2003: \(exp * LVL\) + \(factorial\(LVL\) * exp_mul\) + \(exp_add * LVL\))
