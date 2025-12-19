/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.ui.UIElement;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.displays.TonePickerSchemaElement;
import r48.schema.specialized.cmgb.IGroupBehavior;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.search.CommandTag;
import r48.tr.TrNames;
import r48.tr.TrPage.FF0;
import r48.tr.TrPage.FF1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSrcLoc;

/**
 * RPGCommand database entry.
 * Created on 12/30/16.
 */
public class RPGCommand extends App.Svc {
    public final int commandId;
    public final DatumSrcLoc srcLoc;
    public final String dbId, nameRawUnlocalized;
    public FF1 name;

    public SchemaElement specialSchema;
    /**
     * For use by LibLCF245Dumper
     */
    public boolean specialSchemaEssential;

    // As the entire paramType cannot be replaced (paramType is relied upon for parameter names), this instead allows supplementing it.
    public LinkedList<LinkedList<SpecialTag>> paramSpecialTags = new LinkedList<LinkedList<SpecialTag>>();
    public LinkedList<Param> params = new LinkedList<Param>();
    public int indentPre;
    /**
     * This is conditional solely because of Show Inn (R2k).
     * Importantly, this accepts the parameters object, like all roots here. There have been some callers disrespecting this.
     */
    public Function<IRIO, Integer> indentPost = (params) -> 0;
    // Something that can also go before this command instead of a block leave
    public int blockLeaveReplacement = -1;
    public boolean needsBlockLeavePre;
    // typeBlockLeave fulfills any block leave relation
    // typeListLeave fulfills the list leave
    // typeRestrictiveBlockLeave can only exist when it is required to fulfill a leave relation
    public boolean typeBlockLeave;
    public boolean typeListLeave;
    public boolean typeStrictLeave;

    public FF0 description;

    // Extrenely special behavior for certain commands.
    // Note that groupBehaviors should try to be as stable as possible, i.e. not corrupt stuff.
    public LinkedList<IGroupBehavior> groupBehaviors = new LinkedList<IGroupBehavior>();
    // Used when this command is created.
    public int[] template = new int[0];
    public int category;

    // Find Translatables, etc.
    public final HashSet<CommandTag> tags = new HashSet<>();
    public boolean commandSiteAllowed = true;

    // For copy all text
    public int textArg = -1;

    public RPGCommand(App app, int objId, DatumSrcLoc srcLoc, String dbId, String nru) {
        super(app);
        commandId = objId;
        this.srcLoc = srcLoc;
        this.dbId = dbId;
        nameRawUnlocalized = nru;
    }

    public void finish() {
        // Names use NDB syntax, thus, separate context
        name = app.dTrFF1(srcLoc, TrNames.cmdbName(dbId, commandId), DatumLoader.readInlineList(srcLoc, nameRawUnlocalized));
    }

    // Pass null for parameters if this is for combobox display.
    public String formatName(@Nullable IRIO paramsObj) {
        return name.r(paramsObj);
    }

    public SchemaElement getParameterSchema(IRIO paramsObj, int i) {
        if (params.size() <= i)
            return app.sdb.getSDBEntry("genericScriptParameter");
        return params.get(i).getSchema(paramsObj);
    }

    /**
     * Returns the contextual name of the given parameter index.
     * This is nullable - if null, the parameter is hidden.
     */
    public @Nullable String getParameterName(IRIO paramsObj, int i) {
        if (params.size() <= i)
            return T.s.cmdb_unkParamName;
        return params.get(i).getName(paramsObj);
    }

    public boolean isAnchor(IRIO paramsObj) {
        return (indentPre + (indentPost.apply(paramsObj))) > 0;
    }

    public boolean isAnchorVis(IRIO paramsObj) {
        return indentPre != 0 || (indentPost.apply(paramsObj) != 0);
    }

    public static abstract class SpecialTag {
        public abstract void applyTo(int idx, LinkedList<UIElement> elementList, IRIO targetParamArray, ISchemaHost launcher, SchemaPath path);
    }

    public static class SpritesheetSpecialTag extends SpecialTag {
        public String spritesheetId;
        public int spritesheetTargstr;

        public SpritesheetSpecialTag(int str, String id) {
            spritesheetId = id;
            spritesheetTargstr = str;
        }

