
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; This should contain ALL RPG::*Item::<name here> classes.

(i RVXA/SchemaBatEF)

; This contains the DropItems kind stuff.

(i RVXA/SchemaBatDI)

; The definitions of files.

(: RPG::Actor)
(vm (rcom-idname RPG::Actor "Actor"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(@ description stringBlobEditor)
(@ nickname string)
(@ character_name f_char_name)
(@ character_index int)
(+ spriteSelector @character_index @character_name Characters/)
(@ face_name f_face_name)
(@ face_index int)
(+ spriteSelector @face_index @face_name Faces/)
(@ class_id class_id)
(@ initial_level int)
(@ max_level int)
(@ equips array 5 int)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)

(> File.Actors arrayIx1 subwindow RPG::Actor)

(: RPG::Animation::Frame)
(@ cell_max roint= 1)
; In this version, 7 is blend_type, 6 is opacity (0-255), 5 is mirror (int_boolean), 4 is angle, 3 is scale (0-100), x is modified by 1, y is modified by 2.
; 0 is presumably cell-id.
(+ table @cell_data @cell_max . 2 1 8 1)

(e animation_flashscope 0 none 1 target 2 screen 3 hideTarget)
(: RPG::Animation::Timing)
(@ frame int)
(@ se RPG::SE)
(@ flash_scope animation_flashscope)
(@ flash_color Color)
(@ flash_duration int)

(e animation_position 0 targetLower 1 targetMiddle 2 targetUpper 3 screenCentre)

(: RPG::Animation)
(vm (rcom-idname RPG::Animation "Animation"))
(@ id index)
(@ name string)
(@ animation1_name f_animss_name)
(@ animation1_hue hue)
(@ animation2_name f_animss_name)
(@ animation2_hue hue)
(@ frame_max int= 1)
(@ position animation_position)
(@ frames subwindow arrayAL1 RPG::Animation::Frame)
(@ timings subwindow array 0 RPG::Animation::Timing)
(C magicGenpos vxaAnimation @animation1_name @animation1_hue @animation2_name @animation2_hue 60)

(> File.Animations arrayIx1 subwindow RPG::Animation)

(: RPG::Armor)
(vm (rcom-idname RPG::Armor "Armour"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(@ description stringBlobEditor)
(@ price int)
(@ icon_index int)
(@ params battle_parameter_array)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)
(@ etype_id etype_id)
(@ atype_id atype_id)

(> File.Armors arrayIx1 subwindow RPG::Armor)

(: RPG::Class::Learning)
(vm
	(define-name Class.RPG::Class::Learning "Learning at level " (@ @level) ": " (@ @skill_id skill_id #t))
)
(@ note stringBlobEditor)
(@ level int)
(@ skill_id skill_id)

(. RPG::Class.exp_params)
(+ hide array 4 int)
(] 0 base typeChanger{ int i float f })
(] 1 extra typeChanger{ int i float f })
(] 2 accA typeChanger{ int i float f })
(] 3 accB typeChanger{ int i float f })

