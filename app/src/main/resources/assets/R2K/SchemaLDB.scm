
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; (less incomplete) database schema

; ---

(e rpg_statattr_rank 0 A 1 B 2 C 3 D 4 E)

(. rpg_actor_equipment_block)
(] 0 weapon item_or_none_id)
(] 1 shield item_or_none_id)
(] 2 armour item_or_none_id)
(] 3 helmet item_or_none_id)
(] 4 accessory item_or_none_id)

(vm
	(define-name rpg_actor_parameter_block
		"HP:" (@ ]0) " MP:" (@ ]1) " ATK:" (@ ]2) " DEF:" (@ ]3) " SPI:" (@ ]4) " AGI:" (@ ]5)
	)
)

(. rpg_actor_parameter_block)
(] 0 maxHp int= 10)
(] 1 maxMp int= 10)
(] 2 atk int= 10)
(] 3 def int= 10)
(] 4 spi int= 10)
(] 5 agi int= 10)

(: RPG::BPB)
(@ "1to50" arrayIdX 1 50 subwindowDynamic "(@ : rpg_actor_parameter_block)" rpg_actor_parameter_block)
(+ optP @51to99_2k3 arrayIdX 51 49 subwindowDynamic "(@ : rpg_actor_parameter_block)" rpg_actor_parameter_block)

; (Past 20kdc wrote a comment here but current 20kdc no longer understands what it meant.)
; HAHAHA-NO. EASYRPG PLAYER DEVS KNEW BETTER ALL ALONG.

(. RPG::BCA)
(+ hide lengthAdjustDef Set... 7)
(+ DA{ :length rpg_bca_def 0 rpg_bca_zro })

(. rpg_bca_def)
(+ array 7 battlecommand_menu_id)

(. rpg_bca_zro)
(+ lengthAdjustDef Set... 7)

(: RPG::Learning)
(@ level int= 1)
(@ skill skill_id)

(. actorpage_graphics)
(@ character_name f_charset_name)
(@ character_index int)
(+ spriteSelector @character_index @character_name CharSet/)
(@ character_blend_mode boolean)
(+ internal_EPGD)
(@ face_name f_faceset_name)
(@ face_index int)
(+ spriteSelector @face_index @face_name FaceSet/)

(. rpg_actorclassbase_levelling)

