/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.specialized;

import gabien.ui.UIElement;
import r48.RubyTable;
import r48.io.data.IRIO;
import r48.io.data.IRIOTypedMask;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Yet another magical binding replacement.
 * Created 10th May, 2024.
 */
public class RubyTableIndividualCellSchemaElement extends SchemaElement {
    public final int x, y, plane;
    public final SchemaElement interior;

    public RubyTableIndividualCellSchemaElement(int x, int y, int plane, SchemaElement interior) {
        super(interior.app);
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.interior = interior;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return interior.buildHoldingEditor(new CellMask(target), launcher, path);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        interior.modifyVal(new CellMask(target), path, setDefault);
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        interior.visit(new CellMask(target), path, v, detailedPaths);
    }

    public class CellMask extends IRIOTypedMask {
        public final RubyTable rt;
        public CellMask(IRIO target) {
            super(target.context);
            rt = new RubyTable(target.editUser());
        }

        @Override
        public long getFX() {
            return rt.getTiletype(x, y, plane);
        }

        @Override
        public IRIO setFX(long fx) {
            rt.setTiletype(x, y, plane, (short) fx);
            return this;
        }

        @Override
        public IRIO addIVar(String sym) {
            return null;
        }

        @Override
        public IRIO getIVar(String sym) {
            return null;
        }

        @Override
        public int getType() {
            return 'i';
        }

        @Override
        public String[] getIVars() {
            return new String[0];
        }
        
    }
}
