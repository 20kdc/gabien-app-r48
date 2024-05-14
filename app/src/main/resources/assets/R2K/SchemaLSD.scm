
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; LSD support

; NOTE: NAMING HERE IS IMPORTANT.
; Some names map to names in SaveSystem that get copied over by R2kSystemDefaultsInstaller etcetc.
; Also, usual disclaimer on anything that could be affected by defaults installer applies.

(: RPG::SaveTitle)
; Yes, this is important. Not setting a timestamp leads to a rejected save last I checked
(@ timestamp float= \1.0)
(@ hero_name string)
(@ hero_level int)
(@ hero_hp int)
(@ face1_name f_faceset_name)
(@ face1_index int)
(+ spriteSelector @face1_index @face1_name FaceSet/)
(@ face2_name f_faceset_name)
(@ face2_index int)
(+ spriteSelector @face2_index @face2_name FaceSet/)
(@ face3_name f_faceset_name)
(@ face3_index int)
(+ spriteSelector @face3_index @face3_name FaceSet/)
(@ face4_name f_faceset_name)
(@ face4_index int)
(+ spriteSelector @face4_index @face4_name FaceSet/)

(e rpg_saveactor_row \-1 back \0 unset \1 front)
(: RPG::SaveActor)
(@ name string)
(@ title string)
(@ level int)
(@ exp int)
(@ current_hp int)
(@ current_sp int)
(@ character_name f_save_charset_name)
(@ character_index int)
(+ spriteSelector @character_index @character_name CharSet/)
(@ character_flags int)
(@ face_name f_faceset_name)
(@ face_index int)
(+ spriteSelector @face_index @face_name FaceSet/)
(@ hp_mod int)
(@ sp_mod int)
(@ atk_mod int)
(@ def_mod int)
(@ spi_mod int)
(@ agi_mod int)
(@ skills subwindow array \0 skill_id)
(@ equipment rpg_actor_equipment_block)
(@ changed_battle_commands_2k3 boolean)
(@ battle_commands_2k3 subwindow array \7 battlecommand_menu_id)
(@ states array \0 state_id)
(@ class_id_2k3 class_complex_id)
(@ row rpg_saveactor_row)
(@ two_weapon boolean)
(@ lock_equipment boolean)
(@ auto_battle boolean)
(@ super_guard boolean)
(@ battler_animation_2k3 battleranimation_id)

(: RPG::SaveTarget)
(@ map map_id)
(@ x int)
(@ y int)
(+ mapPositionHelper @map @x @y)
(@ switch_valid boolean)
(@ switch_id switch_id)

; The screen value is completely ignored
(e rpg_savesystem_screen \5 save)
(: RPG::SaveSystem)
(@ screen rpg_savesystem_screen)
(@ frame_count int)
; If empty, defaults to game's default System.
(@ system_name f_save_system_name)
; If not present, defaults to that in Database.
(+ optP @system_box_tiling int_boolean)
(@ font_id rpg_system_font)
(@ switches subwindow arrayEIdX switch_id \1 \0 boolean)
(@ variables subwindow arrayEIdX var_id \1 \0 int)
(+ subwindow: Messagebox rpg_savesystem_text)
(+ subwindow: Music rpg_savesystem_music)
(+ subwindow: Sound rpg_savesystem_sound)
(+ subwindow: Transitions rpg_savesystem_transition)
(@ can_teleport booleanDefTrue)
(@ can_escape booleanDefTrue)
(@ can_save booleanDefTrue)
(@ can_menu booleanDefTrue)
(@ battle_background string)
(@ save_count int)
(@ save_slot int= \1)
(@ atb_wait_mode_2k3 int_boolean)

(. rpg_savesystem_text)
(@ message_transparent int)
(@ message_position int= \2)
(@ message_prevent_overlap int= \1)
(@ message_continue_events int)
(@ face_name f_faceset_name)
(@ face_index int)
(+ spriteSelector @face_index @face_name FaceSet/)
(@ face_right boolean)
(@ face_flip boolean)
(@ transparent boolean)

(. rpg_savesystem_music)
(@ title_music RPG::Music)
(@ battle_music RPG::Music)
(@ battle_end_music RPG::Music)
(@ inn_music RPG::Music)
(@ boat_music RPG::Music)
(@ ship_music RPG::Music)
(@ airship_music RPG::Music)
(@ gameover_music RPG::Music)
; Note the division. 'vehicle_start' and 'battle_start' are backups for when leaving the respective state.
(@ rtmusic_current RPG::Music)
(@ rtmusic_memorized RPG::Music)
(@ rtmusic_vehicle_start RPG::Music)
(@ rtmusic_battle_start RPG::Music)

