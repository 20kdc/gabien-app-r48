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
    public String done, confirm_accept, confirm_cancel;
    public FF2 contextError;
    public String helpTitle, helpIndex, newDirs;
    public String revertWarn;
    public String notRelease;
    public String odb_disposed, odb_created, odb_modified, odb_lost;
    public FF1 odb_listeners, odb_saveErr, odb_loadObj;
    public String test_binding, test_prFail, test_prOk, test_PTS, test_PTF, test_back, test_toREPL, test_toREPLOk, test_withSchema;
    public String np_synthOk, np_synth2kQ, np_r2k0, np_r2k3, np_nothing;
    public String spr_num, spr_msgNoImage;
    public String soundFail;
    public String cg_savePNG;
    public String cg_copyR48;
    public FF2 frameDisplay;
    public String set_selAll, set_deSelAll;
    public String lAlphaChannel, lHSVRecommend;
    public String bEnumRename, enumOptions, bEnumManual;
    public String usl_full, usl_partial, usl_from, usl_to, usl_addR, usl_confirmReplace;
    public FF2 usl_completeReport;
    public String tsc_dumpOk, tsc_cev, tsc_ctx;
    public FF1 tsc_map;
    public FF3 tsc_ev;
    public String shcEmpty, shcIncompatible;
    public String shNoCloneTmp;
    public String openAud;
    public String bts_ramObj;

    public String mGetGPUInfo;

    public String disableMapRendering;
    public String disableMapAnimation;
}
