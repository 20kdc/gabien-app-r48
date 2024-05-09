/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.IntArrayIterable;
import r48.io.IntUtils;
import r48.io.data.*;
import r48.io.data.obj.DMCXInteger;
import r48.io.data.obj.DMCXObject;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import gabien.uslx.io.MemoryishR;

/**
 * A MoveCommand! It lets stuff move.
 * Created on 02/06/17.
 */
public class MoveCommand extends IRIOFixedObject implements IR2kInterpretable {
    @DMFXOBinding("@code") @DMCXInteger(0)
    public IRIOFixnum code;
    @DMFXOBinding("@parameters") @DMCXObject
    public ParameterArray parameters;

    public MoveCommand(DMContext ctx) {
        super(ctx, "RPG::MoveCommand");
    }

    // 0x100 is the 'string flag'
    // remaining lower byte is the amount of parameter ints
    private static int moveCommandClassifier(int code) {
        int para = 0;
        if ((code == 34) || (code == 35))
            para |= 0x100;
        if ((code == 32) || (code == 33) || (code == 34) || (code == 35))
            para += 1;
        if (code == 35)
            para += 2;
        return para;
    }

    public static MoveCommand[] fromEmbeddedData(DMContext ctx, int[] remainingStream) {
        try {
            return fromEmbeddedDataInside(ctx, remainingStream);
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("In MoveCommand stream");
            for (int i : remainingStream) {
                sb.append(' ');
                sb.append(i);
            }
            throw new RuntimeException(sb.toString(), ex);
        }
    }

    // public for tests
    public static int popMetaInteger(Iterator<Integer> si) {
        int res = 0;
        while (true) {
            int val = si.next();
            res = (res << 7) | (val & 0x7F);
            if ((val & 0x80) == 0)
                break;
        }
        return res;
    }

    // also public for tests
    public static void addMetaInteger(List<Integer> si, int val) {
        addMetaInteger(si, val, false);
    }

    private static void addMetaInteger(List<Integer> si, int val, boolean markLast) {
        if ((val & 0xFFFFFF80) != 0)
            addMetaInteger(si, (val >> 7) & 0x01FFFFFF, true);
        val &= 0x7F;
        if (markLast)
            val |= 0x80;
        si.add(val);
    }

    private static MoveCommand[] fromEmbeddedDataInside(DMContext ctx, int[] remainingStream) {
        Iterator<Integer> si = new IntArrayIterable.ArrayIterator(remainingStream);
        LinkedList<MoveCommand> mcs = new LinkedList<MoveCommand>();
        while (si.hasNext()) {
            int code = si.next();
            MoveCommand mc = new MoveCommand(ctx);
            mc.code.setFX(code);

            IRIOFixnum a = new IRIOFixnum(ctx, 0);
            IRIOFixnum b = new IRIOFixnum(ctx, 0);
            IRIOFixnum c = new IRIOFixnum(ctx, 0);

            mc.parameters.arrVal = new IRIO[] {
                    a,
                    b,
                    c
            };

            int mcc = moveCommandClassifier(code);

            if ((mcc & 0x100) != 0) {
                byte[] newText = new byte[popMetaInteger(si)];
                for (int i = 0; i < newText.length; i++)
                    newText[i] = (byte) (int) si.next();
                mc.parameters.text.putBuffer(newText);
            }

            if ((mcc & 0xFF) > 0)
                a.setFX(popMetaInteger(si));
            if ((mcc & 0xFF) > 1)
                b.setFX(popMetaInteger(si));
            if ((mcc & 0xFF) > 2)
                c.setFX(popMetaInteger(si));
            if ((mcc & 0xFF) > 3)
                throw new RuntimeException("invalid MCC");
            mcs.add(mc);
        }
        return mcs.toArray(new MoveCommand[0]);
    }

    public static int[] toEmbeddedData(IRIOFixedArray<MoveCommand> moveCommands) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        for (IRIO mci : moveCommands.arrVal) {
            MoveCommand mc = (MoveCommand) mci;
            int codeVal = (int) mc.code.getFX();
            res.add(codeVal);

            int mcc = moveCommandClassifier(codeVal);

            if ((mcc & 0x100) != 0) {
                MemoryishR text = mc.parameters.text.getBuffer();
                addMetaInteger(res, (int) text.length);
                for (int i = 0; i < text.length; i++)
                    res.add(text.getU8(i));
            }

            if ((mcc & 0xFF) > 0)
                addMetaInteger(res, (int) mc.parameters.arrVal[0].getFX());
            if ((mcc & 0xFF) > 1)
                addMetaInteger(res, (int) mc.parameters.arrVal[1].getFX());
            if ((mcc & 0xFF) > 2)
                addMetaInteger(res, (int) mc.parameters.arrVal[2].getFX());
            if ((mcc & 0xFF) > 3)
                throw new RuntimeException("invalid MCC");
        }
        int[] r = new int[res.size()];
        int idx = 0;
        for (Integer i : res)
            r[idx++] = i;
        return r;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        int codeVal = R2kUtil.readLcfVLI(bais);
        code.setFX(codeVal);
        addIVar("@parameters");
        IRIOFixnum a = new IRIOFixnum(context, 0);
        IRIOFixnum b = new IRIOFixnum(context, 0);
        IRIOFixnum c = new IRIOFixnum(context, 0);

        parameters.arrVal = new IRIO[] {
                a,
                b,
                c
        };

        int mcc = moveCommandClassifier(codeVal);

        if ((mcc & 0x100) != 0)
            parameters.text.putBuffer(IntUtils.readBytes(bais, R2kUtil.readLcfVLI(bais)));

        for (int i = 0; i < (mcc & 0xFF); i++)
            parameters.arrVal[i].setFX(R2kUtil.readLcfVLI(bais));
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        int codeVal = (int) code.getFX();
        R2kUtil.writeLcfVLI(baos, codeVal);

        int mcc = moveCommandClassifier(codeVal);

        if ((mcc & 0x100) != 0) {
            MemoryishR data = parameters.text.getBuffer();
            R2kUtil.writeLcfVLI(baos, (int) data.length);
            data.getBulk(baos);
        }
        for (int i = 0; i < (mcc & 0xFF); i++)
            R2kUtil.writeLcfVLI(baos, (int) parameters.arrVal[i].getFX());
    }
}
