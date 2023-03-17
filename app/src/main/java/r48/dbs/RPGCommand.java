/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.uslx.append.*;
import gabien.ui.UIScrollLayout;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;
import r48.schema.displays.TonePickerSchemaElement;
import r48.schema.specialized.cmgb.IGroupBehavior;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.tr.TrPage.FF0;
import r48.tr.TrPage.FF2;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * RPGCommand database entry.
 * Created on 12/30/16.
 */
public class RPGCommand extends App.Svc {
    public final int commandId;
    public FF2 name;

    public SchemaElement specialSchema;

    // As the entire paramType cannot be replaced (paramType is relied upon for parameter names), this instead allows supplementing it.
    public LinkedList<SpecialTag> paramSpecialTags = new LinkedList<SpecialTag>();
    public LinkedList<Param> params = new LinkedList<Param>();
    public int indentPre;
    /**
     * This is conditional solely because of Show Inn (R2k).
     * Importantly, this accepts the parameters object, like all roots here. There have been some callers disrespecting this.
     */
    public IFunction<IRIO, Integer> indentPost = (params) -> 0;
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

    // For Find Translatables functionality
    public boolean isTranslatable;

    // For copy all text
    public int textArg = -1;

    public RPGCommand(App app, int objId) {
        super(app);
        commandId = objId;
    }

    // Pass null for parameters if this is for combobox display.
    @SuppressWarnings("unchecked")
    public String formatName(@Nullable IRIO paramsObj) {
        IFunction<RORIO, SchemaElement>[] paramTypes = new IFunction[params.size()];
        int idx = 0;
        for (Param p : params)
            paramTypes[idx++] = p.schema;
        return name.r(paramsObj, paramTypes);
    }

    public SchemaElement getParameterSchema(IRIO paramsObj, int i) {
        if (params.size() <= i)
            return app.sdb.getSDBEntry("genericScriptParameter");
        return params.get(i).schema.apply(paramsObj);
    }

    /**
     * Returns the contextual name of the given parameter index.
     * This is nullable - if null, the parameter is hidden.
     */
    public @Nullable String getParameterName(IRIO paramsObj, int i) {
        if (params.size() <= i)
            return T.s.cmdb_unkParamName;
        return params.get(i).name.apply(paramsObj);
    }

    public boolean isAnchor(IRIO paramsObj) {
        return (indentPre + (indentPost.apply(paramsObj))) > 0;
    }

    public boolean isAnchorVis(IRIO paramsObj) {
        return indentPre != 0 || (indentPost.apply(paramsObj) != 0);
    }

    public static class SpecialTag {
        public boolean hasTonepicker;
        public int tpA, tpB, tpC, tpD;
        public boolean hasSpritesheet;
        public String spritesheetId;
        public int spritesheetTargstr;
        public int tpBase;

        public void applyTo(int idx, UIScrollLayout elementList, IRIO targetParamArray, ISchemaHost launcher, SchemaPath path) {
            App app = launcher.getApp();
            if (hasSpritesheet) {
                SchemaElement scse = app.sdb.helpers.makeSpriteSelector(PathSyntax.compile(app, "]" + idx), PathSyntax.compile(app, "]" + spritesheetTargstr), spritesheetId);
                elementList.panelsAdd(scse.buildHoldingEditor(targetParamArray, launcher, path));
            }
            if (hasTonepicker) {
                SchemaElement scse = new TonePickerSchemaElement(launcher.getApp(), PathSyntax.compile(app, "]" + tpA), PathSyntax.compile(app, "]" + tpB), PathSyntax.compile(app, "]" + tpC), PathSyntax.compile(app, "]" + tpD), tpBase);
                elementList.panelsAdd(scse.buildHoldingEditor(targetParamArray, launcher, path));
            }
        }
    }

    public static class Param {
        /**
         * This can return null, which makes the parameter invisible.
         */
        public final @NonNull IFunction<RORIO, String> name;
        public final @NonNull IFunction<RORIO, SchemaElement> schema;
        public Param(@NonNull IFunction<RORIO, String> n, @NonNull IFunction<RORIO, SchemaElement> s) {
            name = n;
            schema = s;
        }
    }
}
