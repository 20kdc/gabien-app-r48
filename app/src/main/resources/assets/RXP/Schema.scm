
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; XP Schema.

; THE RXP SCHEMA FILE
; <Documentation moved to IRB_SDB.txt

; defining internal dictionaries

(D internal_tilesetDictionary 1 Tilesets 0 @name "" RPG::Tileset)
(D internal_mapDictionary 1 MapInfos 1 @name "" RPG::MapInfo)
(D internal_itemDictionary 1 Items 0 @name "" RPG::Item)
(D internal_animationDictionary 1 Animations 0 @name "" RPG::Animation)
(D internal_actorDictionary 1 Actors 0 @name "" RPG::Actor)
(D internal_commevDictionary 1 CommonEvents 0 @name "" RPG::CommonEvent)
(D internal_skillDictionary 1 Skills 0 @name "" RPG::Skill)
(D internal_stateDictionary 1 States 0 @name "" RPG::State)
(D internal_weaponDictionary 1 Weapons 0 @name "" RPG::Weapon)
(D internal_armourDictionary 1 Armors 0 @name "" RPG::Armor)
(D internal_troopDictionary 1 Troops 0 @name "" RPG::Troop)
(D internal_classDictionary 1 Classes 0 @name "" RPG::Class)
(D internal_elementDictionary 1 System@elements 0 : "" string)
(D internal_variableDictionary 1 System@variables 0 : "" string)
(D internal_switchDictionary 1 System@switches 0 : "" string)

(D internal_enemyDictionary 1 Enemies 0 @name "" RPG::Enemy)

(D internal_eventDictionary 1 __MAP__@events 1 @name "" RPG::Event)

; Defining the basic aliases...
; Note that if you're unsure where to put it, put it here.

(> int_or_var int)
(> selfswitch_id string= A)
(> opacity int= 255)
(> zoom int= 100)

(E iterate_enemy_base EnemyID -1 All)

; Binds contextual troop enemy ID to actual troop enemy ID
(> troop_enemy_id context? troop_enemy_id int)
(> iterate_enemy context? iterate_enemy iterate_enemy_base)

; Defining dictionaries...
; If it's an ID in any game-global dictionary, put it here.

(> tileset_id internal_tilesetDictionary)
(> map_id internal_mapDictionary)
(> item_id internal_itemDictionary)
(> animation_id internal_animationDictionary)
(> actor_id internal_actorDictionary)
(> commonevent_id internal_commevDictionary)
(> skill_id internal_skillDictionary)
(> state_id internal_stateDictionary)
(> weapon_id internal_weaponDictionary)
(> armour_id internal_armourDictionary)
(> troop_id internal_troopDictionary)
(> class_id internal_classDictionary)
(> element_id internal_elementDictionary)
(> var_id internal_variableDictionary)
(> switch_id internal_switchDictionary)

; NOTE: Do not get confused with troop_enemy_id.
;       This is an entry in the Enemies table.
(> enemy_id internal_enemyDictionary)

(> f_pano_name { string imgSelector Graphics/Panoramas/ Panoramas/ })
(> f_bb_name { string imgSelector Graphics/Battlebacks/ Battlebacks/ })
(> f_fog_name { string imgSelector Graphics/Fogs/ Fogs/ })
(> f_char_name { string imgSelector Graphics/Characters/ Characters/ })
(> f_icon_name { string imgSelector Graphics/Icons/ Icons/ })
(> f_tileset_name { string imgSelector Graphics/Tilesets/ Tilesets/ })
(> f_autotile_name { string imgSelector Graphics/Autotiles/ Autotiles/ })
(> f_animss_name { string imgSelector Graphics/Animations/ Animations/ })
(> f_battler_name { string imgSelector Graphics/Battlers/ Battlers/ })
(> f_ws_name { string imgSelector Graphics/Windowskins/ Windowskins/ })
(> f_title_name { string imgSelector Graphics/Titles/ Titles/ })
(> f_go_name { string imgSelector Graphics/Gameovers/ Gameovers/ })
(> f_picture_name { string imgSelector Graphics/Pictures/ Pictures/ })

; Defining the enums.
; In most cases these are global, so just don't move them from here.

(E character_id_base EventID -1 player 0 thisEvent)
(M internal_eventDictionary character_id_base character_id)

(E id_or_none_base ID 0 none)

; or-none dictionaries. Unfinished, add as needed
(M internal_commevDictionary id_or_none_base commonevent_id_or_none)
(M internal_skillDictionary id_or_none_base skill_id_or_none)
(M internal_stateDictionary id_or_none_base state_id_or_none)
(M internal_weaponDictionary id_or_none_base weapon_id_or_none)
(M internal_armourDictionary id_or_none_base armour_id_or_none)
(M internal_troopDictionary id_or_none_base troop_id_or_none)
(M internal_classDictionary id_or_none_base class_id_or_none)

(e comparison_type 0 == 1 >= 2 <= 3 > 4 < 5 !=)
(e change_text_options_position 0 top 1 middle 2 bottom)

(E iterate_actor_base ActorID 0 All)
(M internal_actorDictionary iterate_actor_base iterate_actor)

(e iterate_battler0 0 EnemyIx 1 ActorIx)
(E iterate_battler1 ID -1 All)
(e weather_type 0 none 1 rain 2 storm 3 snow)
; -1 may be supported by MKXP, meaning "KeepDestAlpha", but nobody else supports it.
(e blend_type 0 Normal 1 Add 2 Sub)

; ---- not quite sure about this bit
(e enemy_action_kind 0 Basic 1 Skill 2 Item)
(e enemy_action_basic 0 Attack 1 Guard 2 Escape 3 Rest)
(> enemy_action_basic_skill_item_id int)
(E force_action_target Actor/TroopEnemyID -2 Last -1 Random)
; ----

; event_trigger NOTE. 4 is used for West exit of LP,
;  which acts as a vertical trigger via a Ruby sub.
; Meanwhile, 3 is used for *both* pages of the Map Events in the RV map.

; Defining bits and pieces...

; This is actually a native serialized object
(> Color CTNative Color)
(> Tone CTNative Tone)

(: RPG::AudioFile)
(vm
	(define-name Class.RPG::AudioFile (@ @name) " p." (@ @pitch) " v." (@ @volume))
)
; This depends on context, apparently, so cannot be filenamed.
(@ name string)
(@ pitch int= 100)
(@ volume int= 100)

(. rpg_audiofile_bgm)
(+ hide RPG::AudioFile)
(@ name { string fileSelector Audio/BGM/ })
(+ soundPlayerComplex Audio/BGM/ @name @volume @pitch .)
(@ pitch int= 100)
(@ volume int= 100)

(. rpg_audiofile_bgs)
(+ hide RPG::AudioFile)
(@ name { string fileSelector Audio/BGS/ })
(+ soundPlayerComplex Audio/BGS/ @name @volume @pitch .)
(@ pitch int= 100)
(@ volume int= 100)

(. rpg_audiofile_me)
(+ hide RPG::AudioFile)
(@ name { string fileSelector Audio/ME/ })
(+ soundPlayerComplex Audio/ME/ @name @volume @pitch .)
(@ pitch int= 100)
(@ volume int= 100)

(. rpg_audiofile_se)
(+ hide RPG::AudioFile)
(@ name { string fileSelector Audio/SE/ })
(+ soundPlayerComplex Audio/SE/ @name @volume @pitch .)
(@ pitch int= 100)
(@ volume int= 100)
