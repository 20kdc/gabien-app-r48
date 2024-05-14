
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(obj 0 "H_MusicType")
(. If @music_type is inherit, both @music and @music_type are inherited from the parent map.)
(. If @music_type is ignored, the music is not changed \(whatever was already playing remains playing\).)
(. If @music_type is specified, the music to play is specified by the @music value.)
