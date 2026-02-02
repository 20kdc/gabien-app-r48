/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.textboxes;

import r48.R48;
import r48.dbs.ObjectRootHandle;
import r48.dbs.PathSyntax;
import r48.tr.TrPage.FF0;

/**
 * Split from StringLenSchemaElement 12th October 2024.
 */
public class StringConfigLenSchemaElement extends StringLenSchemaElement {
    public String lenRoot;
    public PathSyntax lenPath;

    public StringConfigLenSchemaElement(R48 app, FF0 arg, String lenRoot, PathSyntax lenPath) {
        super(app, arg);
        this.lenRoot = lenRoot;
        this.lenPath = lenPath;
    }

    @Override
    public int getLen() {
        ObjectRootHandle oh = app.odb.getObject(lenRoot);
        return (int) lenPath.getRO(oh.getObject()).getFX();
    }
}
