/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.chunks.IR2kStruct;
import r48.schema.integers.IntegerSchemaElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Solves the FT3 memory problem by cutting the branches.
 * Created on February 10th 2018.
 */
public class LcfMagicalBinder implements IMagicalBinder {
    private final ISupplier<IR2kStruct> inner;
    public LcfMagicalBinder(ISupplier<IR2kStruct> iSupplier) {
        inner = iSupplier;
    }

    @Override
    public RubyIO targetToBound(RubyIO target) {
        IR2kStruct s = inner.get();
        try {
            s.importData(new ByteArrayInputStream(target.userVal));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return s.asRIO();
    }

    @Override
    public boolean applyBoundToTarget(RubyIO bound, RubyIO target) {
        IR2kStruct s = inner.get();
        s.fromRIO(bound);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            s.exportData(baos);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        byte[] tba = baos.toByteArray();
        // Try to ensure target is a blob.
        if (IntegerSchemaElement.ensureType(target, 'u', false)) {
            target.setSymlike("Blob", false);
        } else if (!target.symVal.equals("Blob")) {
            target.setSymlike("Blob", false);
        } else {
            if (target.userVal.length == tba.length) {
                boolean same = true;
                for (int i = 0; i < tba.length; i++) {
                    if (tba[i] != target.userVal[i]) {
                        same = false;
                        break;
                    }
                }
                if (same)
                    return false;
            }
        }
        target.userVal = tba;
        return true;
    }
}
