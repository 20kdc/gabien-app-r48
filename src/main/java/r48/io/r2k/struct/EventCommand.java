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

/**
 * What is this again?
 * Created on 31/05/17.
 */
public class EventCommand implements IR2kStruct {
    public int code, indent;
    public byte[] text;
    public int[] parameters;

    @Override
    public void importData(InputStream bais) throws IOException {
        code = R2kUtil.readLcfVLI(bais);
        indent = R2kUtil.readLcfVLI(bais);
        text = R2kUtil.readLcfBytes(bais, R2kUtil.readLcfVLI(bais));
        parameters = new int[R2kUtil.readLcfVLI(bais)];
        for (int i = 0; i < parameters.length; i++)
            parameters[i] = R2kUtil.readLcfVLI(bais);
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, code);
        R2kUtil.writeLcfVLI(baos, indent);
        R2kUtil.writeLcfVLI(baos, text.length);
        baos.write(text);
        R2kUtil.writeLcfVLI(baos, parameters.length);
        for (int i = 0; i < parameters.length; i++)
            R2kUtil.writeLcfVLI(baos, parameters[i]);
        return false;
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::EventCommand", true);
        mt.iVars.put("@code", new RubyIO().setFX(code));
        mt.iVars.put("@indent", new RubyIO().setFX(indent));
        RubyIO[] params = new RubyIO[parameters.length + 1];
        params[0] = new RubyIO().setString(text);
        for (int i = 0; i < parameters.length; i++)
            params[i + 1] = new RubyIO().setFX(parameters[i]);
        RubyIO paramArr = new RubyIO();
        paramArr.type = '[';
        paramArr.arrVal = params;
        mt.iVars.put("@parameters", paramArr);
        return mt;
    }

    @Override
    public void fromRIO(RubyIO src) {
        code = (int) src.getInstVarBySymbol("@code").fixnumVal;
        indent = (int) src.getInstVarBySymbol("@indent").fixnumVal;
        RubyIO[] params = src.getInstVarBySymbol("@parameters").arrVal;
        // Just in case.
        if (params[0].type != '"')
            throw new RuntimeException("CORRUPTION! First parameter must be string. " + code + " " + indent);
        text = params[0].strVal;
        parameters = new int[params.length - 1];
        for (int i = 1; i < params.length; i++) {
            if (params[i].type != 'i')
                throw new RuntimeException("CORRUPTION! Non-first parameter must be int. " + code + " " + indent);
            parameters[i - 1] = (int) params[i].fixnumVal;
        }
    }
}
