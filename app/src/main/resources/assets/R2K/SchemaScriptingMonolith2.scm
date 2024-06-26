
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; ___
; | |
; | |
; --- 2
; the second monolith
; for picture commands only

(. show_picture_parameters)
(@ indent indent)
(@ parameters show_picture_array)

(. show_picture_array)
(+ lengthAdjust General\ Mode \15)
(+ lengthAdjust Old\ 2k3\ Mode \16)
(+ lengthAdjust \2k3\ 1.12\ Mode \31)
(+ DA{ :length show_picture_array_nbo \31 show_picture_array_nbn })
(+ DA{ ]2 comm_picture_array_head_yv \0 comm_picture_array_head_nv })
(+ comm_picture_array_foot)
(+ show_picture_array_foot)

(. move_picture_parameters)
(@ indent indent)
(@ parameters move_picture_array)

(. move_picture_array)
(+ lengthAdjust General\ Mode \17)
(+ lengthAdjust Old\ 2k3\ Mode \18)
(+ lengthAdjust \2k3\ 1.12\ Mode \23)
(+ DA{ :length move_picture_array_nbo \23 move_picture_array_nbn })
(+ DA{ ]2 comm_picture_array_head_yv \0 comm_picture_array_head_nv })
(+ comm_picture_array_foot)
(+ move_picture_array_foot)

; ---

(. erase_picture_parameters)
(@ indent indent)
(@ parameters erase_picture_array)

(e erase_picture_range \0 const/ppp \1 var \2 range)
(. erase_picture_array)
(] \0 _ string)
; can't depend on ]2 for pictureId if it isn't around (will cause disambiguator failures)
(+ DA{ :length erase_picture_array_nc \3 erase_picture_array_wc \4 erase_picture_array_wc })
(. erase_picture_array_nc)
; Notably, setting this flips it into WC
(+ ]?2 choiceType_112 const/ppp erase_picture_range)
(] \1 pictureId picture_id)
(. erase_picture_array_wc)
(+ ]?2 choiceType_112 const/ppp erase_picture_range)
(+ DA{ ]2 ]1 pictureId picture_id \1 ]1 pictureVarId var_id })
; I don't know if it actually crashes, consider this a motivation NOT to mess it up
(+ DA{ ]2 ]3 _ int \2 ]3 rangeLast_112 picture_id })

; --- Components

(e comm_picture_effectmode \0 none \1 rotate \2 waver)

(. comm_picture_array_head_yv)
(] \2 posVars int_boolean)
(] \3 xVar var_id)
(] \4 yVar var_id)

(. comm_picture_array_head_nv)
(] \2 posVars int_boolean)
(] \3 x int)
(] \4 y int)

(. comm_picture_array_foot)
(] \5 mapAttach int_boolean)
; this is basically a guess
(] \8 maskColour0 int_boolean)
(] \9 red% percent)
(] \10 green% percent)
(] \11 blue% percent)
(] \12 saturation% percent)
(+ r2kTonePicker ]9 ]10 ]11 ]12)
(] \13 effectMode comm_picture_effectmode)
(+ DA{ ]13 ]14 effectPower int \0 ]14 _ int })

(. comm_picture_nbo)
(] \1 pictureId picture_id)
(] \6 magnify internal_r2kPPPV value%\  percent)
(] \7 topTransparency internal_r2kPPPV value\  int+0)

(. comm_picture_nbn)
(] \18 picIdIsVar_112 int_boolean)
(+ DA{ ]18 ]1 picIdVar var_id \0 ]1 picId picture_id })
(] \21 magnifyIsVar_112 int_boolean)
(+ DA{ ]21 ]6 magnifyVar var_id \0 ]6 magnify { internal_r2kPPPV value%\  percent } })
(] \22 topTransparIsVar_112 int_boolean)
(+ DA{ ]22 ]7 topTransparVar var_id \0 ]7 topTranspar internal_r2kPPPV value\  int+0 })

; ---

(. show_picture_array_foot)
(+ DA{ :length ]15 bottomTransparency_2k3 internal_r2kPPPV value\  int+0 \15 { } })

(. show_picture_array_nbo)
(] \0 img f_picture_name)
(+ r2kTonePickerPreview ]9 ]10 ]11 ]12 ]0 Picture/)
(+ comm_picture_nbo)

(e show_picture_maplayer \0 invisible \1 aboveParallax \2 aboveLowerTiles \3 aboveLowerEvents \4 aboveMidEvents \5 aboveUpperTiles \6 aboveUpperEvents \7 aboveWeather \8 aboveAnimations \9 aboveTextbox \10 aboveTimers)
(e show_picture_btllayer \0 invisible \1 aboveBackground \2 aboveBattlersAndAnims \3 aboveWeather \4 aboveTextbox \5 aboveTimers)
(e show_picture_sheettype \0 singleConstFrame \1 singleVarFrame \2 animation)

(. show_picture_array_nbn)
(] \0 img f_picture_name)
(+ r2kTonePickerPreview ]9 ]10 ]11 ]12 ]0 Picture/)
; 15: bottomTransparency (marked Ignored in Ghabry's sheet)
; 16, 17: useless
(] \16 _ int)
(] \17 _ int)
; 18: CNBN handles this (T.O.D)
; 19, 20: Name replacement suffix character count
(] \20 nameReplaceSfxVar_112 var_or_none_id)
(+ DA{ ]20 ]19 nameRepSfxChrCount_112 int= \4 \0 ]19 _ int })
; 21, 22: CNBN handles this (magtrain)
; 23 through 30: Just this.
(] \23 sheetSplitW_112 int)
(+ DA{ ]23 show_picture_array_nbn_sheetdata_ext_on \0 show_picture_array_nbn_sheetdata_ext_off })
(] \28 mapLayer_112 { hide int= \7 show_picture_maplayer })
(] \29 battleLayer_112 show_picture_btllayer)
(] \30 flags_112 subwindow: Flags... bitfield= \97 eraseOnMapChange eraseOnBattleEnd . . tintAffects flashAffects shakeAffects)
(+ comm_picture_nbn)

(. show_picture_array_nbn_sheetdata_ext_on)
(] \24 sheetSplitH_112 int)
(] \25 sheetAnimType_112 show_picture_sheettype)
(+ DA{ ]25 ]26 sheetIndex_112 int \1 ]26 sheetIdxVar_112 var_id \2 ]26 sheetAnimSpeed_112 int })
(] \27 sheetOneLoop_112 int_boolean)

(. show_picture_array_nbn_sheetdata_ext_off)
(] \24 _ int)
(] \25 _ int)
(] \26 _ int)
(] \27 _ int)

; ---

(. move_picture_array_foot)
(] \15 moveSecs/10 int)
(] \16 wait int_boolean)
(+ DA{ :length ]17 bottomTransparency_2k3 internal_r2kPPPV value\  int+0 \17 { } })

(. move_picture_array_nbo)
(] \0 _ string)
(+ comm_picture_nbo)

(. move_picture_array_nbn)
(] \0 _ string)
; 17: bottomTransparency
; 18: id type in CPN
; 19, 20: NRC
(] \19 _ int)
(] \20 _ int)
; 21, 22: CNBN handles this (magtrain)
(+ comm_picture_nbn)
