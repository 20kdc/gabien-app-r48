
; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Character generator layers.
; ':' specifies the beginning of a layer description; '+' makes it enabled by default; 'x' makes it enabled by default and invisible.
;  The name and default colour multiplier is specified, followed by group IDs.
;  Only one layer in any given group ID may be active at any given time.
; '.' specifies text for a given language.
;  CharGen is being treated as 'likely replaced by external assets'.
;  The language, then name, is specified.
;  If no language is specified, then this is applied to all languages.
;  You should thus use the structure:
;   : CgCat
;   . "Cat"
;   . Lojban "mlatu"
; 'm' specifies the current mode.
; 'i' specifies an image in the current mode.
; Example:
;    Src
;    Img Z
;  i hi  0

; Note that images are implicitly prefixed with "CharGen/" + layer ID, and postfixed with ".png"
; Drawing is performed in upwards Z order ; highest Z is at the front.

(x Base FFFFFFFF)
(. Base)
(m CS)
(i CS \0)

(+ \0 FFFF0000 Number)
(. \'0\'\ Indicator)
(m CS)
(i CS \1)

(: \1 FFFF0000 Number)
(. \'1\'\ Indicator)
(m CS)
(i CS \1)
