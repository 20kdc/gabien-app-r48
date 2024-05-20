
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Common commands between RXP/RVXA.

(C categories "1 - Text" "2 - Flow" "3 - Party" "4 - Map" "5 - Battle" "6 - Misc")

(cmd 0 ">> insert point")
(d "This is a placeholder. The idea is that you replace this command with the one you actually want to have. R48 doesn't really need these.")
(L)

; Comments

(cmd 108 "//*" ($ " " ]0))
(d "The beginning of a comment.")
(p note string)
(C groupBehavior messagebox 408)

(cmd 408 "//" ($ " " ]0))
(d "A comment continues.")
(p note string)

; Choices and input

(cmd 102 "Show Choices: " (? ]0 ((@ ]0 choice_array) " " (if-ne ]1 0 "(can cancel)")) "(c.402/403/404)"))
(C category 0)
(d "Show a set of choices to the user, potentially allowing the cancel button.")
(C tag translatable)
(p choices string_array)
(p cancellable int_boolean)
(C groupBehavior form 402 Choice 403 Cancelled 404)
(I 1)

; See IRB_CMDB.txt for indentation logic docs, and the 'K command'.

(cmd 402 "When Choice =" ($ " " ]0))
(C category 0)
(d "When a given choice index is chosen...")
(p choice int)
(p textRef string)
(C groupBehavior expectHead 102 402 403)
(i \-1)
(I 1)
(K 102)

(cmd 403 "When cancelled (idx 4)")
(C category 0)
(d "When choice index 4 (cancel) is chosen...")
(C groupBehavior expectHead 102 402 403)
(i \-1)
(I 1)
(K 102)

(cmd 404 "End Choice")
(C category 0)
(d "The end of a choice block.")
(C groupBehavior expectHead 102 402 403)
(i \-1)
(K 102)

(cmd 103 "Input Number" ($ " to " ]0 var_id #t))
(C category 0)
(d "Asks the player to input a number, and store the result in a variable.")
(p varId var_id)
(p digitCount int)

; SV/SS

(cmd 121 "Control Switch" (vv= ]0 ]1 "" "es") (? ]0 (" " (@ ]0 switch_id #t) (vv= ]0 ]1 "" (".." (@ ]1 switch_id #t))) " = " (@ ]2 int_boolean_switch_not))))
(C category 1)
(d "Sets a switch or a range of switches on or off.")
(p firstSwitch switch_id)
(p lastSwitch switch_id)
(p turnOff int_boolean)

(cmd 122 "Set Variable" (vv= ]0 ]1 "" "s") (? ]0 (" " (@ ]0 var_id #t) (vv= ]0 ]1 "" (".." (@ ]1 var_id #t))) " " (@ ]2 set_variables_operation) " " (@ : set_variables_parameters))))
(C category 1)
; note: the parameters given here are for formatting reasons, since the X overrides any actual use.
(d "Modifies a variable or a range of variables (first/lastVar) in a given way (operation) by a given value (source)")
(p firstVar var_id)
(p lastVar var_id)
(p op set_variables_operation)
(p src set_variables_source)
(X set_variables_parameters)

(cmd 123 "Control Selfswitch" (? ]0 (" " (@ ]0 selfswitch_id) " = " (@ ]1 int_boolean_switch_not))))
(C category 1)
(d "Enables or disables a selfswitch.")
(p selfSwitch selfswitch_id)
(p turnOff int_boolean)

; Conditional Branch

(cmd 111 "Conditional" (? ]0 (@ : conditional_branch_parameters) " Branch (c.411/412)"))
(C category 1)
(d "A conditional branch. Made up of this, optionally a 411 'Else', and a 412 'End Conditional'. Skips some code if a given condition is false.")
; The parameter exists so the A: detect works
(p type conditional_branch_types)
(C groupBehavior form 411 Else 412)
(X conditional_branch_parameters)
(I 1)

(cmd 411 "Else")
(C category 1)
(d "Splits a conditional - begins the code run if and only if the condition is false.")
(C groupBehavior expectHead 111)
(i \-1)
(I 1)
(l)

(cmd 412 "End Conditional")
(C category 1)
(d "Ends a conditional branch block.")
(C groupBehavior expectHead 111 411)
(i \-1)
(l)

; Loops

(cmd 112 "Start Loop (c.113/413)")
(C category 1)
(d "Begins a loop.")
(C groupBehavior form 413)
; this is a blank 'placeholder' command
(I 1)

(cmd 113 "Break Loop")
(C category 1)
(d "Leave a loop formed between a Start Loop and End Loop.")

