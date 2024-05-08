/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.dm2chk.DM2Array;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BPB
 * Created on August 31st 2017
 */
public class BPB extends IRIOFixedObject implements IR2kInterpretable {
    @DMFXOBinding("@1to50")
    public DM2Array<BattleParamBlock> parameters1;
    @DMOptional @DMFXOBinding("@51to99_2k3")
    public DM2Array<BattleParamBlock> parameters2;

    public BPB(DMContext ctx) {
        super(ctx, "RPG::BPB");
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        // 6 * short size * 50 'lower'
        // the rest is handed to the 2k3 block
        byte[] md = new byte[50 * 6 * 2];
        bais.read(md);
        parameters1.importData(new ByteArrayInputStream(md));
        if (bais.available() > 0) {
            addIVar("@51to99_2k3");
            parameters2.importData(bais);
        } else {
            parameters2 = null;
        }
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        parameters1.exportData(baos);
        if (parameters2 != null)
            parameters2.exportData(baos);
    }

    public void parameters1_add() {
        parameters1 = new DM2Array<BattleParamBlock>(context) {
            @Override
            public BattleParamBlock newValue() {
                return new BattleParamBlock(context);
            }
        };
        parameters1.arrVal = new IRIO[50];
        for (int i = 0; i < 50; i++)
            parameters1.arrVal[i] = new BattleParamBlock(context);
    }

    public void parameters2_add() {
        parameters2 = new DM2Array<BattleParamBlock>(context) {
            @Override
            public BattleParamBlock newValue() {
                return new BattleParamBlock(context);
            }
        };
    }
}
