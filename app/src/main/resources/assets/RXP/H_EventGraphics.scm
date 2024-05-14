
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(obj 0 "Something")
(. If @tile_id is not \0, then the event is displayed as a tile. Otherwise, @character_name refers to the spritesheet.)
(. Importantly, tile events, when collidable \(@through is false\) use the collision rules of the chosen tile, as if the event occupied a layer above all existing tile layers.)