(+ label r2kLevellingGuide
"RM2000: inflate starts out at (1.5 + (exp_mul * 0.01)), base is exp. for each level, add (correction + base), then perform base *= inflate.
Then change inflate to (((LVL * 0.002) + 0.802) * (inflate - 1)) + 1, and run for the next level until done. Levels are 1 to LVL. My goodness this is long.
RM2003: (exp * LVL) + (factorial(LVL) * exp_mul) + (exp_add * LVL)")
(+ optP @init_level_exp int= 30)
(+ optP @each_level_exp_mul int= 30)
(@ each_level_exp_add int)

(: RPG::Actor)
(vm (r2k-namename RPG::Actor))
; Totally not a C&H reference. I added an 'i'.
(@ name stringLen= TediBear 12)
(@ title stringLen= Survivalist 12)
(+ subwindow: "Graphics/Face" actorpage_graphics)
(@ init_level int= 1)
(+ rpg_actorclassbase_levelling)
(+ optP @final_level int= 1)
(@ battle_auto boolean)
(@ battle_super_guard boolean)
(@ battle_parameters subwindow RPG::BPB)
(@ battle_skillspanel_name string)
(@ battle_posx_2k3 int)
(@ battle_posy_2k3 int)
(@ battler_anim_2k3 battleranimation_id)
(@ can_crit boolean)
(@ crit_percent percent)
(@ dual_wield boolean)
(@ lock_equipment boolean)
(@ equipment rpg_actor_equipment_block)
; This will default to 1 (IMPORTANT: This value being wrong causes crashes)
(@ no_weapon_attack_anim animation_id)
(@ class_2k3 class_or_none_id)
(@ learn_skills subwindow arrayIx1 RPG::Learning)
(@ editor_use_skillspanel_name boolean)
(@ state_ranks subwindow hash state_id rpg_statattr_rank)
(@ attr_ranks subwindow hash attribute_id rpg_statattr_rank)
(@ battle_commands_2k3 subwindow RPG::BCA)

; ---

(: RPG::BattlerAnimationData)
(e rpg_badia_movetype 0 none 1 step 2 jump 3 move)
; Unsure if this is right (If it's wrong adjust the custom* stuff in the set_base)
(E rpg_badia_pose Custom.No 0 none 1 idle 2 rightHand 3 leftHand 4 skillUse 5 dead 6 damage 7 badStatus 8 defending 9 walkingLeft 10 walkingRight 11 victory 12 item)
(> rpg_badia_weapon int)
(@ move_type rpg_badia_movetype)
(@ has_afterimage int_boolean)
(@ pose rpg_badia_pose)

(: RPG::Skill)
(vm (r2k-namename RPG::Skill))
(+ rpg_skill_sect0)
(+ subwindow: "User Feedback" rpg_skill_sect2)
(+ subwindow: "Cost / Chance" rpg_skill_sect3)
(+ subwindow: "Result" rpg_skill_sect4)

(. rpg_skill_sect0)
(@ name stringLen 20)
(@ description stringLen 50)
(e rpg_skill_type 0 normal 1 teleportMenu 2 battleEscape 3 switchControl 4 subSkill2k3)
(@ type rpg_skill_type)
(e rpg_skill_scope 0 hurtEnemy 1 hurtAllEnemies 2 healUserActor 3 healAnyActor 4 healAllActors)
(@ scope_n_healing rpg_skill_scope)
(@ switch_control_target switch_id)
(@ usable_in_battle boolean)
(@ usable_outside_battle boolean)
(@ mod_by_attributes subwindow hash attribute_id boolean)

(. rpg_skill_sect2)
(@ animation animation_id)
(@ sound RPG::Sound)
(@ use_text_1_2KO string)
(@ use_text_2_2KO string)
(e rpg_skill_fm 0 A 1 B 2 C 3 dodge)
(@ failure_message_2KO rpg_skill_fm)
(@ OFED_battler_anim_display_actor actor_id)
(@ battler_anim_data subwindow hash actor_id RPG::BattlerAnimationData)
(+ optP @easyrpg_battle_message_2k3 string)

(. rpg_skill_sect3)
(@ sp_cost_val_normal int)
(@ sp_cost_percent_2k3 boolean)
(@ sp_cost_val_percent_2k3 percent)
(@ hit_chance percent)
(@ base_dmg int)
(@ phys_dmg_frac20 int)
(@ mag_dmg_frac20 int)
(@ ignore_def boolean)
(@ variance int)

(. rpg_skill_sect4)
(@ mod_hp boolean)
(@ mod_sp boolean)
(@ mod_atk boolean)
(@ mod_def boolean)
(@ mod_spi boolean)
(@ mod_agi boolean)
(@ steal_enemy_hp boolean)
(@ add_states_2k3 boolean)
(@ mod_states subwindow hash state_id boolean)
(@ affect_target_attr_defence boolean)

; ---

; Is this even used???
(: RPG::ItemAnimation)
(e rpg_ia_type 1 animation 0 battleAnimWeapon)
(@ type rpg_ia_type)
(@ movement rpg_badia_movetype)
(@ battle_anim animation_or_none_id)
(@ has_afterimage int_boolean)
(@ loop_count int)
(@ weapon_batanim_idx rpg_badia_weapon)
(@ ranged boolean)
(@ ranged_batanim_idx rpg_badia_weapon)
(e rpg_ia_speed 0 fast 1 medium 2 slow)
(@ ranged_speed rpg_ia_speed)

(e rpg_item_type 0 normal 1 weapon 2 shield 3 armour 4 helmet 5 accessory 6 medicine 7 skillLearn 8 material 9 special 10 switch)
(e rpg_item_rtarget 0 single 1 centre 2 allAtSameTime 3 allInSequence)
(E rpg_item_usecount Uses 0 Unlimited)
(: RPG::Item)
(vm (r2k-namename RPG::Item))
(@ name stringLen 20)
(@ description stringLen 50)
(@ animation animation_id)
(@ type rpg_item_type)
(@ price int)
(+ subwindow: "Equip Stats" rpg_item_sect0)
(@ use_count rpg_item_usecount)
(@ use_sp_cost int)
(+ optP @easyrpg_using_message string)

(+ label r2kitemActorClass2 "@system/@item_allow_classbased_2k3 controls which of these two are used.")
(@ actor_set subwindow: "Allowed Actors" hash actor_id boolean)
(@ class_set_2k3 subwindow: "Allowed Classes" hash class_id boolean)

(+ subwindow: "Use: Skill (skillLearn on 2k + weapon/shield/armour/helmet/accessory on 2k3)" rpg_item_sect1)
(+ subwindow: "Use: Medicine" rpg_item_sect2)
(+ subwindow: "Use: Material" rpg_item_sect3)
(+ subwindow: "Use: Switch" rpg_item_sect4)
(+ subwindow: "Weapon Data" rpg_item_sect-1)
(@ state_set subwindow: "weapon adds / medicine removes" hash state_id boolean)

(. rpg_item_sect-1)
(@ dead_only boolean)
(@ hit_chance int= 90)
(@ crit_chance int)
(@ attack_preemptive boolean)
(@ dual_attack boolean)
(@ attack_all boolean)
(@ ignore_evasion boolean)
(@ prevent_crit boolean)
(@ raise_evasion boolean)
(@ attr_set subwindow hash attribute_id boolean)
(@ state_chance int)
(@ state_effect boolean)
(@ weapon_anim_def_2k3 actor_id)
(@ weapon_anim_data_2k3 subwindow hash actor_id RPG::ItemAnimation)
(@ ranged_target rpg_item_rtarget)
(@ ranged_return int_boolean)
(@ two_handed boolean)

(. rpg_item_sect0)
(@ equipbuff_atk int)
(@ equipbuff_def int)
(@ equipbuff_spi int)
(@ equipbuff_agi int)
(@ no_terrain_damage boolean)
(@ cursed_2k3 boolean)
(@ half_sp_cost boolean)

(. rpg_item_sect1)
(@ use_skill_2k3 boolean)
(@ skill_use_item_msg_2k3 int_boolean)
(@ skill_id skill_id)

(. rpg_item_sect2)
(@ entire_party boolean)
(@ recover_hp_rate int)
(@ recover_hp int)
(@ recover_sp_rate int)
(@ recover_sp int)
(+ label r2kMedicineHPSP "HP/SP are 'flat' (doesn't scale with actor's max HP/SP), while the 'rate' variables are percentages.")
(@ medicine_only_on_map boolean)

(. rpg_item_sect3)
(@ usebuff_maxhp int)
(@ usebuff_maxsp int)
(@ usebuff_atk int)
(@ usebuff_def int)
(@ usebuff_spi int)
(@ usebuff_agi int)

(. rpg_item_sect4)
(@ switch_usable_in_battle boolean)
(@ switch_usable_on_map booleanDefTrue)
(@ switch_id switch_id)

; ---

(e rpg_enemyaction_kind 0 basic 1 skill 2 transformToNewEnemy)
(e rpg_enemyaction_basic 0 attack 1 dualAttack 2 defense 3 observe 4 charge 5 selfDestruct 6 escape 7 nothing)

(e rpg_enemyaction_conditiontype 0 always 1 switchActive 2 turnCountInRange 3 livingPartySizeInRange 4 enemyHpInRange 5 enemySpInRange 6 partyLevelInRange 7 partyFatigueInRange)

(: RPG::EnemyAction)
(@ rating int= 50)
(@ condition_type rpg_enemyaction_conditiontype)
(@ condition_range_low int)
(@ condition_range_high int)
(@ condition_opt_switch_id switch_id)
(@ act_kind rpg_enemyaction_kind)
(@ act_basic rpg_enemyaction_basic)
(@ act_skill skill_id)
(@ act_transform_enemy enemy_id)
(@ act_set_switch boolean)
(@ act_set_switch_id switch_id)
(@ act_reset_switch boolean)
(@ act_reset_switch_id switch_id)

(: RPG::Enemy)
(vm (r2k-namename RPG::Enemy))
(@ name stringLen= Slime 20)
(@ battler_name f_monster_name)
(@ battler_hue hue)
(@ battler_blend_mode boolean)
(@ max_hp int= 10)
(@ max_sp int= 10)
(@ atk int= 10)
(@ def int= 10)
(@ spi int= 10)
(@ agi int= 10)
(@ exp int)
(@ gold int)
(@ drop_item item_or_none_id)
(@ drop_percent percent)
(@ can_crit boolean)
(@ crit_chance int)
(@ miss boolean)
(@ levitate boolean)
(@ state_ranks subwindow hash state_id rpg_statattr_rank)
(@ attr_ranks subwindow hash attribute_id rpg_statattr_rank)
(@ actions subwindow hash int+0 subwindow RPG::EnemyAction)

; ---

(. rpg_troop_pagecondition_flags_a)
(+ __bitfield__)
(@ switch_a boolean)
(@ switch_b boolean)
(@ variable_>=_val boolean)
(@ turn boolean)
(@ fatigue boolean)
(@ enemy_hp boolean)
(@ actor_hp boolean)
(@ turn_enemy_2k3 boolean)

(. rpg_troop_pagecondition_flags_b)
(+ __bitfield__)
(@ turn_actor boolean)
(@ command_actor boolean)

(: RPG::Troop::PageCondition)
(@ flags_a subwindow rpg_troop_pagecondition_flags_a)
(@ flags_b_2k3 subwindow rpg_troop_pagecondition_flags_b)
(@ switch_a_id switch_id)
(@ switch_b_id switch_id)
(@ variable_id var_id)
(@ variable_value int)
(@ turn_a int)
(@ turn_b int)
(@ fatigue_min int)
(@ fatigue_max int= 100)
(@ enemy_index int)
(@ enemy_hp%_min int)
(@ enemy_hp%_max int= 100)
(@ actor_id actor_id)
(@ actor_hp%_min int)
(@ actor_hp%_max int= 100)
(@ turn_enemy_index_2k3 int)
(@ turn_enemy_a_2k3 int)
(@ turn_enemy_b_2k3 int)
(@ turn_actor_id_2k3 actor_id)
(@ turn_actor_a_2k3 int)
(@ turn_actor_b_2k3 int)
(@ command_actor_id_2k3 actor_id)
(@ command_id_2k3 battlecommand_id)

(: RPG::Troop::Page)
(@ condition RPG::Troop::PageCondition)
(@ list EventListEditor)

(: RPG::Troop::Member)
(@ enemy enemy_id)
(@ x int= 160)
(@ y int= 75)
(@ invisible boolean)

(. rpg_troop_core)
(@ name string)
(@ members subwindow arrayIx1 RPG::Troop::Member)
(@ terrain_set subwindow hash terrain_id boolean)
; issue 52 states that RPG Maker 2k3 expects this to be AX1
(@ pages subwindow arrayAX1 subwindow RPG::Troop::Page)
(@ auto_position boolean)
(@ randomized_memberset_2k3 boolean)
(C magicGenpos r2kTroop . . . . 0)

(: RPG::Troop contextDictionary troop_enemy_id . 1 @members 0 @enemy enemy_id rpg_troop_core)
(vm (r2k-namename RPG::Troop))

; ---

(. rpg_terrain_specialflags)
(+ __bitfield__)
(@ back_party boolean)
(@ back_enemies boolean)
(@ lat_party boolean)
(@ lat_enemies boolean)

(: RPG::Terrain)
(vm (r2k-namename RPG::Terrain))
(@ name string)
(@ damage int)
(@ encounter%_mod int= 100)
(@ background_name f_battleback_name)
(@ boat_pass boolean)
(@ ship_pass boolean)
(@ airship_pass booleanDefTrue)
(@ airship_land booleanDefTrue)
(e rpg_terrain_bushdepth 0 normal 1 third 2 half 3 full)
(@ bush_depth rpg_terrain_bushdepth)
(+ subwindow: "2K3 Extra..." rpg_terrain_bkg)

(. rpg_terrain_bkg)
(@ footstep_sound_2k3 RPG::Sound)
(@ footstep_for_damage_2k3 boolean)

(@ back_as_frame_2k3 boolean)

(@ background_a_name_2k3 string)
(@ background_a_scrh_2k3 boolean)
(@ background_a_scrh_speed_2k3 int)
(@ background_a_scrv_2k3 boolean)
(@ background_a_scrv_speed_2k3 int)

(@ background_b_exists_2k3 boolean)

(@ background_b_name_2k3 string)
(@ background_b_scrh_2k3 boolean)
(@ background_b_scrh_speed_2k3 int)
(@ background_b_scrv_2k3 boolean)
(@ background_b_scrv_speed_2k3 int)

(@ special_flags_2k3 rpg_terrain_specialflags)
(@ special_back_party_2k3 int)
(@ special_back_enemies_2k3 int)
(@ special_lat_party_2k3 int)
(@ special_lat_enemies_2k3 int)
(@ grid_loc_2k3 int)
(@ grid_a_2k3 int)
(@ grid_b_2k3 int)
(@ grid_c_2k3 int)


(: RPG::Attribute)
(@ name string)
(@ magical boolean)
(@ a_rate int= 300)
(@ b_rate int= 200)
(@ c_rate int= 100)
(@ d_rate int= 50)
(@ e_rate int)

; ---

(e rpg_state_restriction 0 noRestrict 1 cannotAct 2 onlyAttackEnemy 3 onlyAttackAlly)
(e rpg_state_affecttype 2 leaveParamsAlone 0 halfParams 1 doubleParams)
(e rpg_state_changetype 2 nothing 0 lose 1 gain)
(: RPG::State)
(vm (r2k-namename RPG::State))
(@ name stringLen 8)
(@ persists int_boolean)
(@ colour int)
(@ priority int)
(@ restriction rpg_state_restriction)
(@ a_rate int)
(@ b_rate int)
(@ c_rate int)
(@ d_rate int)
(@ e_rate int)
(@ hold_turn int)
(@ auto_release_prob int)
(@ release_by_damage int)
(@ affect_type_2k3 rpg_state_affecttype)
(@ affect_atk boolean)
(@ affect_def boolean)
(@ affect_spi boolean)
(@ affect_agi boolean)
(@ reduce_hit_ratio int)
(@ avoid_attacks_2k3 boolean)
(@ reflect_magic_2k3 boolean)
(@ cursed_2k3 boolean)
; This is definitely correct, at least
(E rpg_state_pose Custom.No-1 100 none 0 idle 1 rightHand 2 leftHand 3 skillUse 4 dead 5 damage 6 badStatus 7 defending 8 walkingLeft 9 walkingRight 10 victory 11 item)
(@ battler_pose_2k3 rpg_state_pose)
(@ restrict_skill boolean)
(@ restrict_skill_level int)
(@ restrict_magic boolean)
(@ restrict_magic_level int)
(@ hp_change_type rpg_state_changetype)
(@ hp_change_max int)
(@ hp_change_val int)
(@ hp_change_map_val int)
(@ hp_change_map_steps int)
(@ sp_change_type rpg_state_changetype)
(@ sp_change_max int)
(@ sp_change_val int)
(@ sp_change_map_val int)
(@ sp_change_map_steps int)
(@ msg_actor string)
(@ msg_enemy string)
(@ msg_already string)
(@ msg_affected string)
(@ msg_recovery string)
; ---

(: RPG::Animation::Timing)
(@ frame int)
(@ sound RPG::Sound)
(e rpg_animationtiming_flashscope 0 noFlash 1 target 2 screen)
(@ flash_scope rpg_animationtiming_flashscope)
(@ flash_red int= 31)
(@ flash_green int= 31)
(@ flash_blue int= 31)
(@ flash_power int= 31)
(e rpg_animationtiming_screenshake 0 noShake 1 target 2 screen)
(@ screen_shake rpg_animationtiming_screenshake)

(: RPG::Animation::Cell)
(@ visible boolean)
(@ cell_id int= 0)
(@ x int= 0)
(@ y int= 0)
(@ scale int= 100)
(@ tone_r int= 100)
(@ tone_g int= 100)
(@ tone_b int= 100)
(@ tone_grey int= 100)
(@ transparency int= 0)

(: RPG::Animation::Frame)
(@ cells subwindow arrayIx1 subwindow RPG::Animation::Cell)

(e rpg_animation_scope 0 target 1 screen)
(e rpg_animation_position 0 up 1 middle 2 down)

(. rpg_animation_bat1)
(@ animation_name f_battle_name)
(. rpg_animation_bat2)
(@ animation_name f_battle2_name)

(: RPG::Animation)
(vm (r2k-namename RPG::Animation))
(@ name string)
; Animation source changes dependent on the boolean.
(+ DA{ @battle2_2k3 rpg_animation_bat1 true rpg_animation_bat2 })
(@ battle2_2k3 boolean)
(@ scope rpg_animation_scope)
(@ position rpg_animation_position)
; issue 52 states that RPG Maker 2k3 expects this to be AX1
(@ frames subwindow arrayAX1 subwindow RPG::Animation::Frame)
; yet another "aaaaaa why is this happening" fix
(@ timings subwindow arrayIx1 subwindow RPG::Animation::Timing)
(C magicGenpos r2kAnimation @animation_name . . . 30)

; ---

(: RPG::Tileset)
(vm (r2k-namename RPG::Tileset))
(@ name string= Template)
(@ tileset_name { string= templatetileset imgSelector ChipSet/ ChipSet/ })
; This is kind of important:
; The highpass data is obvious, lowpass/TID not so much.
; Lowpass/TID allocation is:

; 0 through 17 is the special bunch, divided into these groups:

; 0-3:
;  0: 0-999 set.
;  1: 1000-1999 set.
;  2: 2000-2999 set.
;  3: 3000-3049 set.
; 4-5:
;  4: 3050-3099 set.
;  5: 3100-3149 set.
;  (With out of range IDs this could extend further)
; 6-17: 4000-4599 set, divided into 50s, I think
;  (With out of range IDs this could extend further)

; 18-161 is the 5000-5143 set.

(+ tableSTAD R2K/TS162 @terrain_id_data . . 3 162 1 1 1)
(+ tableSTADF R2K/TSPass162 @lowpass_data . . 3 162 1 1 15 down left right up upper wall .)
(+ tableSTADF R2K/TSPass144 @highpass_data . . 3 144 1 1 15 down left right up upper wall counter)
(@ anim_cyclic boolean)
(@ anim_speed int)
; Sets up @highpass_data
(C magicR2kSystemDefaults 2)

; ---

(e rpg_system_ldbid 0 RM2000 2003 RM2003)
; 9 might in fact be a padding item
(e rpg_system_menucommand 1 item 2 skill 3 equipment 4 save 5 status 6 row 7 order 8 wait 9 unknown/padding/quit)
(e rpg_system_itemactorclass2k3 0 Actor 1 Class)

(: RPG::System::TestBattler)
(@ actor actor_id)
(@ level int= 1)
(@ equip_weapon item_id)
(@ equip_shield item_id)
(@ equip_armour item_id)
(@ equip_helmet item_id)
(@ equip_accessory item_id)

(: RPG::System)
; Despite what https://github.com/EasyRPG/Player/issues/885 says, the default I leave this at is giving me RM2000 results (default menus).
; Not so much chunk presence as chunk contents... The difference between "I have to make this optional and document it" or "I don't".
(@ ldb_id rpg_system_ldbid)

; NOTE: NAMING HERE IS IMPORTANT.
; Some names map to names in SaveSystem that get copied over by R2kSystemDefaultsInstaller etcetc.
; Also, usual disclaimer on anything that could be affected by defaults installer applies.

(+ optP @save_count_2k3en int)
(+ optP @save_count_other int)

(+ subwindow: "Music" rpg_system_music)
(+ subwindow: "Sounds" rpg_system_sound)
(+ subwindow: "Transitions" rpg_system_transition)
(+ subwindow: "Test Parameters" rpg_system_testing)

(@ title_name { string imgSelector Title/ Title/ })
(@ show_title_2k3 booleanDefTrue)
(@ gameover_name { string imgSelector GameOver/ GameOver/ })
(@ system_name f_system_name)
(@ system_box_tiling int_boolean)
(@ system2_name_2k3 f_system2_name)

(+ label r2kitemActorClass1 "This applies to all items, and replaces the allowed-actor lists with allowed-class lists.")

(@ item_allow_classbased_2k3 rpg_system_itemactorclass2k3)

(@ party subwindow array 0 actor_id)
(+ optP @menu_commands_2k3 subwindow array 0 rpg_system_menucommand)

(@ boat_name f_charset_name)
(@ boat_index int)
(+ spriteSelector @boat_index @boat_name CharSet/)
(@ ship_name f_charset_name)
(@ ship_index int)
(+ spriteSelector @ship_index @ship_name CharSet/)
(@ airship_name f_charset_name)
(@ airship_index int)
(+ spriteSelector @airship_index @airship_name CharSet/)

(e rpg_system_font 0 gothic 1 mincho)
(@ font_id rpg_system_font)

(@ frame_show_2k3 boolean)
(@ frame_name_2k3 { string imgSelector Frame/ Frame/ })
(@ invert_animations_2k3 boolean)

(. rpg_system_testing)
(@ test_condition int)
(@ test_actor actor_id)
(@ test_battle_background f_battleback_name)
(@ test_battle_data subwindow arrayIx1 RPG::System::TestBattler)
(@ test_battle_terrain terrain_id)
(e rpg_system_testing_battle_formation 0 fromTerrain 1 loose 2 tight)
(@ test_battle_formation rpg_system_testing_battle_formation)
(e rpg_system_testing_battle_condition 0 none 1 initiative 2 back 3 surround 4 pincers)
(@ test_battle_condition rpg_system_testing_battle_condition)

(. rpg_system_music)
(@ title_music RPG::Music)
(@ battle_music RPG::Music)
(@ battle_end_music RPG::Music)
(@ inn_music RPG::Music)
(@ boat_music RPG::Music)
(@ ship_music RPG::Music)
(@ airship_music RPG::Music)
(@ gameover_music RPG::Music)

(. rpg_system_sound)
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

(. rpg_system_transition)
(@ transition_fadein transition_type_no_default)
(@ transition_fadeout transition_type_no_default)
(@ battle_start_fadein transition_type_no_default)
(@ battle_start_fadeout transition_type_no_default)
(@ battle_end_fadein transition_type_no_default)
(@ battle_end_fadeout transition_type_no_default)

; ---

(: RPG::CommonEvent)
(vm
	(define-name Class.RPG::CommonEvent (@ @name) " (" (@ @trigger rpg_cev_trigger) ")")
)
; deliberately ordered to make startOnCallBlocking default
(e rpg_cev_trigger 5 startOnCall 3 startOnCondition 4 startOnConditionParallel)
(@ name string)
(@ trigger rpg_cev_trigger)
(@ condition_switch boolean)
(@ condition_switch_id switch_id)
(@ list EventListEditor)

; ---

(e rpg_battlecommand_type 0 attack 1 skill 2 subSkill 3 defense 4 item 5 escape 6 special)
(: RPG::BattleCommand)
(@ name string)
(@ type rpg_battlecommand_type)

(e rpg_battlecommand_battletype 0 traditional 1 alternative 2 timeGauge)
(e rpg_battlecommand_dir 0 noModify 1 up 2 right 3 down 4 left)

(: RPG::BattleCommands)
(@ auto_placement boolean)
(@ row_back boolean)
(@ window_small boolean)
(@ transparent boolean)
(@ battle_type rpg_battlecommand_battletype)
(@ commands subwindow arrayIx1 RPG::BattleCommand)
(@ death_handler_1 int)
(@ death_handler_2 int)
(@ death_event commonevent_or_none_id)
(@ death_teleport boolean)
(+ condHide @death_teleport rpg_battlecommands_teleport)

(. rpg_battlecommands_teleport)
(@ death_teleport_map map_id)
(@ death_teleport_x int)
(@ death_teleport_y int)
(@ death_teleport_dir rpg_battlecommand_dir)
(+ mapPositionHelper @death_teleport_map @death_teleport_x @death_teleport_y)

; ---

(: RPG::Class)
(vm (r2k-namename RPG::Class))
(@ name string)
(@ dual_wield boolean)
(@ lock_equipment boolean)
(@ battle_auto boolean)
(@ battle_super_guard boolean)
(@ battle_parameters subwindow RPG::BPB)
(@ battler_anim_2k3 battleranimation_id)
(+ rpg_actorclassbase_levelling)
(@ learn_skills subwindow arrayIx1 RPG::Learning)
(@ state_ranks subwindow hash state_id rpg_statattr_rank)
(@ attr_ranks subwindow hash attribute_id rpg_statattr_rank)
(@ battle_commands_2k3 subwindow RPG::BCA)

; ---

(: RPG::BattlerAnimation)
(vm (r2k-namename RPG::BattlerAnimation))
; I really don't understand this stuff
(e rpg_battleranimation_type 0 battlerCharSet 1 animationId)
(@ name string)
(@ type rpg_battleranimation_type)
(@ battler_name string)
(@ battler_index int)
(@ animation_id animation_id)

(. rpg_bas_animset_base)
; This entry doesn't get serialized so long as OPAQUE == nil ; there's code to ensure this.
(] 0 _ OPAQUE)
(] 1 idle subwindow RPG::BattlerAnimation)
(] 2 rightHand subwindow RPG::BattlerAnimation)
(] 3 leftHand subwindow RPG::BattlerAnimation)
(] 4 skillUse subwindow RPG::BattlerAnimation)
(] 5 dead subwindow RPG::BattlerAnimation)
(] 6 damage subwindow RPG::BattlerAnimation)
(] 7 badStatus subwindow RPG::BattlerAnimation)
(] 8 defending subwindow RPG::BattlerAnimation)
(] 9 walkingLeft subwindow RPG::BattlerAnimation)
(] 10 walkingRight subwindow RPG::BattlerAnimation)
(] 11 victory subwindow RPG::BattlerAnimation)
(] 12 item subwindow RPG::BattlerAnimation)
(] 13 custom13 subwindow RPG::BattlerAnimation)
(] 14 custom14 subwindow RPG::BattlerAnimation)
(] 15 custom15 subwindow RPG::BattlerAnimation)
(] 16 custom16 subwindow RPG::BattlerAnimation)
(] 17 custom17 subwindow RPG::BattlerAnimation)
(] 18 custom18 subwindow RPG::BattlerAnimation)
(] 19 custom19 subwindow RPG::BattlerAnimation)
(] 20 custom20 subwindow RPG::BattlerAnimation)
(] 21 custom21 subwindow RPG::BattlerAnimation)
(] 22 custom22 subwindow RPG::BattlerAnimation)
(] 23 custom23 subwindow RPG::BattlerAnimation)
(] 24 custom24 subwindow RPG::BattlerAnimation)
(] 25 custom25 subwindow RPG::BattlerAnimation)
(] 26 custom26 subwindow RPG::BattlerAnimation)
(] 27 custom27 subwindow RPG::BattlerAnimation)
(] 28 custom28 subwindow RPG::BattlerAnimation)
(] 29 custom29 subwindow RPG::BattlerAnimation)
(] 30 custom30 subwindow RPG::BattlerAnimation)
(] 31 custom31 subwindow RPG::BattlerAnimation)
(] 32 custom32 subwindow RPG::BattlerAnimation)

