/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.obj;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IntegerR2kProp;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kProp;

/**
 * As the street-lights are turning on outside...
 * Created on 31/05/17.
 */
public class Music extends R2kObject {
    public StringR2kProp name = new StringR2kProp();
    public IntegerR2kProp fadeTime = new IntegerR2kProp(0);
    public IntegerR2kProp volume = new IntegerR2kProp(100);
    public IntegerR2kProp tempo = new IntegerR2kProp(100);
    public IntegerR2kProp balance = new IntegerR2kProp(50);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name),
                new Index(0x02, fadeTime),
                new Index(0x03, volume),
                new Index(0x04, tempo),
                new Index(0x05, balance)
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Music", true);
        rio.iVars.put("@name", new RubyIO().setString(name.data));
        rio.iVars.put("@fade", new RubyIO().setFX(fadeTime.i));
        rio.iVars.put("@volume", new RubyIO().setFX(volume.i));
        rio.iVars.put("@tempo", new RubyIO().setFX(tempo.i));
        rio.iVars.put("@balance", new RubyIO().setFX(balance.i));
        R2kUtil.unkToRio(rio, unknownChunks);
        return rio;
    }
}
