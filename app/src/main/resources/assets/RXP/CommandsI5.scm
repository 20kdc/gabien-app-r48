
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; [Interpreter Part 5]

(cmd 201 (? ]0 (@ : transfer_player_paramassist) "Transfer Player"))
(C category 2)
(p useVars int_boolean)
(P 0 mapVar var_id)
(v 0 map map_id)
(P 0 xVar var_id)
(v 0 x int)
(P 0 yVar var_id)
(v 0 y int)
(p playerDir direction_disable)
(p fadeType transfer_player_fadetype)
(x transfer_player_paramassist)

(cmd 202 (? ]0 (@ : set_event_location_paramassist) "Set Event Location"))
(C category 3)
(X set_event_location_paramassist)

(cmd 203 "Scroll map")
(C category 3)
(p direction direction)
(p distance int)
(p speed int)

(cmd 204 "Change Map" (? ]0 (" " (@ ]0 cms_type))) " Settings")
(C category 3)
(p type cms_type)
(X change_map_settings_parameters)

(cmd 205 "Fog Colour Tone")
(C category 3)
(p colour Tone)
(p duration int)

(cmd 206 "Fog Opacity Change")
(C category 3)
(p opacity opacity)
(p duration int)

(cmd 207 "Show Animation" ($ " on " ]0 character_id) ($ ": " ]1 animation_id))
(C category 3)
(p event character_id)
(p anim animation_id)

(cmd 208 "Set Player Opacity")
(C category 2)
(p opaque int_boolean)

(cmd 209 "Set event" ($ " " ]0 character_id) " route" ($ " " ]1))
(C category 3)
(p eID character_id)
(p route RPG::MoveRoute)

; NOTE: This apparently does nothing (not even defined),
;       it seems to be solely an assist for RPG Maker XP itself
;       the structure is, firstly the 209, which counts it's own first command,
;       then one 509 for each *additional* command.
;       The first 509 contains the entire MoveRoute list.
;       From a screenshot, the editor apparently looks like this:

;       Set event route: This event
;                        Turn Left
;                        Turn Right
;                        Do a dance
;       >>

;       the actual truth of the matter, and the terminating NOP, being abstracted away.

(cmd 509 "(MoveRoute Editor Helper)")
(C category 3)
(p ex genericScriptParameter)

(cmd 210 "Wait for move end")
(C category 3)

; Yes, there is a gap here.

(cmd 221 "Prepare for transition")
(C category 3)

(cmd 222 "Transition" ($ " " ]0))
(C category 3)
(p transition string)

(cmd 223 "Change Screen Tone")
(C category 3)
(p tone Tone)
(p duration int)

(cmd 224 "Screen flash")
(C category 3)
(p colour Color)
(p duration int)

(cmd 225 "Screen shake")
(C category 3)
(p power int)
(p speed int)
(p duration int)

(cmd 231 "Set/show picture" ($ " " ]0) ($ " to " ]1))
(C category 3)
(p pictureId int)
(p image f_picture_name)
(p centred int_boolean)
(p posVars int_boolean)
(P 3 xVar var_id)
(v 0 x int)
(P 3 yVar var_id)
(v 0 y int)
(p zoom_x zoom)
(p zoom_y zoom)
(p opacity opacity)
(p blendType blend_type)

(cmd 232 "Move picture" ($ " " ]0))
(C category 3)
(p pictureId int)
(p duration int)
(p centred int_boolean)
(p posVars int_boolean)
(P 3 xVar var_id)
(v 0 x int)
(P 3 yVar var_id)
(v 0 y int)
(p zoom_x zoom)
(p zoom_y zoom)
(p opacity opacity)
(p blendType blend_type)

(cmd 233 "Rotate picture" ($ " " ]0))
(C category 3)
(p pictureId int)
(p speed int)

(cmd 234 "Change picture" ($ " " ]0) " tone" ($ " to " ]1) ($ " " ]2))
(C category 3)
(p pictureId int)
(p picTone1 Tone)
(p picTone2 int)

(cmd 235 "Erase picture" ($ " " ]0))
(C category 3)
(p pictureId int)

(cmd 236 "Weather set" ($ " to " ]0 weather_type))
(C category 3)
(p type weather_type)
(p power int)
(p fadeDuration int)

(cmd 241 "Set BGM" ($ " to " ]0))
(C category 5)
(p track rpg_audiofile_bgm)

(cmd 242 "Fade out BGM over" ($ " " ]0) " seconds")
(C category 5)
(p seconds int)

; [ gap here, perfectly fine ]

(cmd 245 "Set BGS" ($ " to " ]0))
(C category 5)
(p track rpg_audiofile_bgs)

(cmd 246 "Fade out BGS over" ($ " " ]0) " seconds")
(C category 5)
(p seconds int)

(cmd 247 "Backup BGM/BGS")
(C category 5)

(cmd 248 "Start BGM/BGS from backup")
(C category 5)

(cmd 249 "Play ME" ($ " " ]0))
(C category 5)
(p track rpg_audiofile_me)

(cmd 250 "Play SE" ($ " " ]0))
(C category 5)
(p track rpg_audiofile_se)

(cmd 251 "Stop Last SE")
(C category 5)