(> rpg_bas_animset_weapon arrayIxN 1 33 subwindow RPG::BattlerAnimation)

(: RPG::BattlerAnimationSet)
(vm (r2k-namename RPG::BattlerAnimationSet))
(@ name string)
(e rpg_bas_speed 0 slow 8 normal 14 fast)
(@ speed rpg_bas_speed)
(@ base_data subwindow rpg_bas_animset_base)
(@ weapon_data subwindow rpg_bas_animset_weapon)

; ---

(: RPG::Database)
(+ jsonExchange "Import entry text JSON" r2k-rpg-database-import-text "Export entry text JSON" r2k-rpg-database-export-text)
(@ actors subwindow hash int+1 subwindow RPG::Actor)
(@ skills subwindow hash int+1 subwindow RPG::Skill)
(@ items subwindow hash int+1 subwindow RPG::Item)
(@ enemies subwindow hash int+1 subwindow RPG::Enemy)
(@ troops subwindow hash int+1 subwindow RPG::Troop)
(@ terrains subwindow hash int+1 subwindow RPG::Terrain)
(@ attributes subwindow hash int+1 subwindow RPG::Attribute)
(@ states subwindow hash int+1 subwindow RPG::State)
(@ animations subwindow hash int+1 subwindow RPG::Animation)
(@ tilesets subwindow hash int+1 subwindow RPG::Tileset)
(@ terms subwindow RPG::Terms)
(@ system subwindow RPG::System)
(@ switches subwindow hash int+1 string)
(@ variables subwindow hash int+1 string)
(@ common_events subwindow hash int+1 subwindow RPG::CommonEvent)
; Supposedly an integer. Supposedly.
; Currently representing as an array of bytes.
(@ db_version subwindow array 0 int)
(@ battle_commands_2k3 subwindow RPG::BattleCommands)
(@ classes_2k3 subwindow hash int+1 subwindow RPG::Class)
(@ battle_anim_sets_2k3 subwindow hash int+1 subwindow RPG::BattlerAnimationSet)
; Really Unimportant
(+ hide optP @unused_27 subwindow array 0 int)
(+ hide optP @unused_28 subwindow array 0 int)
(+ hide optP @unused_31 subwindow array 0 int)
; Supplies defaults
(C magicR2kSystemDefaults 0)

(> File.RPG_RT.ldb RPG::Database)
