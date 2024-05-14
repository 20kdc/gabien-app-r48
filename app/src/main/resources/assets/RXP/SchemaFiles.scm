
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; This is not a critical file for basic operations,
;  but for full usage it is.
; Defining files.
; Keep in mind that when typeChanger{ is involved,
;  the first element is always null (0 is a special value)

(: RPG::Actor)
(C datum \(rcom-idname\ RPG::Actor\ \"Actor\"\))
(@ id index)
(@ name string)
(@ class_id class_id)
(@ weapon_id weapon_id_or_none)
(@ weapon_fix boolean)
(@ armor1_id armour_id_or_none)
(@ armor1_fix boolean)
(@ armor2_id armour_id_or_none)
(@ armor2_fix boolean)
(@ armor3_id armour_id_or_none)
(@ armor3_fix boolean)
(@ armor4_id armour_id_or_none)
(@ armor4_fix boolean)
; This is the stats table.
; maxhp, maxsp, str, dex, agi, int
(+ table @parameters . . \2 \6 \100 \1)
(@ battler_name f_battler_name)
(@ battler_hue hue)
(@ exp_basis int= \30)
(@ exp_inflation int= \30)
(@ initial_level int= \1)
(@ final_level int= \1)
(@ character_name f_char_name)
(@ character_hue hue)

(> File.Actors arrayIx1 subwindow RPG::Actor)

(: RPG::Animation::Frame)
(@ cell_max roint= \1)
(+ table @cell_data @cell_max . \2 \1 \8 \1)

(e animation_timing_condition \0 always \1 whenHit \2 whenNotHit)
(e animation_flashscope \0 none \1 target \2 screen \3 hideTarget)
(: RPG::Animation::Timing)
(@ frame int)
(@ se rpg_audiofile_se)
(@ flash_scope animation_flashscope)
(@ flash_color Color)
(@ flash_duration int)
(@ condition animation_timing_condition)

(e animation_position \0 targetLower \1 targetMiddle \2 targetUpper \3 screenCentre)

(: RPG::Animation)
(C datum \(rcom-idname\ RPG::Animation\ \"Animation\"\))
(@ id index)
(@ name string)
(@ animation_name f_animss_name)
(@ animation_hue hue)
(@ frame_max int= \1)
(@ position animation_position)
(@ frames subwindow arrayAL1 RPG::Animation::Frame)
(@ timings subwindow array \0 RPG::Animation::Timing)
(C magicGenpos xpAnimation @animation_name @animation_hue @animation_name @animation_hue \60)

(> File.Animations arrayIx1 subwindow RPG::Animation)

(: RPG::Armor)
(C datum \(rcom-idname\ RPG::Armor\ \"Armour\"\))
(@ id index)
(@ name string)
(@ icon_name string)
(@ description string)
(@ kind int)
(@ auto_state_id state_id)
(@ price int)
(@ pdef int)
(@ mdef int)
(@ eva int)
(@ str_plus int)
(@ dex_plus int)
(@ agi_plus int)
(@ int_plus int)
(@ guard_element_set subwindow array \0 element_id)
(@ guard_state_set subwindow array \0 state_id)

(> File.Armors arrayIx1 subwindow RPG::Armor)

(: RPG::Class::Learning)
(@ level int)
(@ skill_id skill_id)

(: RPG::Class)
(C datum \(rcom-idname\ RPG::Class\ \"Class\"\))
(@ id index)
(@ name string)
(@ position int)
(@ weapon_set subwindow array \0 weapon_id)
(@ armor_set subwindow array \0 armour_id)
(+ table @element_ranks . . \1 \1 \1 \1)
(+ table @state_ranks . . \1 \1 \1 \1)
(@ learnings subwindow array \0 RPG::Class::Learning)

(> File.Classes arrayIx1 subwindow RPG::Class)

(e commonevent_trigger \0 static \1 autorunOnSwitch \2 autorunOnSwitchParallel)
(: RPG::CommonEvent)
(C datum \
\(define-name\ Class.RPG::CommonEvent\ \"CommonEvent\ \"\ \(@\ @id\)\ \":\ \"\ \(@\ @name\)\
\	\(if-ne\ @trigger\ 0\ \(\
\	\	\"\ \"\ \(@\ @trigger\ commonevent_trigger\)\ \"\ \"\ \(@\ @switch_id\ switch_id\ #t\)\
\	\)\)\
\)\
)
(@ id index)
(@ name string)
(@ trigger commonevent_trigger)
(@ switch_id switch_id)
(@ list EventListEditor)

(> File.CommonEvents arrayIx1 subwindow RPG::CommonEvent)

(: RPG::Enemy::Action)
(@ kind enemy_action_kind)
(@ basic enemy_action_basic)
(@ skill_id skill_id)
(@ condition_turn_a int)
(@ condition_turn_b int)
(@ condition_hp int)
(@ condition_level int)
(@ condition_switch_id switch_id)
(@ rating int)

(: RPG::Enemy)
(C datum \(rcom-idname\ RPG::Enemy\ \"Enemy\"\))
(@ id index)
(@ name string)
(@ battler_name f_battler_name)
(@ battler_hue hue)
(@ maxhp int)
(@ maxsp int)
(@ str int)
(@ dex int)
(@ agi int)
(@ int int)
(@ atk int)
(@ pdef int)
(@ mdef int)
(@ eva int)
(@ animation1_id animation_id)
(@ animation2_id animation_id)
(+ table @element_ranks . . \1 \1 \1 \1)
(+ table @state_ranks . . \1 \1 \1 \1)
(@ actions subwindow array \0 subwindow RPG::Enemy::Action)
(@ exp int)
(@ gold int)
(@ item_id item_id)
(@ weapon_id weapon_id)
(@ armor_id armour_id)
(@ treasure_prob int)

(> File.Enemies arrayIx1 subwindow RPG::Enemy)

; aka parameter to set_target_battlers
(e rpg_item_skill_scope \0 none \1 singleEnemy \2 allEnemies \3 singleAlly \4 allAllies \5 singleDeadAlly \6 allDeadAllies \7 user)
(e rpg_item_skill_occasion \0 alwaysUsable \1 inBattleOnly \2 outOfBattleOnly)

(: RPG::Item)
(C datum \(rcom-idname\ RPG::Item\ \"Item\"\))
(@ id index)
(@ name string)
(@ icon_name f_icon_name)
(@ description string)
(@ scope rpg_item_skill_scope)
(@ occasion rpg_item_skill_occasion)
(@ animation1_id animation_id)
(@ animation2_id animation_id)
(@ menu_se subwindow rpg_audiofile_se)
(@ common_event_id commonevent_id_or_none)
(@ price int)
(@ consumable boolean)
(@ parameter_type int)
(@ parameter_points int)
(@ recover_hp_rate int)
(@ recover_hp int)
(@ recover_sp_rate int)
(@ recover_sp int)
(@ hit int= \100)
(@ pdef_f int)
(@ mdef_f int)
(@ variance int)
(@ element_set subwindow array \0 element_id)
(@ plus_state_set subwindow array \0 state_id)
(@ minus_state_set subwindow array \0 state_id)

(> File.Items arrayIx1 subwindow RPG::Item)

(: RPG::MapInfo)
(C datum \
\(define-name\ Class.RPG::MapInfo\ \"MapInfo:\ \"\ \(@\ @name\)\ \"\ \(order\ \"\ \(@\ @order\)\ \"\)\"\)\
)
(@ name string)
(@ order int= \0)
(@ parent_id int= \0)
; These three mean nothing
(@ expanded boolean)
(@ scroll_x int= \320)
(@ scroll_y int= \240)

(> File.MapInfos hash int subwindow RPG::MapInfo)

(. scriptBlob)
(] \0 loadOrder int)
(] \1 name string)
(] \2 zlibBlob zlibBlobEditor)

(. File.Scripts)
(+ internal_scriptIE)
(+ array \0 scriptBlob)

(: RPG::Skill)
(C datum \(rcom-idname\ RPG::Skill\ \"Skill\"\))
(@ id index)
(@ name string)
(@ icon_name string)
(@ description string)
(@ scope rpg_item_skill_scope)
(@ occasion rpg_item_skill_occasion)
(@ animation1_id animation_id)
(@ animation2_id animation_id)
(@ menu_se subwindow rpg_audiofile_se)
(@ common_event_id commonevent_id)
(@ sp_cost int)
(@ power int)
(@ atk_f int)
(@ eva_f int)
(@ str_f int)
(@ dex_f int)
(@ agi_f int)
(@ int_f int)
(@ hit int= \100)
(@ pdef_f int)
(@ mdef_f int)
(@ variance int)
(@ element_set subwindow array \0 element_id)
(@ plus_state_set subwindow array \0 state_id)
(@ minus_state_set subwindow array \0 state_id)

(> File.Skills arrayIx1 subwindow RPG::Skill)

(: RPG::State)
(C datum \(rcom-idname\ RPG::State\ \"State\"\))
(@ id index)
(@ name string)
(@ animation_id animation_id)
(@ restriction int)
(@ nonresistance boolean)
(@ zero_hp boolean)
(@ cant_get_exp boolean)
(@ cant_evade boolean)
(@ slip_damage boolean)
(@ rating int= \5)
(@ hit_rate int= \100)
(@ maxhp_rate int= \100)
(@ maxsp_rate int= \100)
(@ str_rate int= \100)
(@ dex_rate int= \100)
(@ agi_rate int= \100)
(@ int_rate int= \100)
(@ atk_rate int= \100)
(@ pdef_rate int= \100)
(@ mdef_rate int= \100)
(@ eva int)
(@ battle_only booleanDefTrue)
(@ hold_turn int)
(@ auto_release_prob int)
(@ shock_release_prob int)
(@ guard_element_set subwindow array \0 element_id)
(@ plus_state_set subwindow array \0 state_id)
(@ minus_state_set subwindow array \0 state_id)

(> File.States arrayIx1 subwindow RPG::State)

(: RPG::System::Words)
(@ hp string= hp)
(@ sp string= sp)
(@ agi string= agi)
(@ atk string= atk)
(@ dex string= dex)
(@ int string= int)
(@ str string= str)
(@ gold string= gold)
(@ item string= item)
(@ mdef string= mdef)
(@ pdef string= pdef)
(@ equip string= equip)
(@ guard string= guard)
(@ skill string= skill)
(@ armor1 string= armour1)
(@ armor2 string= armour2)
(@ armor3 string= armour3)
(@ armor4 string= armour4)
(@ attack string= attack)
(@ weapon string= weapon)

(: RPG::System::TestBattler)
(@ level int)
(@ actor_id actor_id)
(@ armor1_id int)
(@ armor2_id int)
(@ armor3_id int)
(@ armor4_id int)
(@ weapon_id int)

(: RPG::System)

; these two are Magic (tm)
; and do magic things. what more to say?

(@ _ int)
(@ magic_number int)


(@ words subwindow RPG::System::Words)
(@ load_se rpg_audiofile_se)
(@ save_se rpg_audiofile_se)
(@ shop_se rpg_audiofile_se)
(@ equip_se rpg_audiofile_se)
(@ buzzer_se rpg_audiofile_se)
(@ cancel_se rpg_audiofile_se)
(@ cursor_se rpg_audiofile_se)
(@ escape_se rpg_audiofile_se)
(@ decision_se rpg_audiofile_se)
(@ actor_collapse_se rpg_audiofile_se)
(@ enemy_collapse_se rpg_audiofile_se)
(@ battle_start_se rpg_audiofile_se)
(@ battle_end_me rpg_audiofile_me)
(@ battle_bgm rpg_audiofile_bgm)
(@ title_name f_title_name)
(@ title_bgm rpg_audiofile_bgm)
(@ windowskin_name f_ws_name)
(@ battleback_name f_bb_name)
(@ gameover_name f_go_name)
(@ gameover_me rpg_audiofile_me)
(@ battler_name f_battler_name)
(@ battler_hue hue)
; Believe it or not, not AL1 (see OneShot remake, Melolune)
; The behavior appears to be that the player's party is given the first actor in the database.
(@ party_members subwindow array \0 actor_id)
(@ battle_transition string)
(@ test_battlers subwindow array \0 subwindow RPG::System::TestBattler)
(@ test_troop_id int)
(@ elements subwindow arrayIx1 string)
(@ switches subwindow arrayIx1 string)
(@ variables subwindow arrayIx1 string)
(@ start_x int= \1)
(@ start_y int= \1)
(@ start_map_id int= \1)
(+ mapPositionHelper @start_map_id @start_x @start_y)
(@ edit_map_id int= \1)

(> File.System RPG::System)

(: RPG::Tileset)
(C datum \(rcom-idname\ RPG::Tileset\ \"Tileset\"\))
(@ id index)
(@ name string)
(@ tileset_name f_tileset_name)
(@ autotile_names array \7 f_autotile_name)
(@ panorama_name f_pano_name)
(@ panorama_hue hue)
(+ tableSTAF RXP/TSTablesPass @passages . . \1 \1632 \1 \1 blockDown blockLeft blockRight blockUp UNK. UNK. submerge \'counter\')
(+ tableSTA RXP/TSTables @priorities . . \1 \1632 \1 \1)
(+ tableSTA RXP/TSTables @terrain_tags . . \1 \1632 \1 \1)
(+ hwnd . RXP/H_StuffAboutTiles)
(@ fog_name f_fog_name)
(@ fog_zoom int)
(@ fog_sx int)
(@ fog_sy int)
(@ fog_opacity opacity)
(@ fog_hue hue)
(@ fog_blend_type blend_type)
(@ battleback_name f_bb_name)

(> File.Tilesets arrayIx1 subwindow RPG::Tileset)

(: RPG::Troop::Member)
(@ enemy_id enemy_id)
(@ x int)
(@ y int)
(@ hidden boolean)
(@ immortal boolean)

(: RPG::Troop::Page::Condition)
(@ turn_valid boolean)
(@ turn_a int)
(@ turn_b int)
(@ enemy_valid boolean)
(@ enemy_index troop_enemy_id)
(@ enemy_hp int)
(@ actor_valid boolean)
(@ actor_id actor_id)
(@ actor_hp int)
(@ switch_valid boolean)
(@ switch_id switch_id)

(: RPG::Troop::Page)
(@ condition subwindow RPG::Troop::Page::Condition)
(@ span int)
(@ list EventListEditor)

(. rpg_troop_core)
(@ id index)
(@ name string)
(@ pages subwindow arrayAL1 RPG::Troop::Page)
(@ members subwindow array \0 RPG::Troop::Member)
(C magicGenpos xpTroop . . . . \0)

(> rpg_troop_s2 contextDictionary iterate_enemy iterate_enemy_base \1 @members \0 @enemy_id enemy_id rpg_troop_core)

(: RPG::Troop contextDictionary troop_enemy_id . \1 @members \0 @enemy_id enemy_id rpg_troop_s2)
(C datum \(rcom-idname\ RPG::Troop\ \"Troop\"\))

(> File.Troops arrayIx1 subwindow RPG::Troop)

(: RPG::Weapon)
(C datum \(rcom-idname\ RPG::Weapon\ \"Weapon\"\))
(@ id index)
(@ atk int)
(@ mdef int)
(@ name string)
(@ pdef int)
(@ price int)
(@ agi_plus int)
(@ dex_plus int)
(@ int_plus int)
(@ str_plus int)
(@ icon_name string)
(@ description string)
(@ animation1_id animation_id)
(@ animation2_id animation_id)
(@ element_set subwindow array \0 element_id)
(@ plus_state_set subwindow array \0 state_id)
(@ minus_state_set subwindow array \0 state_id)

(> File.Weapons arrayIx1 subwindow RPG::Weapon)
(> File.xScripts File.Scripts)

; The List Of Files That Need Schemas
; Map*** [Of course it's OK, it's a core component. But it's in SchemaEditing.txt, not here,
;          because those files don't have simple-to-recognize names, for one.]
; Actors [OK]
; Animations [OK]
; Armors [OK]
; Classes [OK]
; CommonEvents [OK]
; Enemies [OK]
; Items [OK]
; MapInfos [OK]
; Scripts [OK]
; Skills [OK]
; States [OK]
; System [OK]
; Tilesets [OK]
; Troops [OK]
; Weapons [OK]
; xScripts [OK]
