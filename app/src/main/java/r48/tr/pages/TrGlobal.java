/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr.pages;

import r48.tr.TrPage;

/**
 * Translation page for the launcher and global functionality.
 * Created 2nd March 2023.
 */
public class TrGlobal extends TrPage {
    // gabien
    public String wordLoad, wordSave, wordInvalidFileName;
    // Generic
    public String bContinue, bConfigV;
    public String bOk, bCancel, bConfirm, bCopy, bPaste, bUndo;
    public String bConfigN, bQuit, bOpen, bNew, bSaveAs, bFile, bAccept;
    public String bSize, bOffset;
    // launcher
    public String bSelectEngine, lFrameMS, lGamePath, lSecondaryPath, lChooseEngine;
    public String bBack;
    public String accessLauncherREPL;
    public String dumpLLang;
    // help
    public String helpUnavailable;
    // app
    public FF1 loadingProgress;
    public String loadingDCO;
    public String dlgReloadPFD, dlgReloadED, dlgNoSysDump;
    // img
    public FF1 img_bmpX;
    public String img_png32;
    public String img_png8;
    public String img_xyz;
    // errorhandler
    public String err_hasOccurred, err_appWasStarted;
    public String err_backupOk, err_backupFail;
    public String err_footer;
}
