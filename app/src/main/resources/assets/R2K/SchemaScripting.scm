
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; -- bits and bobs

(e control_switches_type \0 single \1 range \2 indirect)
(e control_switches_change_mode \0 on \1 off \2 toggle)

(e message_options_position \0 top \1 centre \2 bottom)

(e get_actors_mode \0 wholeParty\(ign.target\) \1 actor \2 varActor)

(e change_battle_parameter \0 maxHP \1 maxMP \2 atk \3 def \4 spi \5 agi)

(e scripting_vehicletype \0 boat \1 ship \2 airShip)

; Weird undocumented 'feature' : -1 teleports party...
; Thanks to Ghabry for finding out and notifying me about this:
; <Ghabry> t20kdc: That's some implementation detail Cherry explained to me. Because the "Change Vehicle Command" checks if the party is in the affected vehicle and in that case moves the party, too. When the Party has no vehicle she has the vehicle ID -1. So "-1" results in the party being moved.
(e scripting_vehicletype_wp \0 boat \1 ship \2 airShip \-1 party)

(e change_sys_bgm_id \0 battle \1 victory \2 inn \3 boat \4 ship \5 airShip \6 gameOver)
(e change_sys_sfx_id \0 cursor \1 act \2 cancel \3 buzzer \4 battle \5 escape \6 enemyAttack \7 enemyHurt \8 actorHurt \9 dodge \10 enemyDeath \11 item)
(e change_sys_gfx_font \0 msGothic \1 msMincho)

(e change_transition_situation \0 mapChangePre \1 mapChangePost \2 enterBattlePre \3 enterBattlePost \4 leaveBattlePre \5 leaveBattlePost)
; also used by LDBSystem, need to figure out a better place for this
(e change_transition_type \0 default \1 fade \2 blocks \3 dwDown \4 dwUp \5 blindsA \6 blindsB \7 blindsC \8 scalingSquareI \9 scalingSquareO \10 moveScrUpDown \11 moveScrDownUp \12 moveScrLeftRight \13 moveScrRightLeft \14 verticalOverlap \15 horizontalOverlap \16 quads \17 zoom \18 pieces \19 haze \20 null \21 none)

; Notably, regarding SaveSystem usage, this necessitated a signed byte type for it...
(e transition_type \-1 default \0 fade \1 blocks \2 dwDown \3 dwUp \4 blindsA \5 blindsB \6 blindsC \7 scalingSquareI \8 scalingSquareO \9 moveScrUpDown \10 moveScrDownUp \11 moveScrLeftRight \12 moveScrRightLeft \13 verticalOverlap \14 horizontalOverlap \15 quads \16 zoom \17 pieces \18 haze \19 null \20 none)
(e transition_type_no_default \0 fade \1 blocks \2 dwDown \3 dwUp \4 blindsA \5 blindsB \6 blindsC \7 scalingSquareI \8 scalingSquareO \9 moveScrUpDown \10 moveScrDownUp \11 moveScrLeftRight \12 moveScrRightLeft \13 verticalOverlap \14 horizontalOverlap \15 quads \16 zoom \17 pieces \18 haze \19 null \20 none)

; (Pretty sure fog and sandstorm are 2k3, anyway)
(e weather_control_type \0 none \1 rain \2 snow \3 fog2k3 \4 sandstorm2k3)
(e weather_control_strength \0 little \1 medium \2 lots)

(e callevent_type \0 commonEvent \1 mapEvent \2 indirectMapEvent)

(e change_actor_class_skillfx \0 noChange \1 replaceSkills \2 addSkills)
(e change_actor_class_statsfx \0 noChange \1 halfStats \2 lvl1Stats \3 currentLevelStats)

(e camera_pan_control_mode \0 lockCamera \1 unlockCamera \2 linearMoveCamera \3 linearFocusOnPlayer)
(e camera_pan_control_direction \0 up \1 right \2 down \3 left)

(e forceescape_mode \0 escape \1 instaKillEnemiesAndWin \2 instaKillSingleEnemyAndWin)
(e change_enemy_hp_mode \0 direct \1 variable \2 currentHp%)

(e choice_index_magic \0 choice \4 invisibleCancel)
; This one's used for display, so the user sees "choice index invisibleCancel" on the display.
; They still have to manually bind it together though.
(e choice_index_magic_2 \4 invisibleCancel)
(E choice_begin_magic choiceIndex+1 \0 None \5 invisibleCancel)

; Various constants for use in CMDB.

(> int_default_1 int= \1)
(> int_default_10 int= \10)
(> int_default_31 int= \31)
(> int_default_50 int= \50)
(> int_default_100 int= \100)
(> int_default_200 int= \200)

; For tones, really.
(> int_default_128 int= \128)

(> picture_id internal_r2kPPPID int+1)

; NOTE: This is in halfwidth characters.
(> textbox_string stringLen \50)
