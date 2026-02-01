/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.gameinfo;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.datum.DatumStruct;
import gabien.datum.DatumStructField;

/**
 * Engine definition. Used to restrain MiniVM's potential arbitrary-write capabilities somewhat.
 * This isn't meant to be a security guarantee, just careful API design.
 * Created 10th March 2023.
 */
public class EngineDef extends DatumStruct<Object> {
    @DatumStructField("initDir")
    public @NonNull String initDir = "";
    @DatumStructField("odbBackend")
    public @NonNull String odbBackend = "<you forgot to select a backend>";
    @DatumStructField("dataPath")
    public @NonNull String dataPath = "";
    @DatumStructField("dataExt")
    public @NonNull String dataExt = "";
    @DatumStructField("mapSystem")
    public @NonNull String mapSystem = "null";
    @DatumStructField("autoDetectPath")
    public @Nullable String autoDetectPath = null;
    @DatumStructField("definesObjects")
    public @NonNull String[] definesObjects = new String[0];
    @DatumStructField("mkdirs")
    public @NonNull String[] mkdirs = new String[0];
    // SDB flags
    @DatumStructField("defineIndent")
    public boolean defineIndent;
    @DatumStructField("allowIndentControl")
    public boolean allowIndentControl;
}
