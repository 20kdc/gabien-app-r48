/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.struct;

import r48.RubyIO;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Stack;

/**
 * A MoveCommand! It lets stuff move.
 * Created on 02/06/17.
 */
public class MoveCommand implements IR2kStruct {
    public int code;
    public byte[] text;
    public int a, b, c;

    public static MoveCommand[] fromEmbeddedData(int[] remainingStream) {
        Stack<Integer> si = new Stack<Integer>();
        for (int i = remainingStream.length - 1; i >= 0; i--)
            si.push(remainingStream[i]);
        LinkedList<MoveCommand> mcs = new LinkedList<MoveCommand>();
        while (!si.empty()) {
            int code = si.pop();
            MoveCommand mc = new MoveCommand();
            mc.code = code;
            mc.text = new byte[0];
            if ((code == 34) || (code == 35)) {
                mc.text = new byte[si.pop()];
                for (int i = 0; i < mc.text.length; i++)
                    mc.text[i] = (byte) (int) si.pop();
            }
            if ((code == 32) || (code == 33) || (code == 34) || (code == 35))
                mc.a = si.pop();
            if (code == 35) {
                mc.b = si.pop();
                mc.c = si.pop();
            }
            mcs.add(mc);
        }
        return mcs.toArray(new MoveCommand[0]);
    }

    public static int[] toEmbeddedData(MoveCommand[] moveCommands) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        for (MoveCommand mc : moveCommands) {
            res.add(mc.code);
            if ((mc.code == 34) || (mc.code == 35)) {
                res.add(mc.text.length);
                for (byte b : mc.text)
                    res.add(((int) b) & 0xFF);
            }
            if ((mc.code == 32) || (mc.code == 33) || (mc.code == 34) || (mc.code == 35))
                res.add(mc.a);
            if (mc.code == 35) {
                res.add(mc.b);
                res.add(mc.c);
            }
        }
        int[] r = new int[res.size()];
        int idx = 0;
        for (Integer i : r)
            r[idx++] = i;
        return r;
    }

    public void fromRIO(RubyIO rubyIO) {
        code = (int) rubyIO.getInstVarBySymbol("@code").fixnumVal;

        RubyIO[] p = rubyIO.getInstVarBySymbol("@parameters").arrVal;
        text = p[0].strVal;
        a = (int) p[1].fixnumVal;
        b = (int) p[2].fixnumVal;
        c = (int) p[3].fixnumVal;
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::MoveCommand", true);
        RubyIO[] p = new RubyIO[4];
        p[0] = new RubyIO().setString(text);
        p[1] = new RubyIO().setFX(a);
        p[2] = new RubyIO().setFX(b);
        p[3] = new RubyIO().setFX(c);
        RubyIO params = new RubyIO();
        params.type = '[';
        params.arrVal = p;
        rio.iVars.put("@code", new RubyIO().setFX(code));
        rio.iVars.put("@parameters", params);
        return rio;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        code = R2kUtil.readLcfVLI(bais);
        text = R2kUtil.readLcfBytes(bais, R2kUtil.readLcfVLI(bais));
        a = R2kUtil.readLcfVLI(bais);
        b = R2kUtil.readLcfVLI(bais);
        c = R2kUtil.readLcfVLI(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, code);
        R2kUtil.writeLcfVLI(baos, text.length);
        baos.write(text);
        R2kUtil.writeLcfVLI(baos, a);
        R2kUtil.writeLcfVLI(baos, b);
        R2kUtil.writeLcfVLI(baos, c);
        return false;
    }
}
