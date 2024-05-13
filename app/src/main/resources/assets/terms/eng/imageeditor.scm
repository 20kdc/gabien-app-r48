; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Global UI stuff

(define-group TrImageEditor
	tdPaste "Tap top-left pixel of destination."
	swapXY "SwapXY"
	rawCopy "Raw Copy"
	badClipboard "Object in clipboard not a valid image."
	palAdj "Adjust"
	modified "Image Editor (modified)"
	noSimpleSave "While the image editor contents would be saved, there's nowhere to save them."
	autogrid "Change grid to suit this asset?"
	hint "Tap/drag: Draw, Camera button: Pan"
	hintDesktop "LMB: Draw, Shift-LMB: Grab Colour, Other: Pan"
	hintGuarded "Drag: Move around, Camera: Return to old tool"
	tdFill "Press to fill area."
	tdEye "Tap on the point to select the colour of."
	tdLineS "Press to start line."
	tdLineE "Press to end line."
	tdEyePal "Touch a point to add a new palette entry for it."
	tdFillS "Press bounding points to fill."
	tdFillE "Press another bounding point to finish."
	autoshade "Autoshade"
	autoshadeLR "LR"
	autoshadeUD "UD"
	loadFail (fl1 "Failed to load " a0 ".")
	saveFail (fl1 "Failed to save " a0 ".")
	indexed "Indexed"
	argb32 "ARGB (32-bit)"
	palWarn "Are you sure? This will create a palette entry for every colour in the image."
	npWarn "Are you sure you want to switch to 32-bit ARGB? The image will no longer contain a palette, which may make editing inconvenient, and some formats will become unavailable."
	ckSetting (fl1 "Colourkey " (if a0 "On" "Off"))
	rmWarn "The engine you're targetting expects indexed-colour images to be colour-keyed. Your image isn't colour-keyed. Continue?"
	grid "Grid..."
	gridSize "Grid Size:"
	gridOffset "Grid Offset:"
	gridColour "Grid Colour"
	charGen "CharacterGen..."
	pal "Pal. "
	palAdd "Add Palette Colour..."
	palChg "Change Palette Colour..."
	newWarn "Are you sure you want to create a new image? This will unload the previous image, destroying unsaved changes."
	newOk "New image created (same size as the previous image)"
	resize "Resize..."
	flipX "FlipX"
	flipY "Y"
	gridOverlay "Overlay"
	tdCopy "Press bounding points to copy."
	tileModeS "Click points of area to restrict drawing to."
	tileModeE "Click remaining point of area to restrict drawing to."
)