(cmd 413 "End Loop/Repeat Above")
(C category 1)
(d "The end of a loop (goes back to the start)")
(C groupBehavior expectHead 112)
(i \-1)
(l)

; Misc

(cmd 115 "Exit Event Processing")
(C category 1)
(d "Stops running the code in this page.")

(cmd 117 "Call Common Event" ($ " " ]0 commonevent_id))
(C category 1)
(d "A common event is a code list loaded from the CommonEvents file. Start one. (Execution will return here when done.)")
(p eventId commonevent_id)

(cmd 118 "Label" ($ " " ]0))
(C category 1)
(d "A label. Can be jumped to.")
(p labelName string)

(cmd 119 "Jump to label" ($ " " ]0))
(C category 1)
(d "Jump to a label. May act odd if it enters/leaves conditional blocks or such.")
(p labelName string)

; I think the timer counts up?
; In any case if running is false, seconds is irrelevant.
(cmd 124 (? ]0 (if-eq ]0 1 "Stop Timer" ("Start Timer at " (@ ]1) " seconds")) "Start/Stop Timer"))
(C category 5)
(d "Enables or disables the timer. If enabling, sets the time to a given value in seconds.")
(X onoff_timer_parameters)

; Actor Stuff

(cmd 129 (? ]0 (@ : add_remove_actor_parameters) "Add/remove actor to party"))
(C category 2)
(d "Removes or adds an actor to/from the party.")
(p actor actor_id)
(p add int_boolean)
(p addInitialize int_boolean)

(cmd 303 "Input Name For Actor" ($ " " ]0 actor_id #t))
(d "Let the user give a party member a name. (Warning: There are no filters.)")
(p actor actor_id)
(p maxLetters int)

; Battle Branches

(cmd 601 "Battle Result Branches Start: If Win")
(d "This is the beginning of the optional result branches section for a Start Battle (301), should be immediately below it, and contains the win code.")
(C category 4)
(C groupBehavior expectHead 301)
(C groupBehavior expectTail 602 603 604)
(C template 602 603 604)
(I 1)

(cmd 602 "Battle Result Branch: If Escape")
(d "This contains the escape code. See 'canEscape' in the relevant Start Battle.")
(C category 4)
(C groupBehavior expectHead 301 601)
(C groupBehavior expectTail 603 604)
(i \-1)
(I 1)

(cmd 603 "Battle Result Branch: If Lose")
(d "This contains the lose code. See 'losingDoesNotGameover' in the relevant Start Battle.")
(C category 4)
(C groupBehavior expectHead 301 601 602)
(C groupBehavior expectTail 604)
(i \-1)
(I 1)

(cmd 604 "Battle Result Branches End")
(d "Ends the Battle Result Branches.")
(C category 4)
(C groupBehavior expectHead 301 601 602 603)
(i \-1)

; Battle Enemy Stuff

(cmd 333 (? ]1 (if-eq ]1 0 "Add" "Remove") "Change") " Enemy" ($ " " ]0 iterate_enemy #t) " State" ($ " " ]2 state_id #t))
(d "Heal or harm an enemy by giving them a state.")
(C category 4)
(p enemy iterate_enemy)
(p remove int_boolean)
(p state state_id)

(cmd 334 "Enemy" ($ " " ]0 iterate_enemy) " recovers all")
(d "Enemy recovers all health, MP, etc. (Note: Players may hate you if you use this.)")
(C category 4)
(p enemy iterate_enemy)

; Screen Changes And Ruby

(cmd 340 "Abort Battle")
(C category 4)
(d "Simply refuse to let the battle continue.")

(cmd 351 "'Call Menu Screen' (aborts battle?)")
(C category 5)
(cmd 352 "'Call Save Screen' (aborts battle?)")
(C category 5)

(cmd 353 "Game Over")
(C category 5)

(cmd 354 "Return to Title")
(C category 5)

(cmd 355 "Ruby>" ($ " " ]0))
(C category 5)
(d "Run some Ruby.")
(p code string)
(C groupBehavior messagebox 655)

(cmd 655 "Ruby." ($ " " ]0))
(C category 5)
(d "Continuation of embedded Ruby.")
(p code string)

; Enable/Disable

(cmd 134 "Enable/Disable Saving:" ($ " " ]0))
(C category 5)
(d "Enables or disables saving the game.")
(p enabled int_boolean)

(cmd 135 "Enable/Disable Menu:" ($ " " ]0))
(C category 5)
(d "Enables or disables accessing the menu.")
(p enabled int_boolean)

(cmd 136 "Enable/Disable Encounters:" ($ " " ]0))
(C category 5)
(d "Enables or disables random encounters.")
(p enabled int_boolean)
