
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Commands that were given dedicated schemas as part of the AudioPlayer work.

; - sound -

; note: this, and it's schema, is shared between MC/35 and EC/11550

(. play_sound_array)
(] \0 sound f_sound_name_nap)
(+ soundPlayerComplex Sound/ ]0 ]1 ]2 ]3)
(] \1 volume int_default_100)
(] \2 tempo int_default_100)
(] \3 balance int_default_50)

(. play_sound_parameters)
(@ indent indent)
(@ parameters play_sound_array)

(. play_sound_move_parameters)
(@ parameters play_sound_array)


(. change_system_sfx_array)
(] \1 sfxId change_sys_sfx_id)
(] \0 sfx f_sound_name_nap)
(+ soundPlayerComplex Sound/ ]0 ]2 ]3 ]4)
(] \2 volume int_default_100)
(] \3 tempo int_default_100)
(] \4 balance int_default_50)

(. change_system_sfx_parameters)
(@ indent indent)
(@ parameters change_system_sfx_array)

; - music -

(. play_bgm_array)
(] \0 bgm f_music_name_nap)
(+ soundPlayerComplex Music/ ]0 ]2 ]3 ]4)
(] \1 fadeTime int)
(] \2 volume int_default_100)
(] \3 tempo int_default_100)
(] \4 balance int_default_50)

(. play_bgm_parameters)
(@ indent indent)
(@ parameters play_bgm_array)


(. change_system_bgm_array)
(] \1 bgmId change_sys_bgm_id)
(] \0 bgm f_music_name_nap)
(+ soundPlayerComplex Music/ ]0 ]3 ]4 ]5)
(] \2 fadeTime int)
(] \3 volume int_default_100)
(] \4 tempo int_default_100)
(] \5 balance int_default_50)

(. change_system_bgm_parameters)
(@ indent indent)
(@ parameters change_system_bgm_array)

