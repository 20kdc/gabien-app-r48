
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Defining the script commands...

(> genericScriptParameter typeChanger{ genericScriptParametersSW [ RPG::MoveRoute oRPG::MoveRoute RPG::MoveCommand oRPG::MoveCommand RPG::AudioFile oRPG::AudioFile string "\"" boolean T boolean F int i })

(> EventListEditor subwindow arrayCS RPG::EventCommand EventCommandEditor_Most event)
(> MoveListEditor subwindow arrayMS RPG::MoveCommand MoveCommandEditor_Most move)

; Defining MoveRoute down here since it depends on things above

(: RPG::MoveRoute)
(@ list MoveListEditor)
(@ repeat booleanDefTrue)
(@ skippable boolean)

; Defining Events...

(: RPG::Event::Page::Condition)
(vm
	(define-name RPG::Event::Page::Condition.switchA (if-eq @switch1_valid #t (
		", Switch " (@ @switch1_id switch_id #t)
	)))
	(define-name RPG::Event::Page::Condition.switchB (if-eq @switch2_valid #t (
		", Switch " (@ @switch2_id switch_id #t)
	)))
	(define-name RPG::Event::Page::Condition.var (if-eq @variable_valid #t (
		", Var " (@ @variable_id var_id #t) " " (@ @variable_value)
	)))
	(define-name RPG::Event::Page::Condition.selfSwitch (if-eq @self_switch_valid #t (
		", Self-Switch " (@ @self_switch_ch selfswitch_id #t)
	)))
	(define-name Class.RPG::Event::Page::Condition
		(@ : RPG::Event::Page::Condition.switchA) (@ : RPG::Event::Page::Condition.switchB)
		(@ : RPG::Event::Page::Condition.var) (@ : RPG::Event::Page::Condition.selfSwitch)
	)
)
(@ switch1_valid boolean)
(@ switch1_id switch_id)
(@ switch2_valid boolean)
(@ switch2_id switch_id)
(@ variable_valid boolean)
(@ variable_id var_id)
(@ variable_value int)
(@ self_switch_valid boolean)
(@ self_switch_ch selfswitch_id)

(e rpg_event_page_graphic_pattern_ 0 Stand 1 "Walk A" 2 "Walk B" 3 "Walk C")
(> rpg_event_page_graphic_pattern ui rpg_event_page_graphic_pattern_ halfsplit halfsplit valButton 0 "Stand" valButton 1 "Walk A" halfsplit valButton 2 "B" valButton 3 "C")

(: RPG::Event::Page::Graphic)
(@ opacity opacity)
(@ pattern rpg_event_page_graphic_pattern)
(@ character_name f_char_name)
(@ tile_id int)
(+ halfsplitPost eventTileHelper @tile_id @character_name 0 RXP/TSTables valButton 0 Clear)
(+ label rxpEventGraphics
"If @tile_id is not 0, then the event is displayed as a tile. Otherwise, @character_name refers to the spritesheet.
Importantly, tile events, when collidable (@through is false) use the collision rules of the chosen tile, as if the event occupied a layer above all existing tile layers.")
(@ direction direction)
(@ blend_type blend_type)
(@ character_hue hue)
(+ internal_EPGD)

(e event_trigger 0 onInteract 1 onPlayerWalkAgainst 2 onEventWalkAgainst 3 autorun 4 parallel)
(e event_movetype 0 fixed 1 random 2 approachPlayer 3 moveRoute)

(: RPG::Event::Page)
(vm
	(define-name Class.RPG::Event::Page (@ @condition))
)
(@ condition subwindow RPG::Event::Page::Condition)
(@ through boolean)
(@ move_type event_movetype)
(@ move_route RPG::MoveRoute)
(@ move_speed int= 3)
(@ move_frequency int= 3)
(@ step_anime boolean)
(@ walk_anime boolean)
(@ always_on_top boolean)
(@ direction_fix boolean)
(@ trigger event_trigger)
(@ list EventListEditor)
(@ graphic { subwindow RPG::Event::Page::Graphic internal_EPGD })

(: RPG::Event)
(@ id index)
(@ x int)
(@ y int)
(@ name string)
(@ pages arrayPAL1 RPG::Event::Page)

; Defining Map.

(: RPG::Map)
(@ bgm rpg_audiofile_bgm)
(@ bgs rpg_audiofile_bgs)
(@ width roint= 20)
(@ height roint= 15)
(+ tableD @data @width @height 3 20 15 3 48 0 0)
(@ events subwindow hash int+1 subwindow RPG::Event)
(@ tileset_id tileset_id)
(@ autoplay_bgm boolean)
(@ autoplay_bgs boolean)

; Limited information here. Pretty sure this is it.
(@ encounter_list subwindow array 0 troop_id)
(@ encounter_step int= 30)
