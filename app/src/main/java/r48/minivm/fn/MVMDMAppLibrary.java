/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import org.eclipse.jdt.annotation.NonNull;

import datum.DatumSymbol;
import r48.App;
import r48.dbs.ObjectInfo;
import r48.dbs.ObjectRootHandle;
import r48.io.data.RORIO;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.schema.EnumSchemaElement;
import r48.schema.util.SchemaPath;

/**
 * MiniVM standard library.
 * Created 8th March 2023.
 */
public class MVMDMAppLibrary {
    public static void add(MVMEnv ctx, App app) {
        ctx.defineSlot(new DatumSymbol("dm-fmt"), new DMFmt(app))
            .help("(dm-fmt TARGET [NAME/#nil [PREFIXENUMS]]) : Passes to FormatSyntax.interpretParameter. If the passed-in object is null (say, due to a PathSyntax failure) returns the empty string. Important: Because of schemas and stuff this doesn't exist in the static translation context. PREFIXENUMS can be #f, #t or #nil (default).");

        ctx.defLib("odb-open-for-modify", MVMType.ANY, MVMType.STR, MVMType.Fn.simple(MVMType.ANY, MVMEnvR48.IRIO_TYPE), (text, fn) -> {
            MVMFn fn2 = (MVMFn) fn;
            ObjectInfo oi = assertObjectInfo(app, text);
            SchemaPath sp = oi.makePath(true);
            Object res = fn2.callDirect(sp.targetElement);
            sp.changeOccurred(false);
            return res;
        }, "(odb-open-for-modify OID RECEIVER): Opens an object to modify. Object MUST be registered in object infos table or an error will be thrown. Modification will be signalled at end of handler; please don't modify outside of that.");
        ctx.defLib("odb-get", MVMEnvR48.RORIO_TYPE, MVMType.STR, (text) -> {
            ObjectInfo oi = assertObjectInfo(app, text);
            ObjectRootHandle ilo = oi.getILO(false);
            if (ilo == null)
                return null;
            return ilo.getObject();
        }, "(odb-get OID): Gets an object's root RORIO. Object MUST be registered in object infos table or an error will be thrown.");
    }

    public static @NonNull ObjectInfo assertObjectInfo(App app, Object text) {
        ObjectInfo oi = app.getObjectInfo((String) text);
        if (oi == null)
            throw new RuntimeException("MVM is not allowed to access undefined object info: " + text);
        return oi;
    }

    public static final class DMFmt extends MVMFn.Fixed {
        public final App app;
        public DMFmt(App app) {
            super(new MVMType.Fn(MVMType.STR, 1, new MVMType[] {MVMEnvR48.RORIO_TYPE, MVMType.STR, MVMType.ANY}, null), "dm-fmt");
            this.app = app;
        }
        @Override
        public Object callDirect(Object a0) {
            if (a0 == null)
                return "";
            return app.format((RORIO) a0, (String) null, EnumSchemaElement.Prefix.Default);
        }

        @Override
        public Object callDirect(Object a0, Object a1) {
            if (a0 == null)
                return "";
            return app.format((RORIO) a0, (String) a1, EnumSchemaElement.Prefix.Default);
        }

        @Override
        public Object callDirect(Object a0, Object a1, Object a2) {
            if (a0 == null)
                return "";
            EnumSchemaElement.Prefix pfx = EnumSchemaElement.Prefix.Default;
            if (a2 != null)
                pfx = MVMU.isTruthy(a2) ? EnumSchemaElement.Prefix.Prefix : EnumSchemaElement.Prefix.NoPrefix;
            return app.format((RORIO) a0, (String) a1, pfx);
        }
    }
}
