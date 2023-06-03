; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Global UI stuff

(define-group TrAppUI
	init "Initializing UI..."
	init2 ""
	initTab (fl1 "Initializing tab: " a0)
	initMapScan "Looking for maps and saves (this'll take a while)..."
	done "Done!"
	(confirm_
		accept "Accept"
		cancel "Cancel"
	)
	contextError (fl2 a0 "\n" a1)
	helpTitle "Help Window"
	helpIndex "Index"
	newDirs "This appears to be newly created. Click to create directories."
	revertWarn "Reverting changes will lose all unsaved work and will reset many windows."
	notRelease (..
		"Not an actual release - you have likely compiled this yourself.\n"
		"The file 'assets/version.txt' needs to exist for text to appear here."
	)
	(odb_
		disposed " [disposed]"
		created " [created]"
		modified " [modified]"
		lost " [modifications lost, should never occur!]"
		listeners (fl1 " [" a0 "ML]")
		saveErr (fl1 "Error saving object: " a0)
		loadObj (fl1 "Loading object...\n" a0)
	)
	(test_
		binding "Binding"
		prOk "PRINT.txt written!"
		prFail "Could not print."
		PTS "PTS"
		PTF "PTF"
		back "Back..."
		toREPL "To REPL"
		toREPLOk "Accessible from REPL as $obj"
		withSchema "With Schema"
	)
	(np_
		synthOk "The synthesis was completed successfully."
		synth2kQ "Would you like a basic template, and if so, compatible with RPG Maker 2000 or 2003? All assets used for this are part of R48, and thus public-domain."
		r2k0 "2000 Template"
		r2k3 "2003 Template"
		nothing "Do Nothing"
	)
	(spr_
		num "Sprite Num."
		msgNoImage "The image wasn't specified."
	)
	soundFail "Unable to load sound."
	(cg_
		savePNG "Save PNG..."
		copyR48 "Copy to R48 Clipboard"
	)
	frameDisplay (fl2 (+ a0 1) " / " a1)
	(set_
		selAll "Select All"
		deSelAll "Deselect All"
	)
	lAlphaChannel "Alpha"
	lHSVRecommend "(NOTE: HSV may be better if precision isn't an issue.)"
	bEnumRename " Name"
	bEnumManual "Manual."
	enumOptions "Options"
	(usl_
		full "Full"
		partial "Partial"
		completeReport (fl2 "Made " a0 " total string adjustments across " a1 " files.")
		from "From: "
		to "To: "
		addR "Add replacement"
		confirmReplace "Confirm & Replace"
	)
	(tsc_
		dumpOk "transcript.html was written to the target's folder."
		cev "Common Events"
		ctx "Context (variables, etc.)"
		map (fl1 "Map:" a0)
		ev (fl3 "Ev." a0 " " a2 ", page " a1)
	)
	shcEmpty "There is nothing in the clipboard."
	shcIncompatible "Incompatible clipboard and target."
	shNoCloneTmp "Cannot clone, this contains a temporary dialog."
	openAud "Open sound file..."
	(bts_
		ramObj "Edit RAM Object"
	)
	mGetGPUInfo "GPU Information (New API Compatibility Test)"
	; Image editor import
	(ie_
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
		undoNone "There is nothing to undo."
		redoNone "There is nothing to redo."
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
)