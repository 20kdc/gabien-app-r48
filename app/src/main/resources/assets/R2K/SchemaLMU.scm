
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(. EventPage_Condition_Flags)
(+ __bitfield__)
(@ switch_a boolean)
(@ switch_b boolean)
(@ var_>=_or_2k3op boolean)
(@ item boolean)
(@ actor boolean)
(@ timer_1 boolean)
(@ timer_2_2k3 boolean)

(: RPG::EventPage::Condition)
(C datum \
\(define-name\ RPG::EventPage::Condition.switchA\ \(if-eq\ @valid@switch_a\ #t\ \(\
\	\",\ Switch\ \"\ \(@\ @switch_a\ switch_id\ #t\)\
\)\)\)\
\(define-name\ RPG::EventPage::Condition.switchB\ \(if-eq\ @valid@switch_b\ #t\ \(\
\	\",\ Switch\ \"\ \(@\ @switch_b\ switch_id\ #t\)\
\)\)\)\
\(define-name\ RPG::EventPage::Condition.var\ \(if-eq\ @valid@var_>=_or_2k3op\ #t\ \(\
\	\",\ Var\ \"\ \(@\ @var_id\ var_id\ #t\)\
\	\(?\ @var_compare_op_2k3\ \(\"\ \"\ \(@\ @var_compare_op_2k3\ var_compare_op_2k3\)\)\ \">=\"\)\
\	\"\ \"\ \(@\ @var_value\)\
\)\)\)\
\(define-name\ RPG::EventPage::Condition.item\ \(if-eq\ @valid@item\ #t\ \(\
\	\",\ Party\ Has\ Item\ \"\ \(@\ @item_id\ item_id\ #t\)\
\)\)\)\
\(define-name\ RPG::EventPage::Condition.actor\ \(if-eq\ @valid@actor\ #t\ \(\
\	\",\ Party\ Has\ Actor\ \"\ \(@\ @actor_id\ actor_id\ #t\)\
\)\)\)\
\(define-name\ RPG::EventPage::Condition.timer1\ \(if-eq\ @valid@timer_1\ #t\ \(\
\	\",\ Timer1\ <=\ \"\ \(@\ @timer_1_secs\)\ \"s\"\
\)\)\)\
\(define-name\ RPG::EventPage::Condition.timer2\ \(if-eq\ @valid@timer_2_2k3\ #t\ \(\
\	\",\ Timer2\ <=\ \"\ \(@\ @timer_2_secs_2k3\)\ \"s\"\
\)\)\)\
\(define-name\ Class.RPG::EventPage::Condition\
\	\(@\ :\ RPG::EventPage::Condition.switchA\)\ \(@\ :\ RPG::EventPage::Condition.switchB\)\
\	\(@\ :\ RPG::EventPage::Condition.var\)\ \(@\ :\ RPG::EventPage::Condition.item\)\ \(@\ :\ RPG::EventPage::Condition.actor\)\
\	\(@\ :\ RPG::EventPage::Condition.timer1\)\ \(@\ :\ RPG::EventPage::Condition.timer2\)\
\)\
)
(+ hide path @valid EventPage_Condition_Flags)
(+ path @valid@switch_a boolean)
(+ condHide @valid@switch_a path @switch_a switch_id)
(+ path @valid@switch_b boolean)
(+ condHide @valid@switch_b path @switch_b switch_id)
(+ path @valid@var_>=_or_2k3op boolean)
(+ condHide @valid@var_>=_or_2k3op path @var_id var_id)
(e var_compare_op_2k3 \0 == \1 >= \2 <= \3 > \4 < \5 !=)
(+ condHide @valid@var_>=_or_2k3op optP @var_compare_op_2k3 var_compare_op_2k3)
(+ condHide @valid@var_>=_or_2k3op path @var_value int)
(+ path @valid@item boolean)
(+ condHide @valid@item path @item_id item_id)
(+ path @valid@actor boolean)
(+ condHide @valid@actor path @actor_id actor_id)
(+ path @valid@timer_1 boolean)
(+ condHide @valid@timer_1 path @timer_1_secs int)
(+ path @valid@timer_2_2k3 boolean)
(+ condHide @valid@timer_2_2k3 optP @timer_2_secs_2k3 int)

(: RPG::MoveRoute)
(@ list MoveListEditor)
(@ repeat booleanDefTrue)
(@ skippable boolean)

(. eventpage_graphics)
(@ character_name f_charset_name)
(@ character_index int)
(+ halfsplit eventTileHelper @character_index @character_name \1 R2K/TS144 spriteSelector @character_index @character_name CharSet/)
(@ character_direction sprite_direction)
(@ character_pattern int= \1)
(@ character_blend_mode boolean)
(+ internal_EPGD)

(: RPG::EventPage)
(C datum \(define-name\ Class.RPG::EventPage\ \(@\ @condition\)\))
(@ condition subwindow RPG::EventPage::Condition)
(+ subwindow: Graphics eventpage_graphics)
; Default is set like this both for convenience and so that a SavePartyLocation is correct
(e eventpage_layer \1 withPlayer+collide \0 belowPlayer \2 abovePlayer)
(@ layer eventpage_layer)
(e eventpage_movetype \0 don\'t \1 random \2 guardVLine \3 guardHLine \4 chasePlayer \5 fleePlayer \6 custom)
(@ move_type eventpage_movetype)
(@ move_freq eventpage_movefreq)
(@ move_speed eventpage_movespeed)
(@ move_route subwindow RPG::MoveRoute)
(@ block_other_events boolean)
(e eventpage_animtype \0 onlyWhenMoving \1 always \2 noActDirchange+onlyWhenMoving \3 noActDirchange+always \4 noActDirchange+never \5 spin \6 noWalkAnimButStillRotate)
(@ anim_type eventpage_animtype)
(e eventpage_trigger \0 playerInteract \1 playerCollide \2 playerOrEventCollide \3 autorun \4 parallel)
(@ trigger eventpage_trigger)
(@ list EventListEditor)

(: RPG::Event)
(C datum \(define-name\ Class.RPG::Event\ \(@\ @name\)\ \"\ \(\"\ \(@\ @x\)\ \",\ \"\ \(@\ @y\)\ \"\)\"\))
(+ windowTitleAttachment \(@\ @name\))
(@ name string)
(@ x int)
(@ y int)
(@ pages arrayPAX1 RPG::EventPage)

(: RPG::Map)
(@ tileset_id tileset_id)
(@ width roint= \20)
(@ height roint= \15)
(e map_scroll_type \0 none \1 vertical \2 horizontal \3 horizontal+vertical)
(@ scroll_type map_scroll_type)
(@ parallax_flag boolean)
(@ parallax_name f_parallax_name)
(@ parallax_loop_x boolean)
(@ parallax_loop_y boolean)
(@ parallax_autoloop_x boolean)
(@ parallax_autoloop_y boolean)
(@ parallax_sx int)
(@ parallax_sy int)
(@ events subwindow hash int+1 subwindow RPG::Event)
(@ top_level boolean)
(+ optP @save_count_2k3en int)
(+ optP @save_count_other int)
(+ tableD @data @width @height \3 \20 \15 \2 \0 \10000)
