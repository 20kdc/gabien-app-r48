/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr.pages;

import r48.tr.TrPage;

/**
 * Central hub for translation pages.
 * Elements should be single-letter unless you expect a field to almost never be directly accessed (fontSizes)
 * Created March 8th 2023.
 */
public class TrRoot extends TrPage {
    public final TrGlobal g = new TrGlobal();
    public final TrTitle t = new TrTitle();
    public final TrAppUI u = new TrAppUI();
    public final TrSchema s = new TrSchema();
    public final TrFontSizes fontSizes = new TrFontSizes();
    public final TrHTML h = new TrHTML();
    public final TrGenpos gp = new TrGenpos();
    public final TrImageEditor ie = new TrImageEditor();
    public final TrMap m = new TrMap();
}
