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
import r48.map.imaging.IImageLoader;
import r48.schema.BooleanSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Created 24th March 2023.
 */
public class XPTroopGenposFrame extends TroopGenposFrame {

    public static final int[] gameBattleDisplay = new int[] {
            0, 0,
    };

    public IImage[] enemies;

    public XPTroopGenposFrame(App app, IRIO t, SchemaPath path, Runnable change) {
        super(app, t, path, change);
        // Immediately try and get needed resources
        IRIO enemi = app.odb.getObject("Enemies").getObject();
        IImageLoader img = app.stuffRendererIndependent.imageLoader;

        enemies = new IImage[enemi.getALen()];
        for (int i = 0; i < enemies.length; i++) {
            enemies[i] = readEnemy(enemi.getAElem(i), img);
        }
    }

    private IImage readEnemy(IRIO value, IImageLoader img) {
        if (SchemaElement.checkType(value, 'o', "RPG::Enemy", false))
            return null;
        IImage im = img.getImage("Battlers/" + value.getIVar("@battler_name").decString(), false);
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
                new BooleanSchemaElement(app, false),
                new BooleanSchemaElement(app, false)
        };
    }

    @Override
    public SchemaPath getCellProp(int ct, int i) {
        SchemaPath memberPath = troopPath.otherIndex("@members").arrayHashIndex(DMKey.of(ct + 1), "[" + (ct + 1) + "]");
        IRIO member = troop.getIVar("@members").getAElem(ct + 1);
        SchemaElement se = getCellPropSchemas()[i];
        if (i == 0)
            return memberPath.newWindow(se, member.getIVar("@enemy_id"));
        if (i == 1)
            return memberPath.newWindow(se, member.getIVar("@x"));
        if (i == 2)
            return memberPath.newWindow(se, member.getIVar("@y"));
        if (i == 3)
            return memberPath.newWindow(se, member.getIVar("@hidden"));
        if (i == 4)
            return memberPath.newWindow(se, member.getIVar("@immortal"));
        throw new RuntimeException("Invalid cell prop.");
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                T.z.l120,
                T.z.l121,
                T.z.l122,
                T.z.l123,
                T.gp.troop_immortal
        };
    }

    @Override
    public IImage getBackground() {
        return null;
    }
}
