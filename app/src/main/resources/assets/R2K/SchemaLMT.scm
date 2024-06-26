
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(e mapinfo_musictype 0 inherit 1 ignored 2 specified)
(e mapinfo_backtype 0 inherit 1 terrainLDB 2 specified)
(e mapinfo_inheritflag 0 inheritParent 1 true 2 false)
(e mapinfo_type 1 map 2 area 0 root)

(: RPG::Encounter)
(@ troop troop_id)

(: RPG::MapInfo)
(vm
	(define-name Class.RPG::MapInfo "MapInfo: " (@ @name))
)
(@ name string)
(@ parent_id map_id)
(@ indent int= 1)
(@ type mapinfo_type)
(@ OFED_edit_pos_x int)
(@ OFED_edit_pos_y int)
(@ OFED_expanded boolean)
(@ music_type mapinfo_musictype)
(+ label r2kMusicType
"If @music_type is inherit, both @music and @music_type are inherited from the parent map.
If @music_type is ignored, the music is not changed (whatever was already playing remains playing).
If @music_type is specified, the music to play is specified by the @music value.")
(@ music RPG::Music)
(@ background_type mapinfo_backtype)
(@ background_name f_battleback_name)
(@ teleport_state mapinfo_inheritflag)
(@ escape_state mapinfo_inheritflag)
(@ save_state mapinfo_inheritflag)
(@ encounters subwindow arrayIx1 RPG::Encounter)
(@ encounter_steps int= 25)
(@ area_rect Rect)

(: RPG::Start)
(@ player_map map_id)
(@ player_x int)
(@ player_y int)
(+ mapPositionHelper @player_map @player_x @player_y)
(+ optP @boat_map map_or_none_id)
(+ optP @boat_x int)
(+ optP @boat_y int)
(+ mapPositionHelper @boat_map @boat_x @boat_y)
(+ optP @ship_map map_or_none_id)
(+ optP @ship_x int)
(+ optP @ship_y int)
(+ mapPositionHelper @ship_map @ship_x @ship_y)
(+ optP @airship_map map_or_none_id)
(+ optP @airship_x int)
(+ optP @airship_y int)
(+ mapPositionHelper @airship_map @airship_x @airship_y)

(: RPG::MapTree)
(@ map_infos subwindow hash int+0 subwindow RPG::MapInfo)
(@ map_order subwindow array 0 map_id)
(@ active_node map_id)
(@ start subwindow: "Player/Vehicle Start Point" RPG::Start)
; Supplies defaults
(C magicR2kSystemDefaults 1)

(> File.RPG_RT.lmt RPG::MapTree)