(: RPG::Class)
(vm (rcom-idname RPG::Class "Class"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(@ description stringBlobEditor)
(@ icon_index int)
(+ label rvxaBatParamsTableDoc
"Columns are: maxHP, maxMP, attack, defense, magicAttack, magicDefense, agility, luck")
(+ table @params . . 2 8 100 1)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)
(@ learnings subwindow array 0 subwindow RPG::Class::Learning)
(@ exp_params RPG::Class.exp_params)

(> File.Classes arrayIx1 subwindow RPG::Class)

(e commonevent_trigger 0 static 1 autorunOnSwitch 2 autorunOnSwitchParallel)
(: RPG::CommonEvent)
(vm
	(define-name Class.RPG::CommonEvent "CommonEvent " (@ @id) ": " (@ @name)
		(if-ne @trigger 0 (
			" " (@ @trigger commonevent_trigger) " " (@ @switch_id switch_id #t)
		))
	)
)
(@ id index)
(@ name string)
(@ trigger commonevent_trigger)
(@ switch_id switch_id)
(@ list EventListEditor)

(> File.CommonEvents arrayIx1 subwindow RPG::CommonEvent)

(: RPG::Enemy)
(vm (rcom-idname RPG::Enemy "Enemy"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(+ optP @description stringBlobEditor)
(@ battler_name string)
(@ battler_hue hue)
(+ optP @icon_index int)
(@ exp int)
(+ optP @eva int)
(+ optP @hit int)
(@ gold int)
(@ params battle_parameter_array)
(@ actions subwindow array 0 subwindow RPG::Enemy::Action)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)
(@ drop_items subwindow array 0 subwindow RPG::Enemy::DropItem)

(> File.Enemies arrayIx1 subwindow RPG::Enemy)

; Notice the similarity to skills?

(: RPG::Item)
(vm (rcom-idname RPG::Item "Item"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(@ description stringBlobEditor)
(@ price int)
(@ occasion battle_usableitem_occasion)
(@ consumable boolean)
(@ success_rate int= 100)
(@ scope battle_usableitem_scope)
(@ speed int)
(@ repeats int= 1)
(@ hit_type battle_usableitem_hit_type)
(@ damage RPG::UsableItem::Damage)
(@ effects subwindow array 0 subwindow RPG::UsableItem::Effect)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)
(@ tp_gain int)
(@ icon_index int)
(@ animation_id animation_id)
(@ itype_id int= 1)

(> File.Items arrayIx1 subwindow RPG::Item)

(: RPG::MapInfo)
(vm
	(define-name Class.RPG::MapInfo "MapInfo: " (@ @name) " (order " (@ @order) ")")
)
(@ name string)
(@ order int= 0)
(@ parent_id int= 0)
; These three mean nothing
(@ expanded boolean)
(@ scroll_x int= 320)
(@ scroll_y int= 240)

(> File.MapInfos hash int subwindow RPG::MapInfo)

(. scriptBlob)
; This value seems essentially completely random, even for the same content.
(] 0 maybeChecksum int)
(] 1 name string)
(] 2 zlibBlob zlibBlobEditor)

(. File.Scripts)
(+ internal_scriptIE)
(+ array 0 scriptBlob)

(: RPG::Skill)
(vm (rcom-idname RPG::Skill "Skill"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(@ description stringBlobEditor)
(@ scope battle_usableitem_scope)
(@ occasion battle_usableitem_occasion)
(@ success_rate int= 100)
(@ speed int)
(@ repeats int)
(@ hit_type battle_usableitem_hit_type)
(@ damage RPG::UsableItem::Damage)
(@ effects subwindow array 0 subwindow RPG::UsableItem::Effect)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)
(@ mp_cost int)
(@ tp_cost int)
(@ tp_gain int)
(@ message1 string)
(@ message2 string)
(@ stype_id stype_id)
(@ icon_index int)
(@ animation_id animation_id)
(@ required_wtype_id1 wtype_id)
(@ required_wtype_id2 wtype_id)

(> File.Skills arrayIx1 subwindow RPG::Skill)

(: RPG::State)
(vm (rcom-idname RPG::State "State"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(+ optP @description stringBlobEditor)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)
; Hero gets state message?
(@ message1 string)
; Enemy gets state message?
(@ message2 string)
; Passive alert message?
(@ message3 string)
; Any loses state message?
(@ message4 string)
(@ priority int)
(@ max_turns int)
(@ min_turns int)
(@ icon_index int)
(@ restriction int)
(@ chance_by_damage int)
(@ remove_by_damage boolean)
(+ optP @release_by_damage boolean)
(@ remove_by_walking boolean)
(@ steps_to_remove int)
(@ auto_removal_timing int)
(@ remove_at_battle_end boolean)
(@ remove_by_restriction boolean)

(> File.States arrayIx1 subwindow RPG::State)

(: RPG::System::Vehicle)
; For all your Thomas The Tank Engine theme needs.
(@ bgm RPG::BGM)
(@ start_x int)
(@ start_y int)
(@ start_map_id map_id)
(@ character_name f_char_name)
(@ character_index int)
(+ spriteSelector @character_index @character_name Characters/)

(. RPG::System.sounds)
(+ subwindow array 24 RPG::SE)
(] 0 cursor RPG::SE)
(] 1 ok RPG::SE)
(] 2 cancel RPG::SE)
(] 3 buzzer RPG::SE)
(] 4 equip RPG::SE)
(] 5 save RPG::SE)
(] 6 load RPG::SE)
(] 7 battleStart RPG::SE)
(] 8 escape RPG::SE)
(] 9 enemyAttack RPG::SE)
(] 10 enemyDamage RPG::SE)
(] 11 enemyCollapse RPG::SE)
(] 12 bossCollapse1 RPG::SE)
(] 13 bossCollapse2 RPG::SE)
(] 14 actorDamage RPG::SE)
(] 15 actorCollapse RPG::SE)
(] 16 recovery RPG::SE)
(] 17 miss RPG::SE)
(] 18 evasion RPG::SE)
(] 19 magicEvasion RPG::SE)
(] 20 magicReflection RPG::SE)
(] 21 shop RPG::SE)
(] 22 useItem RPG::SE)
(] 23 useSkill RPG::SE)

(: RPG::System::Terms)
(@ basic subwindow array 8 string)
(@ etypes subwindow array 0 string)
(@ params subwindow array 8 string)
(@ commands subwindow array 24 string)

(: RPG::System::TestBattler)
(@ level int)
(@ equips array 5 int)
(@ actor_id actor_id)

