/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ui.dialog;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import gabien.GaBIEn;
import gabien.ui.UIScrollLayout;
import gabien.ui.UISplitterLayout;
import gabien.ui.UITextButton;
import r48.App;
import r48.FontSizes;
import r48.RubyIO;
import r48.app.AppMain;
import r48.dbs.CMDB;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.map.mapinfos.RXPRMLikeMapInfoBackend;
import r48.map.systems.IRMMapSystem;
import r48.toolsets.utils.RMTranscriptDumper;
import r48.ui.UISetSelector;

/**
 * Created 23rd October 2022.
 */
public class UITranscriptControl extends App.Prx {
    private UIScrollLayout layout = new UIScrollLayout(true, FontSizes.generalScrollersize);
    private boolean done = false;

    private UISetSelector<TranscriptComponent> setSelector;
    private final LinkedList<TranscriptComponent> components = new LinkedList<TranscriptComponent>();

    private final IRMMapSystem mapSystem;
    private final CMDB commandsEvent;

    public UITranscriptControl(App app, IRMMapSystem ms, CMDB ce) {
        super(app);
        mapSystem = ms;
        commandsEvent = ce;

        components.add(new TCCEv());
        // Order the maps so that it comes out coherently for valid diffs (OSER Solstice Comparison Project)
        LinkedList<Integer> orderedMapInfos = new LinkedList<Integer>();
        HashMap<Integer, IRMMapSystem.RMMapData> mapMap = new HashMap<Integer, IRMMapSystem.RMMapData>();
        for (IRMMapSystem.RMMapData rmd : mapSystem.getAllMaps()) {
            orderedMapInfos.add(rmd.id);
            mapMap.put(rmd.id, rmd);
        }
        Collections.sort(orderedMapInfos);
        for (int id : orderedMapInfos)
            components.add(new TCMap(mapMap.get(id)));
        components.add(new TCCustomData());

        setSelector = new UISetSelector<TranscriptComponent>(components);

        refreshContents();
        
        proxySetElement(new UISplitterLayout(layout, setSelector, false, 0.5), true);
    }

    @Override
    public boolean requestsUnparenting() {
        return done; 
    }

    private void refreshContents() {
        layout.panelsClear();

        layout.panelsAdd(new UITextButton(TXDB.get("Confirm"), FontSizes.dialogWindowTextHeight, new Runnable() {
            @Override
            public void run() {
                PrintStream ps = null;
                try {
                    ps = new PrintStream(GaBIEn.getOutFile(app.rootPath + "transcript.html"), false, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                RMTranscriptDumper dumper = new RMTranscriptDumper(ps);

                Set<TranscriptComponent> comps = setSelector.getSet();
                for (TranscriptComponent tc : components)
                    if (comps.contains(tc))
                        tc.dump(dumper);;

                // Prevent breakage
                app.sdb.updateDictionaries(null);
                app.sdb.kickAllDictionariesForMapChange();

                dumper.end();
                ps.close();
                app.ui.launchDialog(TXDB.get("transcript.html was written to the target's folder."));
                done = true;
            }
        }));
    }

    public abstract class TranscriptComponent {
        public abstract void dump(RMTranscriptDumper dumper);
    }

    public class TCCEv extends TranscriptComponent {
        public TCCEv() {
        }

        @Override
        public void dump(RMTranscriptDumper dumper) {
            dumper.start();
            dumper.startFile("CommonEvents", TXDB.get("Common Events"));
            for (IRIO rio : mapSystem.getAllCommonEvents())
                dumper.dump(rio.getIVar("@name").decString(), rio.getIVar("@list"), commandsEvent);
            dumper.endFile();
        }
        @Override
        public String toString() {
            return TXDB.get("Common Events");
        }
    }

    public class TCMap extends TranscriptComponent {
        public final IRMMapSystem.RMMapData rmd;
        public final String whatDoWeCallThis;

        public TCMap(IRMMapSystem.RMMapData rm) {
            rmd = rm;
            whatDoWeCallThis = app.fmt.formatExtended(TXDB.get("Map:#A"), new RubyIO().setString(rmd.getName(), true));
        }

        @Override
        public void dump(RMTranscriptDumper dumper) {
            IObjectBackend.ILoadedObject map = rmd.getILO(false);
            if (map == null)
                return;
            dumper.startFile(RXPRMLikeMapInfoBackend.sNameFromInt(rmd.id), whatDoWeCallThis);
            // We need to temporarily override map context.
            // This'll fix itself by next frame...
            app.sdb.updateDictionaries(map);
            app.sdb.kickAllDictionariesForMapChange();
            LinkedList<Integer> orderedEVN = new LinkedList<Integer>();
            for (IRIO i : map.getObject().getIVar("@events").getHashKeys())
                orderedEVN.add((int) i.getFX());
            Collections.sort(orderedEVN);
            for (int k : orderedEVN) {
                IRIO event = map.getObject().getIVar("@events").getHashVal(new RubyIO().setFX(k));
                int pageId = 1;
                IRIO pages = event.getIVar("@pages");
                int alen = pages.getALen();
                for (int i = 0; i < alen; i++) {
                    IRIO page = pages.getAElem(i);
                    if (page.getType() == '0')
                        continue; // 0th page on R2k backend.
                    dumper.dump(app.fmt.formatExtended(TXDB.get("Ev.#A #C, page #B"), new RubyIO().setFX(k), new RubyIO().setFX(pageId), event.getIVar("@name")), page.getIVar("@list"), commandsEvent);
                    pageId++;
                }
            }
            dumper.endFile();
        }
        @Override
        public String toString() {
            return whatDoWeCallThis;
        }
    }

    public class TCCustomData extends TranscriptComponent {
        @Override
        public void dump(RMTranscriptDumper dumper) {
            mapSystem.dumpCustomData(dumper);
        }
        @Override
        public String toString() {
            return TXDB.get("Context (variables, etc.)");
        }
    }
}
