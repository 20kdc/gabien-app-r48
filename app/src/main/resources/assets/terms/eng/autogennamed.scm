; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

(define-group TrAutoGen
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
	mFindTranslatables "Find Translatables in Common Events"
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
)
