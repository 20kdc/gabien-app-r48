/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr.pages;

import r48.tr.TrPage;

/**
 * Translation page for AppUI.
 * Created 2nd March 2023.
 */
public class TrAppUI extends TrPage {
    public String init, init2, initMapScan;
    public FF1 initTab;
    public String infoTitle, errorTitle, done, confirm_title, confirm_accept, confirm_cancel;
    public FF2 contextError;
    public String helpTitle, helpIndex, newDirs;
    public String revertWarn;
    public String notRelease;
    public String odb_disposed, odb_created, odb_modified, odb_lost;
    public FF1 odb_listeners;
    public String test_binding, test_prFail, test_prOk, test_PTS, test_PTF, test_back;
    public String np_synthOk, np_synth2kQ, np_r2k0, np_r2k3, np_nothing;
}
