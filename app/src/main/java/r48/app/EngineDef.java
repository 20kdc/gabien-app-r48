/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumExpectListVisitor;
import gabien.datum.DatumKVDHVisitor;
import gabien.datum.DatumODec1Visitor;
import gabien.datum.DatumSymbol;
import gabien.datum.DatumVisitor;

/**
 * Engine definition. Used to restrain MiniVM's potential arbitrary-write capabilities somewhat.
 * This isn't meant to be a security guarantee, just careful API design.
 * Created 10th March 2023.
 */
public class EngineDef {
    private static final HashMap<String, DatumKVDHVisitor.Handler<EngineDef, Object>> map = new HashMap<>();
    static {
        map.put("initDir", (k, c, gt) -> {
            return new DatumODec1Visitor<>(null, null, (v, ctx) -> {
                c.initDir = (String) v;
            }, null);
        });
        map.put("odbBackend", (k, c, gt) -> {
            return new DatumODec1Visitor<>(null, null, (v, ctx) -> {
                c.odbBackend = ((DatumSymbol) v).id;
            }, null);
        });
        map.put("dataPath", (k, c, gt) -> {
            return new DatumODec1Visitor<>(null, null, (v, ctx) -> {
                c.dataPath = (String) v;
            }, null);
        });
        map.put("dataExt", (k, c, gt) -> {
            return new DatumODec1Visitor<>(null, null, (v, ctx) -> {
                c.dataExt = (String) v;
            }, null);
        });
        map.put("mapSystem", (k, c, gt) -> {
            return new DatumODec1Visitor<>(null, null, (v, ctx) -> {
                c.mapSystem = ((DatumSymbol) v).id;
            }, null);
        });
        map.put("autoDetectPath", (k, c, gt) -> {
            return new DatumODec1Visitor<>(null, null, (v, ctx) -> {
                c.autoDetectPath = (String) v;
            }, null);
        });
    }

    public @NonNull String initDir = "";
    public @NonNull String odbBackend = "<you forgot to select a backend>";
    public @NonNull String dataPath = "";
    public @NonNull String dataExt = "";
    public @NonNull String mapSystem = "null";
    public @Nullable String autoDetectPath = null;

    public DatumVisitor newVisitor() {
        return new DatumExpectListVisitor(() -> new DatumKVDHVisitor<EngineDef, Object>(map, this, null));
    }
}
