; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; This lists the engine init dirs and their ODB parameters, which have been purposefully kept out of MVM control.

null (
	initDir "Null/"
	odbBackend r48
)

sticki (
	initDir "Sticki/"
	odbBackend r48
	allowIndentControl #t
	defineIndent #t
	definesObjects (
		"Notes"
	)
)

ika (
	initDir "Ika/"
	odbBackend ika
	mapSystem Ika
	autoDetectPath "Pbm/Map1.pbm"
	definesObjects (
		"Map"
	)
)

osLoc (
	initDir "OSLoc/"
	odbBackend r48
	dataPath "Languages/"
	dataExt ".loc"
	autoDetectPath "Languages/en.loc"
	definesObjects (
		"en"
		"es"
		"fr"
		"ja"
		"pt_BR"
	)
)

r2k (
	initDir "R2K/"
	odbBackend lcf2000
	mapSystem R2k
	autoDetectPath "RPG_RT.ldb"
	allowIndentControl #t
	defineIndent #t
	definesObjects (
		"RPG_RT.ldb"
		"RPG_RT.lmt"
	)
)

rxp (
	initDir "RXP/"
	odbBackend r48
	dataPath "Data/"
	dataExt ".rxdata"
	mapSystem RXP
	autoDetectPath "Data/MapInfos.rxdata"
	allowIndentControl #t
	defineIndent #t
	definesObjects (
		"Actors"
		"Animations"
		"Armors"
		"Classes"
		"CommonEvents"
		"Enemies"
		"Items"
		"MapInfos"
		"Scripts"
		"Skills"
		"States"
		"System"
		"Tilesets"
		"Troops"
		"Weapons"
		"xScripts"
	)
	mkdirs (
		"Data"
		"Graphics"
		"Graphics/Animations"
		"Graphics/Autotiles"
		"Graphics/Battlebacks"
		"Graphics/Battlers"
		"Graphics/Characters"
		"Graphics/Fogs"
		"Graphics/Gameovers"
		"Graphics/Icons"
		"Graphics/Panoramas"
		"Graphics/Pictures"
		"Graphics/Tilesets"
		"Graphics/Titles"
		"Graphics/Transitions"
		"Graphics/Windowskins"
		"Audio/BGM"
		"Audio/BGS"
		"Audio/ME"
		"Audio/SE"
	)
)

rvxa (
	initDir "RVXA/"
	odbBackend r48
	dataPath "Data/"
	dataExt ".rvdata2"
	mapSystem RVXA
	autoDetectPath "Data/MapInfos.rvdata2"
	allowIndentControl #t
	defineIndent #t
	definesObjects (
		"Actors"
		"Animations"
		"Armors"
		"Classes"
		"CommonEvents"
		"Enemies"
		"Items"
		"MapInfos"
		"Scripts"
		"Skills"
		"States"
		"System"
		"Tilesets"
		"Troops"
		"Weapons"
	)
	mkdirs (
		"Data"
		"Graphics"
		"Graphics/Animations"
		"Graphics/Characters"
		"Graphics/Faces"
		"Graphics/Pictures"
		"Graphics/Parallaxes"
		"Graphics/Tilesets"
		"Audio/BGM"
		"Audio/BGS"
		"Audio/ME"
		"Audio/SE"
		; Possibly more to add?
	)
)
