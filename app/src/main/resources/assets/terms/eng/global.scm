; gabien-app-r48 - Editing program for various formats
; Written starting in 2016 by contributors (see CREDITS.txt)
; To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
; A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

; Global terms

(define-group TrGlobal
	wordLoad "Load"
	wordSave "Save"
	wordInvalidFileName "Invalid or missing file name."

	bOk "Ok"
	bCancel "Cancel"
	bConfirm "Confirm"
	bCopy "Copy"
	bPaste "Paste"
	bUndo "Undo"

	bContinue "Continue"
	bConfigV "Configure"
	bSelectEngine "Select Engine"
	bConfigN "Configuration"
	bQuit "Quit R48"

	lFrameMS "MS per frame:"
	lGamePath "Path To Game (if you aren't running R48 in the game folder):"
	lSecondaryPath "Secondary Image Load Location:"
	lChooseEngine "Choose Target Engine:"

	helpUnavailable "This helpfile is unavailable in your language; the English version has been displayed."

	loadingProgress (fl1 "Loading... " a0)
	loadingDCO "Initializing dictionaries & creating objects..."
	dlgNoSysDump "The system dump was unloadable. It should be: r48.error.YOUR_SAVED_DATA.r48"
	dlgReloadPFD "Power failure dump loaded."
	dlgReloadED "Error dump loaded."

	(img_
		bmpX (fl1 "Save BMP-" a0 "I")
		png32 "Save PNG-32T"
		png8 "Save PNG-8I"
		xyz "Save XYZ"
	)

	bBack "Back..."
	accessLauncherREPL "Access Launcher REPL"
	dumpLLang "Write launcher-tr.scm"

	(err_
		hasOccurred (..
			"An error has occurred in R48. This is always the result of a bug somewhere.\n"
			"Version: "
		)
		appWasStarted (..
			"If the rest of R48 disappeared, that means a second error occurred, and R48 has shut down to keep this message up.\n"
			"This is because, if backups failed, then Save would fail anyway - and without these instructions, you're kind of doomed."
		)
		backupOk (..
			"A backup data file has been created.\n"
			"QUIT AFTER READING THIS MESSAGE IN IT'S ENTIRETY.\n"
			"MAKE A COPY OF THE ENTIRE DIRECTORY AND ALL R48 SYSTEM FILES IMMEDIATELY. (anything with .r48 extension is an r48 system file. clip.r48 counts but is probably unnecessary.)\n"
			"DO NOT MODIFY OR DESTROY THIS COPY UNLESS YOUR CURRENT WORK IS COMPLETELY SAFE, VALID, NON-CORRUPT AND BACKED UP.\n"
			"PREFERABLY FORWARD THE ERROR TEXT FILE (r48.error.txt) TO YOUR DEVELOPMENT GROUP.\n"
			"I wrote that in caps since those are the most important instructions for recovering your work.\n"
			"Make a copy of r48.error.YOUR_SAVED_DATA.r48 - it contains your data at the time of the error.\n"
			"You can import the backup using 'Recover data from R48 error' - but copy the game first, as the data may be corrupt.\n"
			"You are encountering an error. Backup as much as you can, backup as often as you can."
		)
		backupFail (..
			"Unfortunately, R48 was unable to make a backup. If R48 is gone, this means that, basically, there's no way the current state could be recoverable.\n"
			"Unless you ran out of disk space, even attaching a debugger would not help you at this point, because the data is likely corrupt.\n"
			"Make a copy of the game immediately, then, if R48 is still around, try to save, but I will summarize by saying: it appears all hope is lost now.\n"
			"The reason for the failure to backup is below."
		)
		footer "Error details follow:"
	)
)
