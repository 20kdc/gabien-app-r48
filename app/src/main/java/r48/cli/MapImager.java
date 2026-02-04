/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

import gabien.GaBIEn;
import gabien.render.IGrDriver;
import gabien.uslx.append.CLIParse;
import gabien.uslx.vfs.FSBackend;
import r48.R48;
import r48.io.data.DMKey;
import r48.map.systems.IRMMapSystem;
import r48.map.systems.MapSystem.MapViewDetails;
import r48.map.systems.MapSystem.MapViewState;

/**
 * Dummy for now
 * Created 2nd February, 2026
 */
public class MapImager {
    private static String out = "";
    public static void main(String[] args) throws IOException {
        GaBIEn.initializeEmbedded();
        CLIAppParams cap = new CLIAppParams();
        HashMap<String, Consumer<String>> parameters = new HashMap<>();
        parameters.put("--outDir", v -> out = v);
        parameters.put("-o", v -> out = v);
        cap.contribute(parameters);
        String[] maps = CLIParse.cliParse("MapImager", args, null, parameters);

        // Proceed
        R48 app = cap.bootApp();
        FSBackend outDir = GaBIEn.mutableDataFS.intoRelPath(out);
        outDir.mkdirs();

        HashSet<String> gums = new HashSet<>();

        if (maps.length == 0) {
            // TODO: At least some of this should be in MapSystem core somehow.
            IRMMapSystem mapSystem = (IRMMapSystem) app.system;
            for (IRMMapSystem.RMMapData data : mapSystem.getAllMaps()) {
                String gum = app.system.mapReferentToGUM(DMKey.of(data.id));
                if (gum != null)
                    gums.add(gum);
            }
        } else {
            // TODO this is hacky and bad and no-good and bad
            // 'the prophecy said you would bring balance to the GUMs, not join them!'
            for (String s : maps) {
                gums.add(s);
            }
        }
        for (String s : gums) {
            System.out.println(s);
            MapViewDetails mvd = app.system.mapViewRequest(s, false);
            if (mvd == null)
                continue;
            MapViewState target = mvd.rebuild();
            IGrDriver image = target.renderMapShot(target.activeDef, 0, false);
            byte[] pngData = image.createPNG();
            try (OutputStream os = outDir.into(s + ".png").openWrite()) {
                os.write(pngData);
            }
            image.shutdown();
        }
        GaBIEn.ensureQuit();
    }
}
