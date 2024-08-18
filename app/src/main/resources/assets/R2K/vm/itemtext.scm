; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; RPG::Actor

(define (r2k-rpg-database-actors-textprops ex tv jv)
	(if ex (dm-h-init jv))
	(dmth-prop ex (dm-i-ref tv "@name") jv "name")
	(dmth-prop ex (dm-i-ref tv "@title") jv "title")
	(dmth-prop ex (dm-i-ref tv "@battle_skillspanel_name") jv "battle_skillspanel_name")
)

(define r2k-rpg-database-actors-import-text (dmth-h-importer r2k-rpg-database-actors-textprops))
(define r2k-rpg-database-actors-export-text (dmth-h-exporter r2k-rpg-database-actors-textprops))

; RPG::Skill

(define (r2k-rpg-database-skills-textprops ex tv jv)
	(if ex (dm-h-init jv))
	(dmth-prop ex (dm-i-ref tv "@name") jv "name")
	(dmth-prop ex (dm-i-ref tv "@description") jv "description")
	(dmth-prop ex (dm-i-ref tv "@use_text_1_2KO") jv "use_text_1_2KO")
	(dmth-prop ex (dm-i-ref tv "@use_text_2_2KO") jv "use_text_2_2KO")
	(dmth-prop ex (dm-i-ref tv "@easyrpg_battle_message_2k3") jv "easyrpg_battle_message_2k3")
)
(define r2k-rpg-database-skills-import-text (dmth-h-importer r2k-rpg-database-skills-textprops))
(define r2k-rpg-database-skills-export-text (dmth-h-exporter r2k-rpg-database-skills-textprops))

; RPG::Item

(define (r2k-rpg-database-items-textprops ex tv jv)
	(if ex (dm-h-init jv))
	(dmth-prop ex (dm-i-ref tv "@name") jv "name")
	(dmth-prop ex (dm-i-ref tv "@description") jv "description")
	(dmth-prop ex (dm-i-ref tv "@easyrpg_using_message") jv "easyrpg_using_message")
)
(define r2k-rpg-database-items-import-text (dmth-h-importer r2k-rpg-database-items-textprops))
(define r2k-rpg-database-items-export-text (dmth-h-exporter r2k-rpg-database-items-textprops))

; name only

(define (r2k-rpg-database-nameonly-textprops ex tv jv)
	(dmth-prim ex (dm-i-ref tv "@name") jv)
)
(define r2k-rpg-database-nameonly-import-text (dmth-h-importer r2k-rpg-database-nameonly-textprops))
(define r2k-rpg-database-nameonly-export-text (dmth-h-exporter r2k-rpg-database-nameonly-textprops))

; string hash

(define r2k-rpg-database-strings-import-text (dmth-h-importer dmth-prim))
(define r2k-rpg-database-strings-export-text (dmth-h-exporter dmth-prim))

; RPG::Terms

(define r2k-rpg-database-terms-import-text (dmth-i-importer dmth-prim))
(define r2k-rpg-database-terms-export-text (dmth-i-exporter dmth-prim))

; RPG::State

(define (r2k-rpg-database-states-textprops ex tv jv)
	(if ex (dm-h-init jv))
	(dmth-prop ex (dm-i-ref tv "@name") jv "name")
	(dmth-prop ex (dm-i-ref tv "@msg_actor") jv "msg_actor")
	(dmth-prop ex (dm-i-ref tv "@msg_enemy") jv "msg_enemy")
	(dmth-prop ex (dm-i-ref tv "@msg_already") jv "msg_already")
	(dmth-prop ex (dm-i-ref tv "@msg_affected") jv "msg_affected")
	(dmth-prop ex (dm-i-ref tv "@msg_recovery") jv "msg_recovery")
)
(define r2k-rpg-database-states-import-text (dmth-h-importer r2k-rpg-database-states-textprops))
(define r2k-rpg-database-states-export-text (dmth-h-exporter r2k-rpg-database-states-textprops))

; RPG::BattleCommand

(define r2k-rpg-database-battlecommands-import-text (dmth-a-importer r2k-rpg-database-nameonly-textprops))
(define r2k-rpg-database-battlecommands-export-text (dmth-a-exporter r2k-rpg-database-nameonly-textprops))

; RPG::Database

(define (r2k-rpg-database-import-text target json)
	(r2k-rpg-database-actors-import-text (dm-i-ref target "@actors") (dm-h-ref json "actors"))
	(r2k-rpg-database-skills-import-text (dm-i-ref target "@skills") (dm-h-ref json "skills"))
	(r2k-rpg-database-items-import-text (dm-i-ref target "@items") (dm-h-ref json "items"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@enemies") (dm-h-ref json "enemies"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@troops") (dm-h-ref json "troops"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@terrains") (dm-h-ref json "terrains"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@attributes") (dm-h-ref json "attributes"))
	(r2k-rpg-database-states-import-text (dm-i-ref target "@states") (dm-h-ref json "states"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@animations") (dm-h-ref json "animations"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@tilesets") (dm-h-ref json "tilesets"))
	(r2k-rpg-database-terms-import-text (dm-i-ref target "@terms") (dm-h-ref json "terms"))
	(r2k-rpg-database-strings-import-text (dm-i-ref target "@switches") (dm-h-ref json "switches"))
	(r2k-rpg-database-strings-import-text (dm-i-ref target "@variables") (dm-h-ref json "variables"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@common_events") (dm-h-ref json "commonEvents"))
	(r2k-rpg-database-battlecommands-import-text
		(dm-i-ref (dm-i-ref target "@battle_commands_2k3") "@commands")
		(dm-h-ref json "battleCommands"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@classes_2k3") (dm-h-ref json "classes"))
	(r2k-rpg-database-nameonly-import-text (dm-i-ref target "@battle_anim_sets_2k3") (dm-h-ref json "battleAnimSets"))
)

(define (r2k-rpg-database-export-text target json)
	(dm-h-init json)
	(r2k-rpg-database-actors-export-text (dm-i-ref target "@actors") (dm-h-add! json "actors"))
	(r2k-rpg-database-skills-export-text (dm-i-ref target "@skills") (dm-h-add! json "skills"))
	(r2k-rpg-database-items-export-text (dm-i-ref target "@items") (dm-h-add! json "items"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@enemies") (dm-h-add! json "enemies"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@troops") (dm-h-add! json "troops"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@terrains") (dm-h-add! json "terrains"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@attributes") (dm-h-add! json "attributes"))
	(r2k-rpg-database-states-export-text (dm-i-ref target "@states") (dm-h-add! json "states"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@animations") (dm-h-add! json "animations"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@tilesets") (dm-h-add! json "tilesets"))
	(r2k-rpg-database-terms-export-text (dm-i-ref target "@terms") (dm-h-add! json "terms"))
	(r2k-rpg-database-strings-export-text (dm-i-ref target "@switches") (dm-h-add! json "switches"))
	(r2k-rpg-database-strings-export-text (dm-i-ref target "@variables") (dm-h-add! json "variables"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@common_events") (dm-h-add! json "commonEvents"))
	(r2k-rpg-database-battlecommands-export-text
		(dm-i-ref (dm-i-ref target "@battle_commands_2k3") "@commands")
		(dm-h-add! json "battleCommands"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@classes_2k3") (dm-h-add! json "classes"))
	(r2k-rpg-database-nameonly-export-text (dm-i-ref target "@battle_anim_sets_2k3") (dm-h-add! json "battleAnimSets"))
)
