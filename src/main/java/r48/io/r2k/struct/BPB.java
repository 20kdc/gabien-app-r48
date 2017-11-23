/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import gabien.ui.ISupplier;
import r48.RubyIO;
import r48.io.r2k.chunks.ArrayR2kStruct;
import r48.io.r2k.chunks.IR2kStruct;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BPB
 * Created on August 31st 2017
 */
public class BPB implements IR2kStruct {
    public ArrayR2kStruct<BattleParamBlock> parameters1 = new ArrayR2kStruct<BattleParamBlock>(null, new ISupplier<BattleParamBlock>() {
        @Override
        public BattleParamBlock get() {
            return new BattleParamBlock();
        }
    });
    public ArrayR2kStruct<BattleParamBlock> parameters2 = null;

    public BPB() {
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::BPB", true);
        rio.addIVar("@1to50", parameters1.asRIO());
        if (parameters2 != null)
            rio.addIVar("@51to99_2k3", parameters2.asRIO());
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        parameters1.fromRIO(src.getInstVarBySymbol("@1to50"));
        if (src.getInstVarBySymbol("@51to99_2k3") != null) {
            initP2();
            parameters2.fromRIO(src.getInstVarBySymbol("@51to99_2k3"));
        }
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        // 6 * short size * 50 'lower'
        // the rest is handed to the 2k3 block
        byte[] md = new byte[50 * 6 * 2];
        int sz = bais.read(md);
        // Just hope sz is right.
        byte[] sub = new byte[sz];
        System.arraycopy(md, 0, sub, 0, sz);
        parameters1.importData(new ByteArrayInputStream(sub));
        if (bais.available() > 0) {
            initP2();
            parameters2.importData(bais);
        } else {
            parameters2 = null;
        }
    }

    private void initP2() {
        parameters2 = new ArrayR2kStruct<BattleParamBlock>(null, new ISupplier<BattleParamBlock>() {
            @Override
            public BattleParamBlock get() {
                return new BattleParamBlock();
            }
        }, true);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        parameters1.exportData(baos);
        if (parameters2 != null)
            parameters2.exportData(baos);
        return false;
    }
}
