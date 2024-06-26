
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; BatEffects/BatFeatures definitions.
; Should contain ALL RPG::*Item::<name here> classes.
; Used in SchemaFiles for all those pesky battle bits & pieces


; Thanks to Arri for helping me correct these.
(e parameter_id \0 maxHP \1 maxMP \2 attack \3 defense \4 magicAttack \5 magicDefense \6 agility \7 luck)

(e exparameter_id \0 hit \1 evasion \2 critical \3 criticalEvade \4 magicEvade \5 magicReflect \6 counter \7 hpRegen \8 mpRegen \9 tpRegen)
(e spparameter_id \0 targetRate \1 guardEffect \2 recoveryEffect \3 pharmacology \4 mpCost \5 tpCharge \6 physDam \7 magiDam \8 floorDm \9 expRate)
(e battle_specialflag_id \0 autoBattle \1 guard \2 substitute \3 preserveTP)
(e battle_specialeffect_id \0 Escape)
(e battle_collapsetype \0 normal \1 boss \2 instant)
(e battle_partyability_id \0 halfEncounters \1 disableEncounters \2 cancelSurprise \3 raisePreemptiveRate \4 doubleGold \5 doubleItems)

; This actually had to be fished out of module_rpg<?>.rb:
;  1-6 are the opponent choices,
;  7-11 are the ally choices.
;  9, 10 work on dead friends. 11 works on the user.
;  1,3,7,9,11 work on one target.
;  3,4,5,6 work on random targets.
;  2, 8, 10 work on all in a given class.
;  1, 7, 9 require selection.
;  In the case of random targets, the amount is dictated by the ID - 2.
;  These rules lead to the enum given.
(e battle_usableitem_scope \1 E-Single \2 E-All \3 E-1-Random \4 E-2-Random \5 E-3-Random \6 E-4-Random \7 A-Single \8 A-All \9 A-Single-I.Dead \10 A-All-I.Dead \11 Self)
(e battle_usableitem_hit_type \0 certain \1 physical \2 magical)

; More rules stuff
; 1 3 5 act on HP
; 2 4 6 act on MP
; 3 4 perform recover
; 5 6 perform drain

(e battle_usableitem_damagetype \1 hp \2 mp \3 hpRecover \4 mpRecover \5 hpDrain \6 mpDrain)

; etcetc some rules here

(e battle_usableitem_occasion \0 all \1 battle \2 menu)

(: RPG::UsableItem::Damage)
(@ type battle_usableitem_damagetype)
(@ formula string= \10)
(@ critical boolean)
(@ variance int)
(@ element_id element_id)

(C defaultCB)
(@ data_id int)

(obj 11 "elementRate")
(@ data_id element_id)
(obj 12 "debuffRate")
(@ data_id parameter_id)
(obj 13 "stateRate")
(@ data_id state_id)
(obj 14 "stateResist")
(@ data_id state_id)
(obj 21 "param")
(@ data_id parameter_id)
(obj 22 "xparam")
(@ data_id exparameter_id)
(obj 23 "sparam")
(@ data_id spparameter_id)
(obj 31 "atkElement")
(@ data_id element_id)
(obj 32 "atkState")
(@ data_id state_id)
(obj 33 "atkSpeed")
(@ data_id int)
(obj 34 "atkTimes")
(@ data_id int)
(obj 41 "stypeAdd")
(@ data_id stype_id)
(obj 42 "stypeSeal")
(@ data_id stype_id)
(obj 43 "skillAdd")
(@ data_id skill_id)
(obj 44 "skillSeal")
(@ data_id skill_id)
(obj 51 "equipWtype")
(@ data_id wtype_id)
(obj 52 "equipAtype")
(@ data_id atype_id)
(obj 53 "equipFix")
(@ data_id etype_id)
(obj 54 "equipSeal")
(e battle_slottype_dataids \0 doNothing \1 allowDualWield \2 inhibitDualWield)
(@ data_id etype_id)
(obj 55 "slotType")
(@ data_id battle_slottype_dataids)
(obj 61 "actionPlus")
(@ data_id int)
(obj 62 "specialFlag")
(@ data_id battle_specialflag_id)
(obj 63 "collapseType")
(@ data_id battle_collapsetype)
(obj 64 "partyAbility")
(@ data_id battle_partyability_id)

(: RPG::BaseItem::Feature)
(@ code battle_feature_code)
(+ hwnd @code RVXA/BatFeatures)
(@ value typeChanger{ int i float f })
(+ flushCommandBuffer @code battle_feature_code)

; The actual meanings need to be checked.
; Code 21 is definitely "apply state @data_id with chance @value1."
; However, data_id can be 0 - in which case it's either a NOP or an "apply damage". Not sure which yet.

(C defaultCB)
(@ data_id int)
(obj 11 "recoverHP")
(@ data_id int)
(obj 12 "recoverMP")
(@ data_id int)
(obj 13 "gainTP")
(@ data_id int)
(obj 21 "addState|addAtkStates")
(@ data_id state_id)
(obj 22 "removeState")
(@ data_id state_id)
(obj 31 "addBuff")
(@ data_id parameter_id)
(obj 32 "addDebuff")
(@ data_id parameter_id)
(obj 33 "removeBuffs")
(@ data_id parameter_id)
(obj 34 "removeDebuffs")
(@ data_id parameter_id)
(obj 41 "specialEffect")
(@ data_id battle_specialeffect_id)
(obj 42 "increaseParam")
(@ data_id parameter_id)
(obj 43 "learnSkill")
(@ data_id skill_id)
(obj 44 "runCommonEvent")
(@ data_id commonevent_id)

(: RPG::UsableItem::Effect)
(@ code battle_effect_code)
(+ hwnd @code RVXA/BatEffects)
(@ value1 float)
(@ value2 float)
(+ flushCommandBuffer @code battle_effect_code)

(. battle_parameter_array)

; If you saw this pattern elsewhere and thought to 'correct' it, here's why not to do that:
; The array at the start is to ensure the type is correct,
;  so when creating an object things don't explode.
; It's also a good raw access hatch in case it turns out an array has *more than 8* elements despite spec.
; Otherwise you wouldn't know!

(+ subwindow array \8 int)
(] \0 maxHP int)
(] \1 maxMP int)
(] \2 attack int)
(] \3 defense int)
(] \4 maxAtk int)
(] \5 maxDef int)
(] \6 agility int)
(] \7 luck int)
