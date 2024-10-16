
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(D internal_mapDictionary 1 RPG_RT.lmt@map_infos 1 @name #{}# RPG::MapInfo)
(D internal_eventDictionary 1 __MAP__@events 1 @name #{}# RPG::Event)

(D internal_variableDictionary 1 RPG_RT.ldb@variables 1 : "" string)
(D internal_switchDictionary 1 RPG_RT.ldb@switches 1 : "" string)

(D internal_tilesetDictionary 1 RPG_RT.ldb@tilesets 1 @name #{}# RPG::Tileset)
(D internal_terrainDictionary 1 RPG_RT.ldb@terrains 1 @name #{}# RPG::Terrain)
(D internal_itemDictionary 1 RPG_RT.ldb@items 1 @name #{}# RPG::Item)
(D internal_troopDictionary 1 RPG_RT.ldb@troops 1 @name #{}# RPG::Troop)
(D internal_skillDictionary 1 RPG_RT.ldb@skills 1 @name #{}# RPG::Skill)
(D internal_stateDictionary 1 RPG_RT.ldb@states 1 @name #{}# RPG::State)
(D internal_cevDictionary 1 RPG_RT.ldb@common_events 1 @name #{}# RPG::CommonEvent)
(D internal_classDictionary 1 RPG_RT.ldb@classes_2k3 1 @name #{}# RPG::Class)
(D internal_animDictionary 1 RPG_RT.ldb@animations 1 @name #{}# RPG::Animation)
(D internal_enemyDictionary 1 RPG_RT.ldb@enemies 1 @name #{}# RPG::Enemy)

(D internal_actorDictionary 1 RPG_RT.ldb@actors 1 @name #{}# RPG::Actor)
(D internal_attributeDictionary 1 RPG_RT.ldb@attributes 1 @name #{}# RPG::Attribute)
(D internal_battleanimationDictionary 1 RPG_RT.ldb@battle_anim_sets_2k3 1 @name #{}# RPG::BattlerAnimationSet)

(D internal_battlecommandDictionary 1 RPG_RT.ldb@battle_commands_2k3@commands 0 @name #{}# RPG::BattleCommand)

(> f_charset_name { string= char imgSelector CharSet/ CharSet/ })
; NOTE: The default here is blank, this just stops it crashing
(> f_save_charset_name { string imgSelector CharSet/ CharSet/ })
(> f_faceset_name { string imgSelector FaceSet/ FaceSet/ })
; "nap" versions are without audio preview
(> f_music_name { string= (OFF) fileSelector Music/ soundPlayer Music/ })
(> f_music_name_nap { string= (OFF) fileSelector Music/ })
(> f_sound_name { string= (OFF) fileSelector Sound/ soundPlayer Sound/ })
(> f_sound_name_nap { string= (OFF) fileSelector Sound/ })
(> f_system_name { string= System imgSelector System/ System/ })
; NOTE: The default here is blank, this makes it use the game-picked System
(> f_save_system_name { string imgSelector System/ System/ })
; This used to default to 'system2a', but that causes issues on RPG_RT unless you *have* a system2a handy.
; Not that you should be synthing against RPG_RT, but compatibility is king
(> f_system2_name { string imgSelector System2/ System2/ })
(> f_parallax_name { string imgSelector Panorama/ Panorama/ })
(> f_picture_name { string imgSelector Picture/ Picture/ })
(> f_movie_name { string fileSelector Movie/ })
(> f_battleback_name { string= backdrop imgSelector Backdrop/ Backdrop/ })
(> f_monster_name { string= monster imgSelector Monster/ Monster/ })
(> f_battle_name { string imgSelector Battle/ Battle/ })
(> f_battle2_name { string imgSelector Battle2/ Battle2/ })

(C spritesheet "Select character index..." CharSet/ r2kCharacter)
(C spritesheet "Select face index..." FaceSet/ 48 48 4 0 0 48 48 0)

; Var/Switch
(> var_id internal_variableDictionary)
(E var_or_none_id__ Var.ID 0 None)
(M internal_variableDictionary var_or_none_id__ var_or_none_id)

(> switch_id internal_switchDictionary)

(vm
	(define-tr R2KIDC.Var "Var. ID")
	(define-tr R2KIDC.Switch "Switch. ID")

	(idchanger-add R2KIDC.Var (quote (var_id var_or_none_id)))
	(idchanger-add R2KIDC.Switch (quote (switch_id)))
)

; Continue...
(> item_id internal_itemDictionary)
(E item_or_none_id__ Item.ID 0 None)
(M internal_itemDictionary item_or_none_id__ item_or_none_id)

(> skill_id internal_skillDictionary)
(> state_id internal_stateDictionary)

; It's magic, you know...?
; Never believe it's not so.
(. map_id)
(+ internal_mapDictionary)
(C magicR2kSystemDefaults 4)

(E map_or_none_id__ Map.ID 0 None)
(M internal_mapDictionary map_or_none_id__ map_or_none_id)

(> troop_id internal_troopDictionary)
; not to be confused with an enemy within a troop
(> enemy_id internal_enemyDictionary)

; Binds contextual troop enemy ID to actual troop enemy ID
(> troop_enemy_id context? troop_enemy_id int)

(> tileset_id internal_tilesetDictionary)
(> save_tileset_id { hide int= 0 tileset_id })
(> terrain_id internal_terrainDictionary)

(> actor_id internal_actorDictionary)
(> attribute_id internal_attributeDictionary)

; If you even see any of these mentioned at all, it is 2k3 only. *Always*.
(> battleranimation_id internal_battleanimationDictionary)

(> animation_id internal_animDictionary)
(E animation_or_none_id__ Anim.ID 0 None)
(M internal_animDictionary animation_or_none_id__ animation_or_none_id)

(> class_id internal_classDictionary)
(E class_or_none_id__ Class.ID 0 None)
(M internal_classDictionary class_or_none_id__ class_or_none_id)
(E class_complex_id__ Class.ID -1 Unset 0 None)
(M internal_classDictionary class_complex_id__ class_complex_id)

(> battlecommand_id internal_battlecommandDictionary)

(E battlecommand_or_none_id__ BC.ID 0 <None>)
(M internal_battlecommandDictionary battlecommand_or_none_id__ battlecommand_or_none_id)

(E battlecommand_menu_id__ BC.ID -1 <Blank> 0 Row)
(M internal_battlecommandDictionary battlecommand_menu_id__ battlecommand_menu_id)

(> commonevent_id internal_cevDictionary)
(E commonevent_or_none_id__ CEV.ID 0 None)
(M internal_cevDictionary commonevent_or_none_id__ commonevent_or_none_id)

(E character_id__ EventID 10001 player 10002 boat 10003 ship 10004 airShip 10005 runningEvent)
(M internal_eventDictionary character_id__ character_id)

(E event_or_none_id__ EventID 0 None)
(M internal_eventDictionary event_or_none_id__ event_or_none_id)

(e timer_id 0 timer1 1 timer2)

; NOTE: This is barely ever used, in contrast to RGSS. In general stuff uses sprite_direction
(e direction 2 down 4 left 6 right 8 up)

(e sprite_direction 2 down 0 up 3 left 1 right)

(> eventpage_movefreq int= 6)
(> eventpage_movespeed int= 3)

(: Rect)
(@ left int)
(@ up int)
(@ right int)
(@ down int)

; Internal class used to give bitfields a theoretical Ruby type
(: __bitfield__)

(: RPG::Music)
(@ name f_music_name_nap)
(+ soundPlayerComplex Music/ @name @volume @tempo @balance)
(@ fadeTime int)
(@ volume int= 100)
(@ tempo int= 100)
(@ balance int= 50)

(: RPG::Sound)
(@ name f_sound_name_nap)
(+ soundPlayerComplex Sound/ @name @volume @tempo @balance)
(@ volume int= 100)
(@ tempo int= 100)
(@ balance int= 50)
