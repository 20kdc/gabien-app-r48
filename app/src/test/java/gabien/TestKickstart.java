/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabien;

import gabien.render.IImage;
import gabien.uslx.vfs.impl.AttachedFSBackend;
import gabien.uslx.vfs.impl.JavaIOFSBackend;
import gabien.uslx.vfs.impl.RAMFSBackend;
import gabien.uslx.vfs.impl.RAMFSBackend.VFSDir;
import gabien.uslx.vfs.impl.UnionFSBackend;
import gabien.wsi.IGrInDriver;
import gabien.wsi.IPointer;
import gabien.wsi.ITextEditingSession;
import gabien.wsi.WindowSpecs;
import r48.App;
import r48.app.AppMain;
import r48.app.EngineDef;
import r48.app.EnginesList;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;
import r48.dbs.ObjectDB;
import r48.io.IObjectBackend;
import r48.tests.grand.GrandExecutionError;
import r48.ui.Art;
import r48.wm.GrandWindowManagerUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Ties into gabien-javase to create a test workbench.
 * Unusable outside of it.
 * Created on November 19, 2018.
 */
public class TestKickstart {
    public String currentTestPhase;
    public LinkedList<Supplier<Boolean>> waitingTestEntries = new LinkedList<Supplier<Boolean>>();
    public Consumer<String> waitingFileDialog = null;

    public RAMFSBackend.VFSDir mockVFS;

    public LinkedList<TestGrInDriver> windows = new LinkedList<TestGrInDriver>();
    public MobilePeripherals.DummyPointer pointer;
    public String maintainText = null;
    public boolean maintainTextEnter = false;
    public int windowCount = 1337;

    public GrandWindowManagerUtils gwmu;

    public App kickstart(final String s2, final String encoding, final String engineDefId) {
        currentTestPhase = "Initial Phase";
        kickstartRFS();
        // In case unset.
        Charset charset;
        try {
            charset = Charset.forName(encoding);
        } catch (UnsupportedCharsetException uce) {
            throw new RuntimeException(uce);
        }
        Art a = new Art();
        Config c = new Config(false);
        c.applyUIGlobals();
        InterlaunchGlobals ilg = new InterlaunchGlobals(a, c, (vm) -> {}, (str) -> {}, (str) -> {
            // this is to catch any SDB tr conflicts
            throw new RuntimeException("TR issue during tests: " + str);
        }, true);
        EngineDef engine = EnginesList.getEngines(null).get(engineDefId);
        if (engine == null)
            throw new RuntimeException("missing engine def: " + engineDefId);
        return AppMain.initializeCore(ilg, charset, GaBIEn.mutableDataFS.intoPath(s2), null, engine, (s) -> {});
    }

    public void kickstartRFS() {
        GaBIEn.fontsReady = true;
        GaBIEnImpl.mobileEmulation = true;
        GaBIEnImpl.fontsAlwaysMeasure16 = true;
        windowCount = 0;
        final GaBIEnImpl impl = new GaBIEnImpl() {

            @Override
            public String[] getFontOverrides() {
                return new String[] {
                    GaBIEnImpl.getDefaultFont()
                };
            }

            @Override
            public void ensureQuit() {

            }

            @Override
            public IGrInDriver makeGrIn(String name, int w, int h, WindowSpecs ws) {
                ws.resizable = false;
                w = 960;
                h = 540;
                return new TestGrInDriver(name, ws, w, h);
            }

            @Override
            public void startFileBrowser(String text, boolean saving, String exts, Consumer<String> result, String initialName) {
                waitingFileDialog = result;
            }
        };
        GaBIEn.internal = impl;
        RAMFSBackend ram = new RAMFSBackend();
        ram.vfsRoot.contents.put("RAM", new VFSDir());
        mockVFS = ram.vfsRoot;
        GaBIEn.mutableDataFS = new UnionFSBackend(ram, new AttachedFSBackend(JavaIOFSBackend.ROOT, "real_fs", true));
        GaBIEn.internalFileBrowser = impl;
        GaBIEn.internalWindowing = impl;
        GaBIEn.setupNativesAndAssets(true, false);
        GaBIEnUI.setupAssets();
        // Reset GaBIEn stuff
        new Config(false).applyUIGlobals();
    }

    public void resetODB(App app) {
        IObjectBackend backend = IObjectBackend.Factory.create(app.gameRoot, app.engine.odbBackend, app.engine.dataPath, app.engine.dataExt);
        app.odb = new ObjectDB(app, backend);
    }

    public class TestGrInDriver extends gabien.GrInDriver {
        private String internalMaintainText;
        private boolean didMaintainThisFrame;

        public TestGrInDriver(String name, WindowSpecs ws, int w, int h) {
            super(name, ws, w, h);
            windowCount++;
            peripherals = new IGJSEPeripheralsInternal() {
                // Suppressed warnings because we might end up needing these
                @SuppressWarnings("unused")
                private int ofsX, ofsY;

                @Override
                public void performOffset(int x, int y) {
                    ofsX += x;
                    ofsY += y;
                }

                @Override
                public void clearOffset() {
                    ofsX = 0;
                    ofsY = 0;
                }

                @Override
                public HashSet<IPointer> getActivePointers() {
                    HashSet<IPointer> hs = new HashSet<IPointer>();
                    if (pointer != null)
                        hs.add(pointer);
                    return hs;
                }

                @Override
                public void clearKeys() {

                }

                @Override
                public ITextEditingSession openTextEditingSession(@NonNull String text, boolean multiLine, int textHeight, @Nullable Function<String, String> feedback) {
                    return new ITextEditingSession() {
                        boolean sessionDeadYet = false;
                        @Override
                        public String maintain(int x, int y, int w, int h) {
                            didMaintainThisFrame = true;
                            return maintainText;
                        }

                        @Override
                        public void setText(String text) {
                            System.out.println("Maintain set= " + text);
                            internalMaintainText = text;
                            maintainText = text;
                        }

                        @Override
                        public boolean isEnterJustPressed() {
                            return maintainTextEnter;
                        }

                        @Override
                        public void endSession() {
                            sessionDeadYet = true;
                        }

                        @Override
                        public boolean isSessionDead() {
                            return sessionDeadYet;
                        }
                    };
                }

                @Override
                public String aroundTheBorderworldMaintain(TextboxMaintainer tm, int x, int y, int w, int h) {
                    return "THIS SHOULD NOT BE CALLED";
                }

                @Override
                public void finishRemovingEditingSession() {
                    // nuh UH!
                }
            };
            windows.add(this);
        }

        @Override
        public void flush(IImage backBuffer) {
            try {
                FileOutputStream debugOut = new FileOutputStream("test-out/debug.png");
                debugOut.write(backBuffer.createPNG());
                debugOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (internalMaintainText != null) {
                if (!didMaintainThisFrame) {
                    System.out.println("Maintain stop");
                    internalMaintainText = null;
                    maintainText = null;
                }
            }
            didMaintainThisFrame = false;
            maintainTextEnter = false;
            super.flush(backBuffer);
            while (true) {
                // An entry returns false (which waits a frame) until it's done, then it returns true
                if (waitingTestEntries.size() == 0) {
                    gwmu.printTree();
                    throw new GrandExecutionError("Ran out of test sequence data.");
                }
                Supplier<Boolean> isb = waitingTestEntries.getFirst();
                if (!isb.get())
                    break;
                waitingTestEntries.removeFirst();
            }
        }
    }
}
