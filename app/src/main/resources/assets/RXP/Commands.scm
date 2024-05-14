
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; CommandDB

; I've moved the docs on this to IRB_CMDB.txt

; Confirmed Types (incomplete nowadays because of major usability work adding paramassist types):

;  int
;  string
;  string_array
;  int_boolean
;  int_or_var (alias for int, but the int can have meaning)
;  <the gazillion _id types>
;  direction (alias for int, but an enum. 2: Down, 4: Left, 6: Right, 8: Up)
;  animation_id (alias for int blah blah blah)
;  iterate_actor (0 means all, otherwise integer ID.)
;  iterate_battler0 (int, complicated)
;  iterate_battler1 (int, complicated)

;  selfswitch_id (a string with a set default value of "A")

;  opacity (int= 255)

;  enemy_action_kind (0 Basic 1 Skill 2 Item)
;  enemy_action_basic (0 Attack 1 Guard 2 Escape 3 Rest)
;  force_action_target (Actor/TroopEnemyID -2 Last -1 Random)

;  cms_type (0: panorama, 1: fog, 2: battleback)
;  change_text_options_position (0: up 1: middle 2: down)

;  conditional_branch_parameters
;  change_map_settings_parameters

;  weather_type (0: none, 1: rain, 2: storm, 3: snow)

;  <fully qualified object classname, "RPG::AudioFile" for example>

; notes:
;  operate_value(int_boolean:negate, int_boolean:isVariable, int_or_var:value)

; Basic aliasing is here for convenience (just within the file, and no fancy SchemaDB constructs)

; Completed pages:
;  (1-2 are base pages, no commands)
;  3, 4, 5, 6, 7.
;  (7 is the last page)
; In theory, it's complete.
; Course, there'll probably be schema bugs with all the commands.
; Automated tests are passing for the commands in <test subject name here>.fON

; [Early codes]

(\# RCOM/CommonCommands)

; 0: insert point in CommonCommands
; 108/408: Comments in CommonCommands

(\# RXP/CommandsI3)
(\# RXP/CommandsI4)
(\# RXP/CommandsI5)
(\# RXP/CommandsI6)
(\# RXP/CommandsI7)
