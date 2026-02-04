/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.cli;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.function.Consumer;

import gabien.GaBIEn;
import gabien.uslx.vfs.FSBackend;
import r48.R48;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;
import r48.gameinfo.EngineDef;
import r48.ui.Art;

/**
 * Created 4th February, 2026
 */
public class CLIAppParams {
    public String engine = "";
    public String charset = "UTF-8";
    public String root = ".";
    public String secondary = "";

    public void contribute(HashMap<String, Consumer<String>> parameters) {
        parameters.put("--engine", v -> engine = v);
        parameters.put("--charset", v -> charset = v);
        parameters.put("--root", v -> root = v);
        parameters.put("--secondary", v -> secondary = v);
    }

    public R48 bootApp() {
        final Charset charset;
        try {
            charset = Charset.forName(this.charset);
        } catch (UnsupportedCharsetException uce) {
            throw new RuntimeException(uce);
        }
        final FSBackend rootPath = GaBIEn.mutableDataFS.intoPath(root);
        final FSBackend silPath = secondary.equals("") ? null : GaBIEn.mutableDataFS.intoPath(secondary);
        InterlaunchGlobals ilg = new InterlaunchGlobals(new Art(), new Config(false), report -> {}, progress -> {}, issue -> {}, false);
        EngineDef engine = ilg.getEngineDef(this.engine);
        if (engine == null)
            throw new RuntimeException("EngineDef " + this.engine + " missing!");
        return new R48(ilg, charset, engine, rootPath, silPath, progress -> {});
    }
}
