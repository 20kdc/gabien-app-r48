
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; [Interpreter Part 6]

(cmd 301 "Start Battle (c.601/602/603/604)")
(d "Starts a battle. If one of the Battle Result Branch commands follows, then all of them should (allowing gaps for each branch).")
(C category 4)
(p troop troop_id)
(p canEscape boolean)
(p losingDoesNotGameover boolean)

; Battle branches (601/602/603/604) in CommonCommands

; These 2 have identical schemas
(cmd 302 "Shop Items (c.605)")
(C category 0)
(d "Shop items. This gets followed by 605 additional items.")
(X shop_items_parameters)
(C groupBehavior messagebox 605)
(cmd 605 "Additional Item")
(C category 0)
(d "An additional item for 302. Otherwise pointless.")
(X shop_items_parameters)

; 303 Name Actor in RCOM/CommonCommands

; [ Gap here between 303 to 311 ]

(cmd 311 "Actor" ($ " " ]0 iterate_actor #t) " HP +=" ($ " " : operate_value_1) (? ]0 (if-eq ]4 #t " (can kill)" " (never kills)")))
(C category 2)
(d "Adds/removes HP to/from actor/party.")
(p actor iterate_actor)
; operate_value
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_100)
(p canKill boolean)

(cmd 312 "Actor" ($ " " ]0 iterate_actor #t) " SP +=" ($ " " : operate_value_1))
(d "Adds/removes SP to/from actor/party.")
(C category 2)
(p actor iterate_actor)
; operate_value
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_10)

(cmd 313 (? ]1 (if-eq ]1 0 "Add" "Remove") "Change") " Actor" ($ " " ]0 iterate_actor #t) " State" ($ " " ]2 state_id #t))
(d "Adds/removes a state to/from actor/party.")
(C category 2)
(p actor iterate_actor)
(p remove int_boolean)
(p state state_id)

(cmd 314 "Full recover on actor" ($ " " ]0 iterate_actor #t))
(d "Fully recovers actor/party to full health and SP.")
(C category 2)
(p actor iterate_actor)

(cmd 315 "Actor" ($ " " ]0 iterate_actor #t) " EXP +=" ($ " " : operate_value_1))
(d "Adds/removes EXP to/from actor/party.")
(C category 2)
(p actor iterate_actor)
; operate_value
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_100)

(cmd 316 "Actor" ($ " " ]0 iterate_actor #t) " LVL +=" ($ " " : operate_value_1))
(d "Adds/removes levels to/from actor/party.")
(C category 2)
(p actor iterate_actor)
; operate_value
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P 2 modVar var_id)
(v 0 mod int_default_1)

(cmd 317 "Change Actor" ($ " " ]0 actor_id #t) " Parameter" ($ " " ]1 cap_type) ($ " by " : operate_value_2))
(d "Adds/removes parameter points to/from actor/party.")
(C category 2)
(p actor actor_id)
(p param cap_type)
; operate_value
(p modNegate int_boolean)
(p modIsVar int_boolean)
(P 3 modVar var_id)
(v 0 mod int_default_10)

(cmd 318 (? ]2 (if-eq ]2 0 "Add" "Remove") "Change") " Actor" ($ " " ]0 actor_id #t) " Skill" ($ " " ]3 skill_id #t))
(d "Adds/removes a skill to/from an actor.")
(C category 2)
(p actor actor_id)
(p unlearn boolean)
(p skill skill_id)

(cmd 319 "Actor" ($ " " ]0 actor_id #t) " equips" ($ " " ]1 actor_equip_type) (if-eq ]1 0 ($ " " ]2 weapon_id #t) ($ " " ]2 armour_id #t)))
(d "Adds/removes equipment to/from an actor. (If the equipment is not available, it is automatically given, but only if it is not already available.)")
(C category 2)
(p actor actor_id)
(p type actor_equip_type)
(P 1 equip int)
(v 0 equipWeapon weapon_id)
(v 1 equipArmour armour_id)
(v 2 equipArmour armour_id)
(v 3 equipArmour armour_id)
(v 4 equipArmour armour_id)

(cmd 320 "Rename Actor" ($ " " ]0 actor_id) ($ " to " ]1))
(d "Forcefully renames an actor.")
(C category 2)
(p actor actor_id)
(p newName string)

(cmd 321 "Reclass Actor" ($ " " ]0 actor_id) ($ " to " ]1 class_id))
(d "Changes the class of an actor.")
(C category 2)
(p actor actor_id)
(p newClass class_id)

(cmd 322 "Change Actor" ($ " " ]0 actor_id) " Graphic" ($ " to " ]1))
(d "Changes the graphics of an actor.")
(C category 2)
(p actor actor_id)
(p charName f_char_name)
(p charHue hue)
(p battleName f_battler_name)
(p bettleHue hue)
