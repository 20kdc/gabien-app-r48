/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import r48.io.data.IRIO;

/**
 * For anything that looks even vaguely like:
 * SOME NAME | SOME CONTENTS
 * Based off of the system used in IVarSchemaElement.
 * Created on 29/06/17.
 */
public interface IFieldSchemaElement {
    int getDefaultFieldWidth(IRIO target);

    // NOTE: This is good for one buildHoldingEditor only.
    void setFieldWidthOverride(int w);
}
