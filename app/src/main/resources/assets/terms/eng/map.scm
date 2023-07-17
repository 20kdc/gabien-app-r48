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
	bClone "Clone"
	bDel "Del."
	bSync "Sync"
	tsEvPick (fl1 "Ev.Pick [" a0 " total]")

	l200 "Layer Visibility"
	l202 "Grab Tile"
	l203 "B.Copy"
	l204 "B.Paste"
	l205 "..."
	l206 (fl1 "Tile Layer " a0)
	l207 (fl2 "Tile L" a0 "(" (if a1 "'upper'/'wall' tileset flags" "general") ")")
	l208 "Passability Overlay"
	l209 "Event Layers (lower)"
	l210 "Event Layers (upper)"
	l211 "XP Tile/Event Z-Emulation Layer"
	l212 "Map Border"
	l213 "Event Selection"
	l214 "Panorama"
	l215 "RM-Style Grid Overlay"
	l216 "Event Layers"
	l217 "VXA Tile/Event Z-Emulation Layer"
	l218 "Map"
	l219 "Editing Area..."
	l220 "Click to define first point (old area shown)"
	l221 "Click again to define second point"
	l222 "Events"
	l223 "+ Add Event"
	l236 "Parent/order inconsistency error."
	l237 "Indent inconsistent for map: "
	l238 "Order inconsistency: "
	l239 "These errors must be resolved manually to use this panel."
	l240 "<Insert New Map>"
	l241 "That ID is already in use."
	l242 "Find unused ID."
	l243 "Map ID?"
	l244 "Move Out "
	l245 "Edit Info. "
	l246 "Delete"
	l247 "Root map (ID 0, type 'root) required!"
	l248 "No order for map: "
	l249 "Order contains map twice: "
	l250 "Order expects mapinfos entry: "
	l251 (fl2 a0 " : " a1)
	l252 "New..."
	l253 (fl1 a0 " (Unavailable)")
	l254 "Internal error in R48 or with file."
	l255 "Attempt Load Anyway"
	l256 " (Lowest)"
	l257 "Tiles"
	l258 "Drag to pan. Camera button: Return."
	l259 "Drag to pan. Shift-left: Pick tile. Camera button: Scroll."
	l260 "Drag to pan."
	l261 "Tap/Drag: Use tool. Camera button: Scroll."
	l262 "Shadow/Region"
	l263 "<map removed from RPG_RT.lmt>"

	l271 "L0 (no Upper flag)"
	l272 "L1 (no Upper flag)"
	l273 " (Below Player)"
	l274 " (Player/Same)"
	l275 "L0 (Upper flag)"
	l276 "L1 (Upper flag)"
	l277 " (Above Player)"

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
)