(. rpg_savesystem_sound)
(@ cursor_se RPG::Sound)
(@ decision_se RPG::Sound)
(@ cancel_se RPG::Sound)
(@ buzzer_se RPG::Sound)
(@ battle_se RPG::Sound)
(@ escape_se RPG::Sound)
(@ enemy_attack_se RPG::Sound)
(@ enemy_hurt_se RPG::Sound)
(@ actor_hurt_se RPG::Sound)
(@ dodge_se RPG::Sound)
(@ enemy_death_se RPG::Sound)
(@ item_se RPG::Sound)

(. rpg_savesystem_transition)
; They sure did put a lot of attention into this behavior, huh.
(@ transition_fadein transition_type)
(@ transition_fadeout transition_type)
(@ battle_start_fadein transition_type)
(@ battle_start_fadeout transition_type)
(@ battle_end_fadein transition_type)
(@ battle_end_fadeout transition_type)

(: RPG::Interpreter)
; Regarding this routine, {A=0=1|#A} makes an A of 0 show as 1,
;  because of the Ix1 - then 1 is changed to "empty" and other values are changed to the original count minus 1.
(C datum \
\(define-tr\ TrNStr.rpg_interpreter_empty\ \"empty\"\)\
\(define-tr\ TrNStr.rpg_interpreter_depth\ \(fl1\ a0\ \"-deep\ interpreter\"\)\)\
\(define-name-nls\ Class.RPG::Interpreter\ \(vm\ \(let\
\	\(\(stack-len\ \(dm-a-len\ \(dm-at\ a0\ @stack\)\)\)\)\
\	\(if\ \(=\ stack-len\ 0\)\
\	\	\(TrNStr.rpg_interpreter_empty\ a0\)\
\	\	\(TrNStr.rpg_interpreter_depth\ \(-\ stack-len\ 1\)\)\
\	\)\
\)\)\)\
)
(@ stack arrayIx1 subwindow RPG::InterpreterStackLevel)
(@ shown_message boolean)
(@ waiting_for_nmovement boolean)
; INVESTIGATE THIS!!!
(@ waiting_for_sleep int)
(@ waiting_for_sleep_alt int)
(@ keyii_wait boolean)
; Regarding this, it's limited to a byte.
(@ keyii_variable_OLD int)
(@ keyii_timed boolean)
(@ keyii_time_variable var_id)
(@ keyii_filter_arrowkeys boolean)
(@ keyii_filter_decision boolean)
(@ keyii_filter_cancel boolean)
(@ keyii_filter_1A boolean)
(@ keyii_filter_1B boolean)
(@ keyii_filter_1C boolean)
(@ keyii_filter_1D boolean)
(@ keyii_filter_1E boolean)
; Might actually be memory, not filter - OR could be 2k3 - check the index order for details
(@ keyii_filter_23 boolean)
(@ keyii_filter_24 boolean)
(@ keyii_filter_25 boolean)
(@ keyii_filter_26 boolean)

(: RPG::InterpreterStackLevel)
(@ list EventListEditor)
(@ index int)
(@ event_id event_or_none_id)
(@ actioned boolean)
(@ branches subwindow array \0 int)

(: RPG::SaveCommonEvent)
(@ i subwindow RPG::Interpreter)

(. rpg_savepicture_flags)
(+ __bitfield__)
(@ erase_on_mapchange boolean)
(@ erase_on_battleend boolean)
(@ unused_1 boolean)
(@ unused_2 boolean)
(@ mod_tint boolean)
(@ mod_flash boolean)
(@ mod_shake boolean)

(: RPG::SavePicture)
(C datum \
\(define-name\ Class.RPG::SavePicture\ \(if-ne\ @name\ \"\"\ \(\
\	\(@\ @name\)\
\	\"[\"\ \(@\ @spritesheet_cols_112\)\ \",\"\ \(@\ @spritesheet_rows_112\)\ \"][\"\ \(@\ @spritesheet_frame_112\)\ \"]\"\
\	\"\ at\ \"\ \(@\ @x\)\ \"\ \"\ \(@\ @y\)\
\)\ \"<unused\ picture>\"\)\)\
)
(@ name f_picture_name)
(@ spritesheet_cols_112 int= \1)
(@ spritesheet_rows_112 int= \1)
(@ spritesheet_frame_112 int)
(@ spritesheet_speed_112 int)
; no, not that one
(@ spritesheet_oneshot_112 boolean)

(@ rotation float)
(@ waver int)
(@ layer_map_112 show_picture_maplayer)
(@ layer_battle_112 show_picture_btllayer)
(@ fixed boolean)
(@ start_x float)
(@ start_y float)
(@ lifetime int)
(@ flags_112 rpg_savepicture_flags)

(+ subwindow: Current\ State rpg_savepicture_state_current)
(@ move_remain_time int)
(+ subwindow: Target\ State rpg_savepicture_state_target)

(. rpg_savepicture_state_current)
(@ x float)
(@ y float)
(@ magnify float= \100.0)
(@ transparency boolean)
(@ transparency_top float)
(@ transparency_bottom float)
(@ r float= \100.0)
(@ g float= \100.0)
(@ b float= \100.0)
(@ s float= \100.0)
(@ fx int)
(@ fxstrength float)

(. rpg_savepicture_state_target)
(@ target_x float)
(@ target_y float)
(@ target_magnify int= \100)
(@ target_transp_top int)
(@ target_transp_bottom int)
(@ target_r int= \100)
(@ target_g int= \100)
(@ target_b int= \100)
(@ target_s int= \100)
(@ target_fxstrength int)

(: RPG::SaveParty)
(@ party subwindow array \0 actor_id)
(@ inventory subwindow array \0 RPG::SaveItem)
(@ party_gold int)
; NOTE: Suspicious lack of intra-second info.
;       Could be framecount based
; NOTE2: YNEG examination suggests these are always incrementing.
;        Despite being off. Hm.
(@ timer1_seconds int)
(@ timer1_on boolean)
(@ timer1_visible boolean)
(@ timer1_on_battle boolean)
(@ timer2_seconds int)
(@ timer2_on boolean)
(@ timer2_visible boolean)
(@ timer2_on_battle boolean)
(@ stats_battles int)
(@ stats_defeats int)
(@ stats_escapes int)
(@ stats_victories int)
(@ stats_turns int)
(@ stats_steps int)

; Synthesized from whole nylon by SaveParty
(: RPG::SaveItem)
(@ id item_id)
(@ count int= \1)
(@ usage int)

; Common between the various eventlikes

; note: check if this is actually a bitfield
(e rpg_character_transparency \0 off \3 on)
(. rpg_character_graphics)
(@ character_name f_save_charset_name)
(@ character_index int)
(+ halfsplit eventTileHelper @character_index @character_name \1 R2K/TS144 spriteSelector @character_index @character_name CharSet/)
(@ character_direction sprite_direction)
(@ character_pattern int= \1)
(+ internal_EPGD)

(. rpg_character)
; R2kSavefileEventAccess requires this
(@ active booleanDefTrue)
; In case you're wondering, yes, this is why map_id has the magical mad insane thing on it.
(@ map map_id)
(@ x int)
(@ y int)
(+ mapPositionHelper @map @x @y)
(+ subwindow: Graphics rpg_character_graphics)
(@ dir sprite_direction)
(@ transparency rpg_character_transparency)
(@ remaining_step int)
(@ layer eventpage_layer)
(@ block_other_events boolean)
(@ anim_type eventpage_animtype)
(@ lock_dir boolean)
; These settings have to be the same as the real default ones for the player,
;  or an attempt at synthesizing a "blank" WILL fail.
; This means it's forced to 2,4 by default.
(@ move_freq int= \2)
(@ move_speed int= \4)
(@ move_route RPG::MoveRoute)
(@ move_route_overwrite boolean)
(@ move_route_position int)
(@ move_route_has_looped boolean)
(@ anim_paused int)
(@ through boolean)
(@ wait_time_counter int)
(@ anim_count int)
(@ wait_time int)
(@ jumping boolean)
(@ begin_jump_x int)
(@ begin_jump_y int)
(@ unknown_47 int)
(@ moved_on_frame int)
(@ flying boolean)
(@ flash_red int= \100)
(@ flash_green int= \100)
(@ flash_blue int= \100)
(@ flash_position float)
(@ flash_frames_left int)

(: RPG::SaveMapEvent)
(C datum \
\(define-name\ Class.RPG::SaveMapEvent\ \(@\ @character_name\)\ \":\"\ \(@\ @character_index\)\ \"@\"\ \(@\ @x\)\ \",\"\ \(@\ @y\)\)\
)
(+ rpg_character)
(@ running boolean)
(@ original_moveroute_index int)
(@ pending boolean)
(@ interpreter subwindow RPG::Interpreter)
; 2f is just plain ignored for now

(e rpg_save_vehicletype_opt \0 none \1 boat \2 ship \3 airShip)
(e rpg_save_vehicletype \1 boat \2 ship \3 airShip)

(: RPG::SaveVehicleLocation)
(C datum \
\(define-name\ Class.RPG::SaveVehicleLocation\ \(@\ @character_name\)\ \":\"\ \(@\ @character_index\)\ \"@\"\ \(@\ @map\ map_id\ #t\)\ \"\ \"\ \(@\ @x\)\ \",\"\ \(@\ @y\)\)\
)
(+ rpg_character)
(@ vehicle_type rpg_save_vehicletype)
(@ original_moveroute_index int)
(@ remaining_ascent int)
(@ remaining_descent int)
(@ sprite2_name f_save_charset_name)
(@ sprite2_index int)
(+ halfsplit eventTileHelper @sprite2_index @sprite2_name \1 R2K/TS144 spriteSelector @sprite2_index @sprite2_name CharSet/)

(: RPG::SavePartyLocation)
(C datum \
\(define-name\ Class.RPG::SavePartyLocation\ \(@\ @character_name\)\ \":\"\ \(@\ @character_index\)\ \"@\"\ \(@\ @map\ map_id\ #t\)\ \"\ \"\ \(@\ @x\)\ \",\"\ \(@\ @y\)\)\
)
(+ rpg_character)
(@ sprite_transparent boolean)
; 2f is just plain ignored for now
(@ vehicle_boarding boolean)
(@ vehicle_aboard boolean)
(@ vehicle_type rpg_save_vehicletype_opt)
(@ vehicle_leaving boolean)
; Maybe keep in sync with the movespeed default above
(@ vehicle_movespeed_backup eventpage_movespeed)
(@ menu_activation_waiting boolean)
; -1: not panning
; other: panning
(@ pan_state int= \-1)
(@ pan_x int= \2304)
(@ pan_y int= \1792)
(@ pan_end_x int= \3304)
(@ pan_end_y int= \1792)
(@ pan_speed int= \16)
(@ encounter_steps int)
(@ encounter_activation_waiting boolean)
(@ map_save_count int)
(@ db_save_count int)

(: RPG::SaveMapInfo)
(@ x int)
(@ y int)
(@ encounter_rate int)
(@ tileset_id save_tileset_id)
(@ events subwindow hash int subwindow RPG::SaveMapEvent)
(@ lower_tile_remap table . . . \3 \144 \1 \1)
(@ upper_tile_remap table . . . \3 \144 \1 \1)
(@ parallax_name string)
(@ parallax_loop_x boolean)
(@ parallax_loop_y boolean)
(@ parallax_autoloop_x boolean)
(@ parallax_sx int)
(@ parallax_autoloop_y boolean)
(@ parallax_sy int)

(: RPG::SaveScreen)
(@ tint_r float= \100.0)
(@ tint_g float= \100.0)
(@ tint_b float= \100.0)
(@ tint_s float= \100.0)
(@ tint_end_r int= \100)
(@ tint_end_g int= \100)
(@ tint_end_b int= \100)
(@ tint_end_s int= \100)
(@ tint_frames_left int= \0)

(@ flash_continuous boolean)
(@ flash_r int)
(@ flash_g int)
(@ flash_b int)
(@ flash_position float)
(@ flash_frames_left int)

(@ shake_strength int)
(@ shake_speed int)
(@ shake_x int)
(@ shake_y int)
(@ shake_continuous boolean)
(@ shake_frames_left int)

(@ pan_x int)
(@ pan_y int)
; TODO WORK OUT BATTLEANIM!!!
(@ battleanim_id int)
(@ battleanim_target int)
(@ battleanim_frame int)
(@ battleanim_active boolean)
(@ battleanim_global boolean)
(@ weather weather_control_type)
(@ weather_strength weather_control_strength)

(: RPG::Save)
(C datum \
\(define-name\ Class.RPG::Save\
\	\(vm\ \(r2kts->string\ \(dm-at\ a0\ @title@timestamp\)\)\)\ \"\ \"\
\	\(@\ @title@hero_name\)\ \"\ L\"\ \(@\ @title@hero_level\)\ \"\ \"\ \(@\ @title@hero_hp\)\ \"HP\"\
\)\
)
; Important
(@ title subwindow RPG::SaveTitle)
(@ system subwindow RPG::SaveSystem)
(@ actors subwindow hash actor_id subwindow RPG::SaveActor)
(@ party subwindow RPG::SaveParty)
(@ party_pos subwindow RPG::SavePartyLocation)
; Unimportant Global
(@ boat_pos subwindow RPG::SaveVehicleLocation)
(@ ship_pos subwindow RPG::SaveVehicleLocation)
(@ airship_pos subwindow RPG::SaveVehicleLocation)
(@ targets subwindow array \0 RPG::SaveTarget)
; Unimportant Local
(@ map_info subwindow RPG::SaveMapInfo)
(@ screen subwindow RPG::SaveScreen)
(@ pictures subwindow hash int subwindow RPG::SavePicture)

(@ main_interpreter subwindow RPG::Interpreter)
(@ common_events subwindow hash commonevent_id RPG::SaveCommonEvent)
; Really Unimportant
(+ optP @unused_panorama subwindow array \0 int)
; This is 0.6.0.
(+ optP @easyrpg_player_version_EPL int= \600)

; NOTE: This also creates buttons
(C magicR2kSystemDefaults \3)