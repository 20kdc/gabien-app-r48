/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import static datum.DatumTreeUtils.decVisitor;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import datum.DatumExpectListVisitor;
import datum.DatumKVDHVisitor;
import datum.DatumSymbol;
import datum.DatumVisitor;
import r48.minivm.MVMU;

/**
 * Engine definition. Used to restrain MiniVM's potential arbitrary-write capabilities somewhat.
 * This isn't meant to be a security guarantee, just careful API design.
 * Created 10th March 2023.
 */
public class EngineDef {
    private static final HashMap<String, DatumKVDHVisitor.Handler<EngineDef, Object>> map = new HashMap<>();
    static {
        map.put("initDir", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.initDir = (String) v;
            });
        });
        map.put("odbBackend", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.odbBackend = ((DatumSymbol) v).id;
            });
        });
        map.put("dataPath", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.dataPath = (String) v;
            });
        });
        map.put("dataExt", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.dataExt = (String) v;
            });
        });
        map.put("mapSystem", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.mapSystem = ((DatumSymbol) v).id;
            });
        });
        map.put("autoDetectPath", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.autoDetectPath = (String) v;
            });
        });
        map.put("definesObjects", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                List<Object> obj = MVMU.cList(v);
                c.definesObjects = obj.toArray(new String[0]);
            });
        });
        map.put("mkdirs", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                List<Object> obj = MVMU.cList(v);
                c.mkdirs = obj.toArray(new String[0]);
            });
        });
        // SDB flags
        map.put("defineIndent", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.defineIndent = (Boolean) v;
            });
        });
        map.put("allowIndentControl", (k, c, gt) -> {
            return decVisitor((v, srcLoc) -> {
                c.allowIndentControl = (Boolean) v;
            });
        });
    }

    public @NonNull String initDir = "";
    public @NonNull String odbBackend = "<you forgot to select a backend>";
    public @NonNull String dataPath = "";
    public @NonNull String dataExt = "";
    public @NonNull String mapSystem = "null";
    public @Nullable String autoDetectPath = null;
    public @NonNull String[] definesObjects = new String[0];
    public @NonNull String[] mkdirs = new String[0];
    // SDB flags
    public boolean defineIndent, allowIndentControl;

    public DatumVisitor newVisitor() {
        return new DatumExpectListVisitor(() -> new DatumKVDHVisitor<EngineDef, Object>(map, this, null));
    }
}
