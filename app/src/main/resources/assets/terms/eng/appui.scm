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
		completeReportFind (fl2 "Found " a0 " instances across " a1 " files.")
		from "From: "
		to "To: "
		addR "Add replacement"
		confirmReplace "Confirm & Replace"
		find "Find"
		text "Text: "
		addS "Add search"
		caseInsensitive "Case Insensitive"
		detailedInfo "Detailed Info (SLOW, CAN CRASH FOR MANY MATCHES)"
		modeAll "All"
		modeCTag (fl1 "Command Tag: " a0)
	)
	(tsc_
		dumpOk "transcript.html was written to the target's folder."
		ctx "Context (variables, etc.)"
		map (fl1 "Map:" a0)
		ev (fl3 "Ev." a0 " " a2 ", page " a1)
	)
	shcEmpty "There is nothing in the clipboard."
	shcIncompatible "Incompatible clipboard and target."
	shInspect "Inspect..."
	shLIDC "Local ID Changer..."
	shNoCloneTmp "Cannot clone, this contains a temporary dialog."
	openAud "Open sound file..."
	(bts_
		ramObj "Edit RAM Object"
	)
	mGetGPUInfo "GPU Information (New API Compatibility Test)"
	disableMapRendering "Disable rendering (even across maps!)"
	disableMapAnimation "Disable animation"
	mIDChanger "ID Changer..."
	(idc_
		unavailable "No ID Changer options available."
		typeButton (fl1 "Type: " a0)
		fromButton (fl1 "From: " a0)
		toButton (fl1 "To: " a0)
		beware "This tool replaces all references of 'from' with 'to'.\nIf done wrong, this might as well be a button labelled 'Destroy My Project'.\nIt is wise to back up your project before using this."
		fridge (fl1
			(cond
				((= a0 0) "No occurances found.")
				((= a0 1) "1 occurance changed.")
				(else (.. a0 " occurances changed."))
			)
		)
		swapMode "Swap Mode (swap From and To rather than simply replace From to To)"
		localTo (fl1 "Local to: " a0 "\nReferences outside this will not be changed.")
	)
	prObjectName "Object Name?"
	prSchemaID "Schema ID?"
	prObjectSrc "Source Object Name?"
	prObjectDst "Target Object Name?"
	mTestFonts "Test Fonts"
	mTestGraphics "Test Graphics Stuff"
	mToggleFull "Toggle Fullscreen"
	mSchemaTranslator "Dump Schemaside Translations"
	mTryRecover "Recover data from R48 error <INCREDIBLY DAMAGING>..."
	mAudPlay "Audio Player..."
	mREPL "REPL..."
	mLocateEventCommand "Locate EventCommand in all Pages"
	mSearchCmdsCEV "Search Commands In Common Events..."
	mRMTools "RM-Tools"
	mRunAutoCorrect "See If Autocorrect Modifies Anything"
	mR48Version "R48 Version"
	warnRestoreSafety (..
		"If the backup file is invalid, wasn't created, or is otherwise harmed, this can destroy more data than it saves."
		"Check *everything* before a final save."
		; "I understand." is NOT translated
		"Type 'I understand.' at the prompt behind this window if you HAVE done this."
	)
	dlgBadNum "Not a valid number."
	dlgFontSize "Font Size?"
	dlgClipEmpty "There is nothing in the clipboard."
	mEditObj "Edit Object"
	mCorrectObj "Autocorrect Object By Name And Schema"
	mInspectObj "Inspect Object (no Schema needed)"
	mDiffObj "Object-Object Comparison"
	mAllStr "Retrieve all object strings"
	mLoadIMI "PRINT.txt Into Object"
	mUniversalStringFinder "Universal String Finder"
	mUniversalStringReplacer "Universal String Replacer"
	mTranscriptDump "MEV/CEV Transcript Dump (no Troop/Item/etc.)"

	dClipSaved "The clipboard was saved."
	dClipBad "The clipboard file is invalid or does not exist."
	dClipLoaded "The clipboard file was loaded."

	mHelp "Help"
	mConfiguration "Configuration"
	mImgEdit "Image Editor"
	mObjA "Object Access"
	dFileUnreadable "A file couldn't be read, and R48 cannot create it."
	dObjCompare "objcompareAB.txt and objcompareBA.txt have been made."
	osLocmapsWarn "Wrote locmaps.txt (NOTE: You probably don't actually want to do this! Press this in RXP mode to get the CRCs, then go back to this mode to actually start editing stuff.)"
	osLocmapsGen "Wrote locmaps.txt"
	dFileUnreadableNoSchema "The target file couldn't be read, and there's no schema to create it."
	dCannotReadPRINT "The PRINT.txt file couldn't be read."
	mOther "Other..."
	statusLine (fl2 a0 " modified. Clipboard: " a1)
	mClipboard "Clipboard"
	mClipSave "Save Clipboard To 'clip.r48'"
	mClipLoad "Load Clipboard From 'clip.r48'"
	mClipInspect "Inspect Clipboard"
	dReturnMenuWarn "Are you sure you want to return to menu? This will lose unsaved data."
	mTranslatablesIn (fl1 "Translatables in: " a0)
	cCommonEventsNoSchema "Somehow, the common events file does not have a schema."
	cCommonEventsNoSchema2 "Unable to determine common event schema, looks like that refactor will have to happen now"

	rmCmdCodeRequest "Code (or -1337 for any unknown) ?"
	notFound "Not found."
	confirmDeletion (fl1 "Are you sure you want to delete " a0 "?")

	ccAll "All Commands"
	dumpWhateverICanThinkOfToJSON "Command & Enum Information -> cmdb.json"
)