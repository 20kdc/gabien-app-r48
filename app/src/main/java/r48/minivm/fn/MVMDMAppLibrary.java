/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumSymbol;
import r48.R48;
import r48.dbs.ObjectInfo;
import r48.dbs.ObjectRootHandle;
import r48.io.data.DMContext;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;
import r48.io.undoredo.DMChangeTracker;
import r48.minivm.MVMEnv;
import r48.minivm.MVMEnvR48;
import r48.minivm.MVMType;
import r48.minivm.MVMU;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * MiniVM standard library.
 * Created 8th March 2023.
 */
public class MVMDMAppLibrary {
    public static void add(MVMEnv ctx, R48 app) {
        ctx.defineSlot(new DatumSymbol("dm-fmt"), new DMFmt(app))
            .help("(dm-fmt TARGET [NAME/#nil [PREFIXENUMS]]) : Passes to FormatSyntax.interpretParameter. If the passed-in object is null (say, due to a PathSyntax failure) returns the empty string. Important: Because of schemas and stuff this doesn't exist in the static translation context. PREFIXENUMS can be #f, #t or #nil (default).");

        ctx.defLib("root-new", MVMEnvR48.ROOT_TYPE, MVMType.ANY, MVMType.ANY, (elm, name) -> {
            SchemaElement se = MVMSDBLibrary.coerceToElement(app, elm);
            IRIOGeneric ig = new IRIOGeneric(new DMContext(DMChangeTracker.Null.DISPOSABLE, StandardCharsets.UTF_8));
            SchemaPath.setDefaultValue(ig, se, null);
            return new ObjectRootHandle.Isolated(se, ig, MVMU.coerceToString(name));
        }, "(root-new SCHEMA NAME): Creates an independent root from a schema element and a name.");

        ctx.defLib("root-modify", MVMType.ANY, MVMType.ANY, MVMType.Fn.simple(MVMType.ANY, MVMEnvR48.IRIO_TYPE), (text, fn) -> {
            MVMFn fn2 = (MVMFn) fn;
            ObjectRootHandle lo = assertObjectRoot(app, text, true);
            if (lo == null)
                throw new RuntimeException("Root lookup failed: " + text);
            SchemaPath sp = new SchemaPath(app.sdb.getSDBEntry("OPAQUE"), lo);
            Object res = fn2.callDirect(sp.targetElement);
            sp.changeOccurred(false);
            return res;
        }, "(root-modify OID RECEIVER): Opens an object to modify. OID must be a root or (odb-get OID #t) is implicitly run. Modification will be signalled at end of handler; please don't modify outside of that. Throws on failure.");
        ctx.defLib("root-read", MVMEnvR48.RORIO_TYPE, MVMType.ANY, (text) -> {
            ObjectRootHandle lo = assertObjectRoot(app, text, false);
            if (lo == null)
                return null;
            return lo.getObject();
        }, "(root-read OID): Gets an object's root RORIO. OID must be a root or (odb-get OID #f) is implicitly run. Returns #nil on missing objects; otherwise throws on failure.");
        ctx.defLib("odb-get", MVMEnvR48.ROOT_TYPE, MVMType.ANY, MVMType.BOOL, (text, create) -> {
            return assertObjectRoot(app, text, (Boolean) create);
        }, "(odb-get OID CREATE): Gets an object root handle. OID must either be a root (passthrough), or a string. If a string, object MUST be registered in object infos table or an error will be thrown. Returns #nil if the object doesn't exist, can't be created, but 'should' exist.");
    }

    public static @Nullable ObjectRootHandle assertObjectRoot(R48 app, Object input, boolean create) {
        if (input instanceof ObjectRootHandle)
            return (ObjectRootHandle) input;
        return assertObjectInfo(app, input).getILO(create);
    }

    public static @NonNull ObjectInfo assertObjectInfo(R48 app, Object text) {
        ObjectInfo oi = app.getObjectInfo((String) text);
        if (oi == null)
            throw new RuntimeException("MVM is not allowed to access undefined object info: " + text);
        return oi;
    }

    public static final class DMFmt extends MVMFn.Fixed {
        public final R48 app;
        public DMFmt(R48 app) {
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