        @Override
        public void applyTo(int idx, LinkedList<UIElement> elementList, IRIO targetParamArray, ISchemaHost launcher, SchemaPath path) {
            App app = launcher.getApp();
            SchemaElement scse = app.sdbHelpers.makeSpriteSelector(PathSyntax.compile(app, "]" + idx), PathSyntax.compile(app, "]" + spritesheetTargstr), spritesheetId);
            elementList.add(scse.buildHoldingEditor(targetParamArray, launcher, path));
        }
    }

    public static class TonepickerSpecialTag extends SpecialTag {
        public int tpBase;
        public int tpA, tpB, tpC, tpD;

        public TonepickerSpecialTag(int base, int a, int b, int c, int d) {
            tpBase = base;
            tpA = a;
            tpB = b;
            tpC = c;
            tpD = d;
        }

        @Override
        public void applyTo(int idx, LinkedList<UIElement> elementList, IRIO targetParamArray, ISchemaHost launcher, SchemaPath path) {
            App app = launcher.getApp();
            SchemaElement scse = new TonePickerSchemaElement(launcher.getApp(), PathSyntax.compile(app, "]" + tpA), PathSyntax.compile(app, "]" + tpB), PathSyntax.compile(app, "]" + tpC), PathSyntax.compile(app, "]" + tpD), tpBase);
            elementList.add(scse.buildHoldingEditor(targetParamArray, launcher, path));
        }
    }

    public static class XPMoveCommandSetGraphicSpecialTag extends SpecialTag {
        public XPMoveCommandSetGraphicSpecialTag() {
        }

        @Override
        public void applyTo(int idx, LinkedList<UIElement> elementList, IRIO targetParamArray, ISchemaHost launcher, SchemaPath path) {
            String cName;
            int hue, dir, pat;
            try {
                cName = targetParamArray.getAElem(0).decString();
                hue = (int) targetParamArray.getAElem(1).getFX();
                dir = (int) targetParamArray.getAElem(2).getFX();
                pat = (int) targetParamArray.getAElem(3).getFX();
            } catch (Exception ex) {
                // oops
                ex.printStackTrace();
                return;
            }
            IRIOGeneric ig = new IRIOGeneric(launcher.getApp().ctxDisposableUTF8Encoding);
            ig.addIVar("@character_name").setString(cName);
            ig.addIVar("@character_hue").setFX(hue);
            ig.addIVar("@direction").setFX(dir);
            ig.addIVar("@pattern").setFX(pat);
            elementList.add(EPGDisplaySchemaElement.buildEditorFromObject(launcher.getApp(), launcher.getContext().getRenderer(), ig));
        }
    }

    public static abstract class Param {
        /**
         * Gets the contextual name of the parameter.
         * Can return null, making the parameter invisible.
         */
        public abstract @Nullable String getName(RORIO paramsObj);
        public abstract @NonNull SchemaElement getSchema(RORIO paramsObj);
    }

    public static class PStatic extends Param {
        public final @Nullable FF0 name;
        public final @NonNull SchemaElement se;

        public PStatic(@Nullable FF0 n, @NonNull SchemaElement e) {
            name = n;
            se = e;
        }

        @Override
        public @Nullable String getName(RORIO paramsObj) {
            if (name == null)
                return null;
            return name.r();
        }
        
        @Override
        public @NonNull SchemaElement getSchema(RORIO paramsObj) {
            return se;
        }
    }

    public static class PDyn extends Param {
        public final @NonNull Param def;
        public final HashMap<Integer, Param> contents = new HashMap<>();
        public final int arrayDI;

        public PDyn(Param base, int di) {
            def = base;
            arrayDI = di;
        }

        private Param getParam(RORIO paramsObj) {
            if (paramsObj != null && paramsObj.getType() == '[' && paramsObj.getALen() > arrayDI) {
                int p = (int) paramsObj.getAElem(arrayDI).getFX();
                Param ise = contents.get(p);
                if (ise != null)
                    return ise;
            }
            return def;
        }

        @Override
        public @Nullable String getName(RORIO paramsObj) {
            return getParam(paramsObj).getName(paramsObj);
        }

        @Override
        public @NonNull SchemaElement getSchema(RORIO paramsObj) {
            return getParam(paramsObj).getSchema(paramsObj);
        }
    }
}
