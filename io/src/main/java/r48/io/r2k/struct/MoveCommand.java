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
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A MoveCommand! It lets stuff move.
 * Created on 02/06/17.
 */
public class MoveCommand extends IRIOFixedObject implements IR2kInterpretable {
    @DM2FXOBinding("@code")
    public IRIOFixnum code;
    @DM2FXOBinding("@parameters")
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
            mc.code.val = code;

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
                mc.parameters.text.data = new byte[popMetaInteger(si)];
                for (int i = 0; i < mc.parameters.text.data.length; i++)
                    mc.parameters.text.data[i] = (byte) (int) si.next();
            }

            if ((mcc & 0xFF) > 0)
                a.val = popMetaInteger(si);
            if ((mcc & 0xFF) > 1)
                b.val = popMetaInteger(si);
            if ((mcc & 0xFF) > 2)
                c.val = popMetaInteger(si);
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
            res.add((int) mc.code.val);

            int mcc = moveCommandClassifier((int) mc.code.val);

            if ((mcc & 0x100) != 0) {
                byte[] text = mc.parameters.text.data;
                addMetaInteger(res, text.length);
                for (byte b : text)
                    res.add(((int) b) & 0xFF);
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
        code.val = R2kUtil.readLcfVLI(bais);
        addIVar("@parameters");
        IRIOFixnum a = new IRIOFixnum(context, 0);
        IRIOFixnum b = new IRIOFixnum(context, 0);
        IRIOFixnum c = new IRIOFixnum(context, 0);

        parameters.arrVal = new IRIO[] {
                a,
                b,
                c
        };

        int mcc = moveCommandClassifier((int) code.val);

        if ((mcc & 0x100) != 0)
            parameters.text.data = IntUtils.readBytes(bais, R2kUtil.readLcfVLI(bais));

        for (int i = 0; i < (mcc & 0xFF); i++)
            parameters.arrVal[i].setFX(R2kUtil.readLcfVLI(bais));
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, (int) code.val);

        int mcc = moveCommandClassifier((int) code.val);

        if ((mcc & 0x100) != 0) {
            byte[] data = parameters.text.data;
            R2kUtil.writeLcfVLI(baos, data.length);
            baos.write(data);
        }
        for (int i = 0; i < (mcc & 0xFF); i++)
            R2kUtil.writeLcfVLI(baos, (int) parameters.arrVal[i].getFX());
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@code"))
            return code = new IRIOFixnum(context, 0);
        if (sym.equals("@parameters"))
            return parameters = new ParameterArray(dm2Ctx);
        return null;
    }
}
