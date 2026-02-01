/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos.backend;

import gabien.render.IImage;
import r48.App;
import r48.imagefx.HueShiftImageEffect;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.schema.BooleanSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.SchemaPath;
import r48.texture.ITexLoader;

/**
 * Created on 28/07/17. split 24th March 2023
 */
public class R2kTroopGenposFrame extends TroopGenposFrame {

    public static final int[] gameBattleDisplay = new int[] {
            // 320x160 (RPG Maker 2000)
            0, 0,
            320, 0,
            0, 160,
            320, 160,
            // 2k3?
            0, 240,
            320, 240
    };

    public IImage battleBkg;

    public R2kTroopGenposFrame(App app, IRIO t, SchemaPath path, Runnable change) {
        super(app, t, path, change);
        // Immediately try and get needed resources
        IRIO database = app.odb.getObject("RPG_RT.ldb").getObject();
        ITexLoader img = app.stuffRendererIndependent.imageLoader;
        battleBkg = img.getImage("Backdrop/" + database.getIVar("@system").getIVar("@test_battle_background").decString(), true);
        long max = 0;

        IRIO enemi = database.getIVar("@enemies");

        for (DMKey rio : enemi.getHashKeys())
            max = Math.max(max, rio.getFX());
        enemies = new IImage[(int) (max + 1)];
        for (DMKey map : enemi.getHashKeys())
            enemies[(int) (map.getFX())] = readEnemy(enemi.getHashVal(map), img);
    }

    @Override
    public boolean isStillValid() {
        return troop.getType() == 'o';
    }

    private IImage readEnemy(IRIO value, ITexLoader img) {
        IImage im = img.getImage("Monster/" + value.getIVar("@battler_name").decString(), false);
        return app.ui.imageFXCache.process(im, new HueShiftImageEffect((int) value.getIVar("@battler_hue").getFX()));
    }

    @Override
    public int[] getIndicators() {
        return gameBattleDisplay;
    }

    private SchemaElement[] getCellPropSchemas() {
        return new SchemaElement[] {
                app.sdb.getSDBEntry("enemy_id"),
                new IntegerSchemaElement(app, 0),
                new IntegerSchemaElement(app, 0),
                new BooleanSchemaElement(app, false)
        };
    }

    @Override
    public SchemaPath getCellProp(int ct, int i) {
        SchemaPath memberPath = troopPath.otherIndex("@members").arrayHashIndex(DMKey.of(ct + 1), "[" + (ct + 1) + "]");
        IRIO member = troop.getIVar("@members").getAElem(ct + 1);
        SchemaElement se = getCellPropSchemas()[i];
        if (i == 0)
            return memberPath.newWindow(se, member.getIVar("@enemy"));
        if (i == 1)
            return memberPath.newWindow(se, member.getIVar("@x"));
        if (i == 2)
            return memberPath.newWindow(se, member.getIVar("@y"));
        if (i == 3)
            return memberPath.newWindow(se, member.getIVar("@invisible"));
        throw new RuntimeException("Invalid cell prop.");
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                T.gp.cEID,
                T.gp.cX,
                T.gp.cY,
                T.gp.cInv
        };
    }

    @Override
    public IImage getBackground() {
        return battleBkg;
    }
}
