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
    // launcher
    public String bContinue, bConfigure, bSelectEngine, lFrameMS, lGamePath, lSecondaryPath, lChooseEngine;
    public String bConfig, bQuit;
    // help
    public String helpUnavailable;
    // app
    public FF1 loadingProgress;
    public String loadingDCO;
    public String msgNonEmergencyBackup;
    public String dlgReloadPFD, dlgReloadED, dlgNoSysDump;
}
