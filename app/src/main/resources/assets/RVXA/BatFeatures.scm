
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; VX Ace Battle Features Helpfile


;   /\^.^/\
;  /__/ \__\

; BatFeatures

; (yes I am awful at ASCII art)

; "features_pi" multiplies all features with the same code and data_id together.
;  So in other words, for a given feature code, the data_id has some feature-specific meaning,
;   and the value is the multiplier.
; "features_set" has no @value meaning - all data_id given are part of a set

(obj 0 "unknown")
(.)
(. You should probably set this to something else.)
(. @value: No meaning.)
(. @data_id: No meaning.)
(.)

(obj 11 "elementRate")
(.)
(. All elementRate features are multiplied together)
(. @value: Decimal multiplier.)
(. @data_id: Element ID.)
(.)

(obj 12 "debuffRate")
(.)
(. All debuffRate features are multiplied together)
(. @value: Decimal multiplier.)
(. @data_id: Parameter ID)
(.)

(obj 13 "stateRate")
(.)
(. All stateRate features are multiplied together)
(. @value: Decimal multiplier.)
(. @data_id: State ID)
(.)

(obj 14 "stateResist")
(.)
(. Resist a given state, I would assume)
(. @value: No meaning.)
(. @data_id: State ID)
(.)

(obj 21 "param")
(.)
(. Modifies the rate at which parameters change, though this could just actually be a total multiplier.)
(. All param features are multiplied together)
(. @value: Decimal multiplier.)
(. @data_id: Parameter ID)
(.)

(obj 22 "xParam")
(.)
(. Directly provides a boost to an \'xParam\')
(. All xparam features are added together)
(. @value: Decimal multiplier.)
(. @data_id: Ex-Parameter ID)
(.)

(obj 23 "sParam")
(.)
(. All active features for a given sparam are multiplied together, with a starting value of \1.0 \- the result is the sParam value.)
(. @value: Decimal multiplier.)
(. @data_id: Sp-Parameter ID)
(.)

(obj 31 "atkElement")
(.)
(. Adds an element to the set of elements \(presumably additional\) that attacks act on.)
(. @value: No meaning.)
(. @data_id: Element ID.)
(.)

(obj 32 "atkState")
(.)
(. Attacks while this feature is active may cause a given state.)
(. The values are added together if >1 exists for a state.)
(. @value: Decimal probability.)
(. @data_id: State ID.)
(.)

(obj 33 "atkSpeed")
(.)
(. Adds to attack speed.)
(. Unsure if a multiplier is applied after this or not.)
(. @value: Value to add/multiply on to attack speed.)
(. @data_id: No meaning.)
(.)

(obj 34 "atkTimes")
(.)
(. Adds to the amount of times an attack is performed.)
(. @value: Amount of additional attacks to perform.)
(. Final value will not fall under \0 \- that is, at least one attack will be performed \- but features can negate each other.)
(. @data_id: No meaning.)
(.)

(obj 41 "stypeAdd")
(.)
(. Gives access to a skill type.)
(. @value: No meaning.)
(. @data_id: Stype ID. \(See stype_id on skills, or skill_types in System.\))
(.)

(obj 42 "stypeSeal")
(.)
(. Disables a skill type.)
(. @value: No meaning.)
(. @data_id: Stype ID. \(See stype_id on skills, or skill_types in System.\))
(.)

(obj 43 "skillAdd")
(.)
(. Gives access to a skill.)
(. @value: No meaning.)
(. @data_id: Skill ID.)
(.)

(obj 44 "skillSeal")
(.)
(. Disables a skill.)
(. @value: No meaning.)
(. @data_id: Skill ID.)
(.)

(obj 51 "equipWtype")
(.)
(. Gives access to a weapon type.)
(. @value: No meaning.)
(. @data_id: Wtype ID. \(See wtype_id on weapons, or weapon_types in System.\))
(.)

(obj 52 "equipAtype")
(.)
(. Gives access to an armour type.)
(. @value: No meaning.)
(. @data_id: Atype ID. \(See atype_id on armours, or armor_types in System.\))
(.)

(obj 53 "equipFix")
(.)
(. \'Fixes\' \(prevents changing of?\) equipment with a given Etype.)
(. @value: No meaning.)
(. @data_id: Etype ID. \(See etype_id on \'equipment\': weapons/armours, or Terms/@etypes in System.\))
(.)

(obj 54 "equipSeal")
(.)
(. Prevents use of equipment with a given Etype.)
(. @value: No meaning.)
(. @data_id: Etype ID. \(See etype_id on \'equipment\': weapons/armours, or Terms/@etypes in System.\))
(.)

(obj 55 "slotType")
(.)
(. Sets the slot types available.)
(. \(If multiple of these exist, the highest is used.\))
(. @value: No meaning.)
(. @data_id: If \0, does nothing. If \1, allows dual wield. If \2, inhibits dual wield\(?\))
(.)

(obj 61 "actionPlus")
(.)
(. Gives an additional bit of action time.)
(. @value: As specified \(unsure if true\), the probability of additional action time.)
(. @data_id: No meaning.)
(.)

(obj 62 "specialFlag")
(.)
(. Sets a \'special flag\'.)
(. @value: No meaning.)
(. @data_id: Special Flag ID \(See Ruby code\))
(.)

(obj 63 "collapseType")
(.)
(. What kind of collapse should the actor suffer?)
(. \(Unsure why this is handled via .max!\))
(. @value: No meaning.)
(. @data_id: Collapse type. \(See Ruby code\))
(.)

(obj 64 "partyAbility")
(.)
(. Does the party have this ability?)
(. @value: No meaning.)
(. @data_id: Ability ID. \(See Ruby code\))
(.)
