; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Schema terms

(define-group TrSchema
	(enum_
		id "ID."
		int "Integer"
		sym "Symbol"
		code "Code"
	)
	(cmdb_
		unkParamName "UNK."
		defCatName "Commands"
	)
	bFileBrowser (fl1 "Browse " a0 "...")
	bOpenTable "Open Table..."
	selectTileGraphic "Select Tile Graphic..."
	(ppp_
		constant "Constant"
		idVar "From Id Var. (PPP/EasyRPG/2k3 1.12)\nThe picture ID is retrieved from the variable."
		idNSfx "From Id/Name Suffix Var. Pair (PPP/EasyRPG/2k3 1.12)\nThe picture ID is retrieved from the variable.\nIf this is a show command, the last 4 characters of the image name are replaced with the value of the variable after the given one.\nThus, if the variable chosen was variable 6, with the value 1, and the variable after (variable 7) had the value 2, and the image was 'tomcat':\n1. The image would be \"to0002\"\n2. The picture ID would be 1."
		constant_h "The ID given is the picture's number."
		valVarFN "valVar "
		idVarFN "idVar "
		idNSfxFN "idNSfx "
		unknown "Unknown"
	)
	aElmInv "(This index isn't valid - did you modify a group from another window?)"
	aElmOpt (fl2 "Field " a0 " doesn't exist (default " a1 ")")
	bImport "Import"
	bExportEdit "Export/Edit"
	totemSrc "TotemSrc."
	totemCmp "Composite"
	cantEdit "Can't edit: "
	searchbar "Search:"
	bAddKey "Add Key"
	keyPfx "Key "
	parIdx "Parent Index. "
	bOptAdd "<Not present - Add>"
	manualEdit "Manual Edit:"
	booleanTrue "True"
	booleanFalse "False"
	(mph_
		disabled "Position editor disabled."
		trFail "Can't translate ID to map."
		mapFail "No such map exists."
	)
	cellTitle (fl1 "Cell " a0)
	cellAdd "<add cell here>"
	animEdit "Animation Editor"
	bGrid8px "8px Grid"
	bImportOS "Import scripts/*.rb"
	bExportOS "Export scripts/*.rb"
	(scx_
		done "Script export complete!"
		ch "Script name had to be adjusted: "
		wf "Script could not be written: "
		noIdx "It appears scripts/_scripts.txt does not exist. It acts as an index."
		miss "Script missing: "
		impFail "Wasn't able to import 'r48.edit.txt' from the R48 settings folder."
		editorFail "Unable to start the editor! Wrote to the file 'r48.edit.txt' in the R48 settings folder."
		fail "Wasn't able to export."
	)
	bSearch "Search"
	searchResults "Search Results:"
	bEditHere "Edit Here"
	dErrNoRead "Cannot read"
	dErrNoWrite "Cannot write"
	bAddToGroup "Add to group..."
	bCopyTextToClipboard "Copy text to clipboard"
	cmdOutOfList "The command isn't in the list anymore, so it has no context."
	cmdHelp " ? "
	cmdUnk "This command isn't known by the schema's CMDB."
	cmdNoDescription "This command is known, but no description exists."
	cmdUnkName "Unknown Command"
	theTrueNameOfAtIndent "@indent"
	codeAsInOpcode "Code"
	gpBeginButton "Graphically edit this..."
	svDoReset "Reset Events & Version (use after map change)"
	errInvalidMap "The map's invalid, so that's not possible."
	svDidTheReset "Reset events to map state and set versioning."
	svCauseReset "Try To Get RPG_RT To Reset The Map"
	svCausedTheReset "Ok, cleaned up. If RPG_RT loads this save, the map will probably be reset."
	(r2kinit_
		anim "Default Fallback Animation"
		death "Death"
		animSet "Default Fallback AnimSet"
		slime "Slime x1"
	)
	dOSLocUseless "This is basically useless without a locmaps.txt file. Please prepare one by going into RXP mode, System Tools, and pressing 'Retrieve all object strings', then return here."
	oslocErrNoDB "[NO DB AVAILABLE]"
	oslocErrStrUnknown "[UNKNOWN STRING. I just don't know what went wrong...]"
	toneR "R"
	toneG "G"
	toneB "B"
	toneAL "A/L"
	(array_
		dCFCompat "Incompatible clipboard and target."
		dCFNotArray "Can't copy in - copying in a range into an array requires that range be an array.\nCopying from the array interface will give you these."
		dCFEmpty "Can't copy in - the clipboard is empty."
		bCutArr "Cut Array"
		bInsert "Insert Here..."
		bAddNext "Add Next..."
		bPasteArr "Paste Array"
		bAdd "Add..."
		bModeRegular "Mode: Regular Array"
		bModePager "Mode: Pager"
	)
	(align_
		left "Align Left"
		centre "Centre"
		right "Right"
		button "Group Text..."
	)
	objectHasBecomeInvalid (fl2 "This window has become invalid because of a change in the target data.\nThis can be (for example) caused by undoing the creation of an object you are looking at.\nSchema Path: " a0 "\nSchema Element: " a1)
	seInternalError "This window has become invalid because of an internal error in R48.\nPlease report it! Details:\n"
)
