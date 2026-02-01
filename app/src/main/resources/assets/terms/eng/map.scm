; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Map editing terms

(define-group TrMap
	tPen "Pen"
	tRectangle "Rectangle"
	tFill "Fill"
	tCancel "Cancel"
	tsFinishRect "Click on a tile to finish the rectangle, or:"
	tsStartRect "Click on a tile to start the rectangle, or:"
	tsFinishCopy "Click on another tile to finish copying."
	tsPickTile "Click on a tile to pick it."
	tsPickTileAT "Exact Tile (not AutoTile)"
	tRaisePen "Raise Pen"
	tDeepWater (fl1 "Deep Water, layer " a0)
	tsClickTarget "Click at the target, or close this window."
	tsPlaceEv "Click to place event"
	bReloadPanoramaTS "Reload Panorama/TS"
	bExportShot "Export shot.png"
	dlgWroteShot "Wrote 'shot.png' in R48 settings directory."
	dlgFailedToOpenFile "Failed to open file."
	tDeepWaterButton "DeepWater"
	tShowIDs "Show/Hide Tile IDs"
	tsClickToShowEv "Click on a target to show the events."
	bMove "Move"
	bDel "Del."
	bSync "Sync"
	tsEvPick (fl1 "Ev.Pick [" a0 " total]")
	bShowAtlases "Show Atlases"

	toolsLayerVis "Layer Visibility"
	toolsPickr "Grab Tile"
	toolsCopy "B.Copy"
	toolsPaste "B.Paste"
	toolsAddendum "..."
	(l_
		tile (fl1 "Tile Layer " a0)
		passability "Passability Overlay"
		evLower "Event Layers (lower)"
		evUpper "Event Layers (upper)"
		xpZ "XP Tile/Event Z-Emulation Layer"
		border "Map Border"
		evSel "Event Selection"
		panorama "Panorama"
		grid "RM-Style Grid Overlay"
		ev "Event Layers"
		vxaZ "VXA Tile/Event Z-Emulation Layer"

		rk0l "L0 (no Upper flag)"
		rk1l "L1 (no Upper flag)"
		rkbp " (Below Player)"
		rkps " (Player/Same)"
		rk0u "L0 (Upper flag)"
		rk1u "L1 (Upper flag)"
		rkap " (Above Player)"
	)
	editingArea "Editing Area..."
	areaPointA "Click to define first point (old area shown)"
	areaPointB "Click again to define second point"

	events "Events"
	addEvent "+ Add Event"

	parentOrderInconsistency "Parent/order inconsistency error."
	inconsistentMapIndent "Indent inconsistent for map: "
	inconsistentOrder "Order inconsistency: "
	resolveManually "These errors must be resolved manually to use this panel."
	bNewMap "<Insert New Map>"
	dIDInUse "That ID is already in use."
	bFindUnusedID "Find unused ID."
	dMapID "Map ID?"
	bMoveOut "Move Out "
	bEditInfo "Edit Info. "
	bDelete "Delete"
	dRootMapRequired "Root map (ID 0, type 'root) required!"
	dNoOrder "No order for map: "
	dOrderDuplicate "Order contains map twice: "
	dMissingMap "Order expects mapinfos entry: "
	dSaveColon (fl2 a0 " : " a1)
	bNew "New..."
	unavailable (fl1 a0 " (Unavailable)")
	internalErr "Internal error in R48 or with file."
	bAttemptLoadAnyway "Attempt Load Anyway"
	lowest " (Lowest)"
	tiles "Tiles"
	stCamera "Drag to pan. Camera button: Return."
	stPicker "Drag to pan. Shift-left: Pick tile. Camera button: Scroll."
	stDragToPan "Drag to pan."
	stUseTool "Tap/Drag: Use tool. Camera button: Scroll."
	bShadowRegion "Shadow/Region"
	mapMissing2k3 "<map removed from RPG_RT.lmt>"

	(r2kSavefile_
		errPlyDel "You can't do THAT! ...Who would clean up the mess?"
		errGone "That's already gone."
		plyMap0 "Can't be deleted, but was moved to @map 0 (as close as you can get to deleted)"
		errAlreadyGhost "You're trying to delete a ghost. Yes, I know the Event Picker is slightly unreliable. Poor ghost."
		evGhosted "Transformed to ghost. Re-Syncing it and setting @active to false might get rid of it."
		evRemovalOk "As the version numbers are in sync, this worked."
		cantAddEvents "You can't add events to a savefile, only summon and move existing events."
		evAppearedInCB "The event was already added somehow (but perhaps not synced). The button should now have disappeared."
		noEvMap "There's no map to get the event from!"
		errUserIsAToaster "So, you saw the ghost, got the Map's properties window via System Tools (or you left it up) to delete the event, then came back and pressed Sync? Or has the software just completely broken?!?!?"
		errGhostUnmovable "The ghost refuses to budge."
		name "Player/Vehicles/Events"
	)

	shadowLayerRegion "Region:"
	bLoadMap "Load Map"
	bSearchCmds "Search Commands..."

	(mp_
		noClip "Unable - there is no clipboard."
		notTable "Unable - the clipboard must contain a section of map data - This is not a Table."
		layersMismatch "Unable - the map data must contain the same amount of layers for transfer."
	)
)
