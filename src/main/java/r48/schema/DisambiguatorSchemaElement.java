/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.ui.UIElement;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.dbs.ValueSyntax;
import r48.io.data.IRIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;

/**
 * This is used for things like conditional branches, which are so complicated it's ridiculous.
 * There used to be a note here saying the element must be an enum,
 * but that was before the Decision where all elements started getting rebuilt out of sheer practicality.
 * That was 9 months ago. It's now the 8th of October.
 *
 * Notes on March 13th 2018:
 *  DisambiguatorSyntax is weird.
 *
 * Created on 12/31/16.
 */
public class DisambiguatorSchemaElement extends SchemaElement {
    // PathSyntax
    public String dIndex;

    // ValueSyntax
    // "" is default
    // there's some special translation stuff for SDB1 compat
    public HashMap<String, SchemaElement> dTable;

    public DisambiguatorSchemaElement(String disambiguatorIndex, HashMap<String, SchemaElement> disambiguations) {
        dIndex = disambiguatorIndex;
        dTable = disambiguations;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path2) {
        final SchemaPath path = path2.tagSEMonitor(target, this, true);
        String iv = getDisambigIndex(target);
        SchemaElement ise = getSchemaElement(iv);
        return ise.buildHoldingEditor(target, launcher, path);
    }

    private String getDisambigIndex(IRIO target) {
        target = PathSyntax.parse(target, dIndex);
        if (target == null)
            return "";
        return ValueSyntax.encode(target);
    }

    private SchemaElement getSchemaElement(String dVal) {
        SchemaElement r = dTable.get(dVal);
        if (r == null)
            r = dTable.get("");
        if (r == null)
            r = new AggregateSchemaElement(new SchemaElement[0]);
        return r;
    }

    // used by OCSE
    public SchemaElement getDisambiguation(RubyIO target) {
        return getSchemaElement(getDisambigIndex(target));
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path2, boolean setDefault) {
        final SchemaPath path = path2.tagSEMonitor(target, this, true);

        String iv = getDisambigIndex(target);
        if (!setDefault)
            if (iv.equals(""))
                System.out.println("Warning: Disambiguator working off of nothing here, this CANNOT GO WELL");
        try {
            SchemaElement ise = getSchemaElement(iv);
            ise.modifyVal(target, path, setDefault);
        } catch (RuntimeException e) {
            e.printStackTrace(System.out);
            System.out.println("ArrayDisambiguator Debug: " + iv);
            throw e;
        }
    }
}
