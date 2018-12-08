/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import r48.io.IntUtils;
import r48.io.data.*;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Stack;

/**
 * A MoveCommand! It lets stuff move.
 * Created on 02/06/17.
 */
public class MoveCommand extends IRIOFixedObject implements IR2kInterpretable {
    @DM2FXOBinding("@code")
    public IRIOFixnum code;
    @DM2FXOBinding("@parameters")
    public ParameterArray parameters;

    public MoveCommand() {
        super("RPG::MoveCommand");
    }

    public static MoveCommand[] fromEmbeddedData(int[] remainingStream) {
        Stack<Integer> si = new Stack<Integer>();
        for (int i = remainingStream.length - 1; i >= 0; i--)
            si.push(remainingStream[i]);
        LinkedList<MoveCommand> mcs = new LinkedList<MoveCommand>();
        while (!si.empty()) {
            int code = si.pop();
            MoveCommand mc = new MoveCommand();
            mc.code.val = code;

            IRIOFixnum a = new IRIOFixnum(0);
            IRIOFixnum b = new IRIOFixnum(0);
            IRIOFixnum c = new IRIOFixnum(0);

            mc.parameters.arrVal = new IRIO[] {
                    a,
                    b,
                    c
            };

            if ((code == 34) || (code == 35)) {
                mc.parameters.text.data = new byte[si.pop()];
                for (int i = 0; i < mc.parameters.text.data.length; i++)
                    mc.parameters.text.data[i] = (byte) (int) si.pop();
            }
            if ((code == 32) || (code == 33) || (code == 34) || (code == 35))
                a.val = si.pop();
            if (code == 35) {
                b.val = si.pop();
                c.val = si.pop();
            }
            mcs.add(mc);
        }
        return mcs.toArray(new MoveCommand[0]);
    }

    public static int[] toEmbeddedData(IRIOFixedArray<MoveCommand> moveCommands) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        for (IRIO mci : moveCommands.arrVal) {
            MoveCommand mc = (MoveCommand) mci;
            res.add((int) mc.code.val);
            if ((mc.code.val == 34) || (mc.code.val == 35)) {
                byte[] text = mc.parameters.text.data;
                res.add(text.length);
                for (byte b : text)
                    res.add(((int) b) & 0xFF);
            }
            if ((mc.code.val == 32) || (mc.code.val == 33) || (mc.code.val == 34) || (mc.code.val == 35))
                res.add((int) mc.parameters.arrVal[0].getFX());
            if (mc.code.val == 35) {
                res.add((int) mc.parameters.arrVal[1].getFX());
                res.add((int) mc.parameters.arrVal[2].getFX());
            }
        }
        int[] r = new int[res.size()];
        int idx = 0;
        for (Integer i : res)
            r[idx++] = i;
        return r;
    }

    public void fromRIO(IRIO rubyIO) {
        setDeepClone(rubyIO);
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        code.val = R2kUtil.readLcfVLI(bais);
        addIVar("@parameters");
        parameters.text.data = IntUtils.readBytes(bais, R2kUtil.readLcfVLI(bais));
        IRIOFixnum a = new IRIOFixnum(R2kUtil.readLcfVLI(bais));
        IRIOFixnum b = new IRIOFixnum(R2kUtil.readLcfVLI(bais));
        IRIOFixnum c = new IRIOFixnum(R2kUtil.readLcfVLI(bais));
        parameters.arrVal = new IRIO[] {
                a,
                b,
                c
        };
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, (int) code.val);
        byte[] data = parameters.text.data;
        R2kUtil.writeLcfVLI(baos, data.length);
        baos.write(data);
        R2kUtil.writeLcfVLI(baos, (int) parameters.arrVal[0].getFX());
        R2kUtil.writeLcfVLI(baos, (int) parameters.arrVal[1].getFX());
        R2kUtil.writeLcfVLI(baos, (int) parameters.arrVal[2].getFX());
        return false;
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@code"))
            return code = new IRIOFixnum(0);
        if (sym.equals("@parameters"))
            return parameters = new ParameterArray();
        return null;
    }
}
