/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.util.HashMap;
import java.util.function.Consumer;

import datum.DatumKVDVisitor;
import datum.DatumVisitor;
import r48.dbs.DatumLoader;

/**
 * Engine definitions list.
 * Not quite setup the same way as the languages list, because EngineDef is too mutable
 * Created 10th March 2023.
 */
public class EnginesList {
    public static HashMap<String, EngineDef> getEngines(Consumer<String> loadProgress) {
        HashMap<String, EngineDef> hm = new HashMap<>();
        DatumKVDVisitor kvd = new DatumKVDVisitor() {
            @Override
            public DatumVisitor handle(String key) {
                EngineDef ed = new EngineDef();
                // System.out.println("adding engine " + key);
                hm.put(key, ed);
                return ed.newVisitor();
            }
        };
        DatumLoader.read("engines", loadProgress, kvd);
        DatumLoader.read("engines.aux", loadProgress, kvd);
        return hm;
    }
}
