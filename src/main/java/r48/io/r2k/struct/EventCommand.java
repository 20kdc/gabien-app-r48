/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.struct;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;
import r48.io.r2k.chunks.R2kObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * What is this again?
 * Created on 31/05/17.
 */
public class EventCommand implements IR2kInterpretable {
    public int code, indent;
    public String text;
    public int[] parameters = new int[5];

    @Override
    public void importData(InputStream bais) throws IOException {
        code = R2kUtil.readLcfVLI(bais);
        indent = R2kUtil.readLcfVLI(bais);
        text = R2kUtil.decodeLcfString(R2kUtil.readLcfBytes(bais, R2kUtil.readLcfVLI(bais)));
        parameters = new int[R2kUtil.readLcfVLI(bais)];
        for (int i = 0; i < parameters.length; i++)
            parameters[i] = R2kUtil.readLcfVLI(bais);
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
}
