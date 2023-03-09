/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr.pages;

import r48.tr.TrPage;

/**
 * Translation page for Schema stuff.
 * Created 8th March 2023.
 */
public class TrSchema extends TrPage {
    public String enum_id, enum_int, enum_sym, enum_code;
    public String cmdb_defCatName;
    public String cmdb_unkParamName;
    public FF1 bFileBrowser;
    public String bOpenTable;
    public String selectTileGraphic;
    public String ppp_constant, ppp_idVar, ppp_idNSfx;
    public String ppp_idVarFN, ppp_idFN, ppp_typeFN;
    public String ppp_explain;
    public String ppp_valueVarFN, ppp_isVarFN;
}
