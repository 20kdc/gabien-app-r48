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

import java.util.HashMap;

/**
 * Just boring stuff, really...
 * Created on 31/05/17.
 */
public class MapTreeStart extends R2kObject {
    public IntegerR2kProp playerMap = new IntegerR2kProp(0);
    public IntegerR2kProp playerX = new IntegerR2kProp(0);
    public IntegerR2kProp playerY = new IntegerR2kProp(0);

    public IntegerR2kProp boatMap = new IntegerR2kProp(0);
    public IntegerR2kProp boatX = new IntegerR2kProp(0);
    public IntegerR2kProp boatY = new IntegerR2kProp(0);

    public IntegerR2kProp shipMap = new IntegerR2kProp(0);
    public IntegerR2kProp shipX = new IntegerR2kProp(0);
    public IntegerR2kProp shipY = new IntegerR2kProp(0);

    public IntegerR2kProp airshipMap = new IntegerR2kProp(0);
    public IntegerR2kProp airshipX = new IntegerR2kProp(0);
    public IntegerR2kProp airshipY = new IntegerR2kProp(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, playerMap),
                new Index(0x02, playerX),
                new Index(0x03, playerY),
                new Index(0x0B, boatMap),
                new Index(0x0C, boatX),
                new Index(0x0D, boatY),
                new Index(0x15, shipMap),
                new Index(0x16, shipX),
                new Index(0x17, shipY),
                new Index(0x1F, airshipMap),
                new Index(0x20, airshipX),
                new Index(0x21, airshipY),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO mt = new RubyIO().setSymlike("RPG::Start", true);
        mt.iVars.put("@player_map", new RubyIO().setFX(playerMap.i));
        mt.iVars.put("@player_x", new RubyIO().setFX(playerX.i));
        mt.iVars.put("@player_y", new RubyIO().setFX(playerY.i));

        mt.iVars.put("@boat_map", new RubyIO().setFX(boatMap.i));
        mt.iVars.put("@boat_x", new RubyIO().setFX(boatX.i));
        mt.iVars.put("@boat_y", new RubyIO().setFX(boatY.i));

        mt.iVars.put("@ship_map", new RubyIO().setFX(shipMap.i));
        mt.iVars.put("@ship_x", new RubyIO().setFX(shipX.i));
        mt.iVars.put("@ship_y", new RubyIO().setFX(shipY.i));

        mt.iVars.put("@airship_map", new RubyIO().setFX(airshipMap.i));
        mt.iVars.put("@airship_x", new RubyIO().setFX(airshipX.i));
        mt.iVars.put("@airship_y", new RubyIO().setFX(airshipY.i));
        R2kUtil.unkToRio(mt, unknownChunks);
        return mt;
    }
}
