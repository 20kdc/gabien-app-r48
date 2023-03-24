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
)