(: RPG::System)
; Magic.
(@ _ int)
(@ magic_number int)
(@ version_id int)
; AWESomeTypes (Armor Weapon Equipment Skill)
(@ armor_types subwindow arrayAL1 string)
(@ weapon_types subwindow arrayAL1 string)
(@ skill_types subwindow arrayAL1 string)
(@ terms subwindow RPG::System::Terms)
(@ game_title string)
(@ japanese boolean)
(@ currency_unit string)
(@ window_tone Tone)
(@ title1_name string)
(@ title2_name string)
(@ title_bgm RPG::BGM)
(@ battleback1_name string)
(@ battleback2_name string)
(@ battle_bgm RPG::BGM)
(@ battle_end_me RPG::ME)
(@ gameover_me RPG::ME)
; Now, on the one hand, this is clearly an array.
; On the other hand, the elements need to be named.
; I have a solution for cases like this.
(@ sounds subwindow RPG::System.sounds)
(@ boat subwindow RPG::System::Vehicle)
(@ ship subwindow RPG::System::Vehicle)
(@ airship subwindow RPG::System::Vehicle)
(@ start_map_id map_id)
(@ start_x int)
(@ start_y int)
(@ party_members subwindow array 0 actor_id)
(@ elements subwindow array 0 string)
(@ switches subwindow arrayIx1 subwindow string)
(@ variables subwindow arrayIx1 subwindow string)
(@ battler_name string)
(@ battler_hue hue)
(@ test_battlers subwindow array 0 subwindow RPG::System::TestBattler)
(@ test_troop_id troop_id)
(@ edit_map_id map_id)
(@ opt_extra_exp boolean)
(@ opt_followers boolean)
(@ opt_display_tp boolean)
(@ opt_use_midi boolean)
(@ opt_draw_title boolean)
(@ opt_slip_death boolean)
(@ opt_floor_death boolean)
(@ opt_transparent boolean)

(> File.System RPG::System)

(. RPG::Tileset.names)
(+ hide array 9 string)
(] 0 A1 f_tileset_name)
(] 1 A2 f_tileset_name)
(] 2 A3 f_tileset_name)
(] 3 A4 f_tileset_name)
(] 4 A5 f_tileset_name)
(] 5 B f_tileset_name)
(] 6 C f_tileset_name)
(] 7 D f_tileset_name)
(] 8 E f_tileset_name)

(: RPG::Tileset)
(vm
	(define-name Class.RPG::Tileset "Tileset " (@ @id) " (M" (@ @mode) "): " (@ @name))
)
(@ id index)
(@ name string)
(@ mode int= 1)
(@ note stringBlobEditor)
(+ subwindow: "Tile Attributes" internal_vxaTilesetFlags)
(@ tileset_names RPG::Tileset.names)

(> File.Tilesets arrayIx1 typeChanger{ nil 0 subwindow RPG::Tileset oRPG::Tileset })

(: RPG::Troop::Member)
(@ enemy_id enemy_id)
(@ x int)
(@ y int)
(@ hidden boolean)

(: RPG::Troop::Page::Condition)
(@ turn_valid boolean)
(@ turn_a int)
(@ turn_b int)
(@ turn_ending boolean)
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
(@ members subwindow array 0 RPG::Troop::Member)

(: RPG::Troop contextDictionary troop_enemy_id troop_enemy_id_base 1 @members 0 @enemy_id enemy_id rpg_troop_core)
(vm (rcom-idname RPG::Troop "Troop"))

(> File.Troops arrayIx1 subwindow RPG::Troop)

(: RPG::Weapon)
(vm (rcom-idname RPG::Weapon "Weapon"))
(@ id index)
(@ name string)
(@ note stringBlobEditor)
(@ description stringBlobEditor)
(@ icon_index int)
(@ price int)
(@ params battle_parameter_array)
(@ features subwindow array 0 subwindow RPG::BaseItem::Feature)
(@ etype_id etype_id)
(@ wtype_id wtype_id)
(@ animation_id animation_id)

(> File.Weapons arrayIx1 subwindow RPG::Weapon)

; File list:

; Actors.rvdata2
; Animations.rvdata2 [OK (though see 'To Win A Game That Has No End'-codenamed missing functionality)]
; Armors.rvdata2 [OK]
; Classes.rvdata2 [OK]
; CommonEvents.rvdata2 [OK]
; Enemies.rvdata2 [OK]
; Items.rvdata2 [OK]
; MapInfos.rvdata2 [OK]
; Scripts.rvdata2 [OK]
; Skills.rvdata2 [OK]
; States.rvdata2 [OK]
; System.rvdata2 [OK]
; Tilesets.rvdata2 [OK]
; Troops.rvdata2 [OK]
; Weapons.rvdata2 [OK]
