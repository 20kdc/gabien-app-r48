
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; VX Ace Schema.

(A RVXA/AutoTiles RVXA/AutoTileRules RVXA/WallAT $WallATs$ RVXA/WaterfallAT .)

(D internal_tilesetDictionary \1 Tilesets \0 @name)
(D internal_mapDictionary \1 MapInfos \1 @name)
(D internal_itemDictionary \1 Items \0 @name)
(D internal_commevDictionary \1 CommonEvents \0 @name)
(D internal_animationDictionary \1 Animations \0 @name)
(D internal_elementDictionary \1 System@elements \0 :)
(D internal_variableDictionary \1 System@variables \0 :)
(D internal_switchDictionary \1 System@switches \0 :)

(D internal_actorDictionary \1 Actors \0 @name)
(D internal_skillDictionary \1 Skills \0 @name)
(D internal_stateDictionary \1 States \0 @name)
(D internal_weaponDictionary \1 Weapons \0 @name)
(D internal_armourDictionary \1 Armors \0 @name)
(D internal_troopDictionary \1 Troops \0 @name)
(D internal_classDictionary \1 Classes \0 @name)

(D internal_enemyDictionary \1 Enemies \0 @name)

(D internal_atypeDictionary \1 System@armor_types \0 :)
(D internal_wtypeDictionary \1 System@weapon_types \0 :)
(D internal_etypeDictionary \1 System@terms@etypes \0 :)
(D internal_stypeDictionary \1 System@skill_types \0 :)

(D internal_eventDictionary \1 __MAP__@events \1 @name)

(> string_array subwindow array \0 string)

(> tileset_id internal_tilesetDictionary)
(> map_id internal_mapDictionary)
(> item_id internal_itemDictionary)
(> commonevent_id internal_commevDictionary)
(> actor_id internal_actorDictionary)
(> state_id internal_stateDictionary)
(> animation_id internal_animationDictionary)
(> element_id internal_elementDictionary)
; not sure if there is any dictionary for this
(> balloon_id int)
(> weapon_id internal_weaponDictionary)
(> armour_id internal_armourDictionary)
(> skill_id internal_skillDictionary)
(> troop_id internal_troopDictionary)
(> class_id internal_classDictionary)
(> var_id internal_variableDictionary)
(> switch_id internal_switchDictionary)

; Not to be confused with troop_enemy_id (which is used 99.9% of the time)
(> enemy_id internal_enemyDictionary)

; the AWESomeTypes
(> atype_id internal_atypeDictionary)
(> wtype_id internal_wtypeDictionary)
(> etype_id internal_etypeDictionary)
(> stype_id internal_stypeDictionary)

(> selfswitch_id string= A)

(e vehicle_id \0 boat \1 ship \2 airship)

(> int_or_var int)
(> opacity int= \255)
(> scale int= \100)

(E troop_enemy_id_base TroopEnemyIndex \-1 All)
(> troop_enemy_id context? troop_enemy_id troop_enemy_id_base)

(E character_id_base EventID \-1 player \0 thisEvent)
(M internal_eventDictionary character_id_base character_id)

(E iterate_actor_id_base ActorID \0 All)
(M internal_actorDictionary iterate_actor_id_base iterate_actor_id)

(E iterate_enemy EnemyIdx \-1 All)

; -1 may be supported by MKXP, meaning KeepDestAlpha, but nobody else supports it.
(e blend_type \0 Normal \1 Add \2 Sub)

(> f_animss_name { string imgSelector Graphics/Animations/ Animations/ })
(> f_char_name { string imgSelector Graphics/Characters/ Characters/ })
(> f_face_name { string imgSelector Graphics/Faces/ Faces/ })
(> f_picture_name { string imgSelector Graphics/Pictures/ Pictures/ })
(> f_parallax_name { string imgSelector Graphics/Parallaxes/ Parallaxes/ })
(> f_tileset_name { string imgSelector Graphics/Tilesets/ Tilesets/ })

(C spritesheet Select\ character\ index... Characters/ vxaCharacter)
(C spritesheet Select\ face\ index... Faces/ \96 \96 \4 \0 \0 \96 \96 \0)

; yay native objects
(> Color CTNative Color)
(> Tone CTNative Tone)

(C datum \
\(define-name\ Class.RPG::SE\ \(@\ @name\)\ \"\ p.\"\ \(@\ @pitch\)\ \"\ v.\"\ \(@\ @volume\)\)\
\(define-name\ Class.RPG::ME\ \(@\ :\ Class.RPG::SE\)\)\
\(define-name\ Class.RPG::BGM\ \(@\ :\ Class.RPG::SE\)\)\
\(define-name\ Class.RPG::BGS\ \(@\ :\ Class.RPG::SE\)\)\
)

(: RPG::ME)
(@ name { string fileSelector Audio/ME/ })
(@ pitch int= \100)
(@ volume int= \100)

(: RPG::SE)
(@ name { string fileSelector Audio/SE/ })
(@ pitch int= \100)
(@ volume int= \100)

(: RPG::BGM)
(@ name { string fileSelector Audio/BGM/ })
(@ pitch int= \100)
(@ volume int= \100)

(: RPG::BGS)
(@ name { string fileSelector Audio/BGS/ })
(@ pitch int= \100)
(@ volume int= \100)
