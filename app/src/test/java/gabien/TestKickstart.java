/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabien;

import gabien.uslx.append.*;
import gabien.uslx.vfs.FSBackend;
import gabien.ui.IPointer;
import r48.App;
import r48.app.AppMain;
import r48.app.EngineDef;
import r48.app.EnginesList;
import r48.app.InterlaunchGlobals;
import r48.cfg.Config;
import r48.dbs.ObjectDB;
import r48.io.IObjectBackend;
import r48.tests.grand.GrandExecutionError;
import r48.wm.GrandWindowManagerUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Ties into gabien-javase to create a test workbench.
 * Unusable outside of it.
 * Created on November 19, 2018.
 */
public class TestKickstart {
    public String currentTestPhase;
    public LinkedList<ISupplier<Boolean>> waitingTestEntries = new LinkedList<ISupplier<Boolean>>();
    public IConsumer<String> waitingFileDialog = null;

    public HashMap<String, byte[]> mockFS = new HashMap<String, byte[]>();
    public HashSet<String> mockDFS = new HashSet<String>();


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
        Config c = new Config(false);
        c.applyUIGlobals();
        InterlaunchGlobals ilg = new InterlaunchGlobals(c, (vm) -> {}, (str) -> {}, (str) -> {
            // this is to catch any SDB tr conflicts
            throw new RuntimeException("TR issue during tests: " + str);
        });
        EngineDef engine = EnginesList.getEngines(null).get(engineDefId);
        if (engine == null)
            throw new RuntimeException("missing engine def: " + engineDefId);
        return AppMain.initializeCore(ilg, charset, s2, "", engine, (s) -> {});
    }

    public void kickstartRFS() {
        FontManager.fontsReady = true;
        GaBIEnImpl.mobileEmulation = true;
        GaBIEnImpl.fontsAlwaysMeasure16 = true;
        windowCount = 0;
        mockFS.clear();
        mockDFS.clear();
        final GaBIEnImpl impl = new GaBIEnImpl(true) {

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
            public void startFileBrowser(String text, boolean saving, String exts, IConsumer<String> result, String initialName) {
                waitingFileDialog = result;
            }
        };
        GaBIEn.internal = impl;
        GaBIEn.mutableDataFS = new FSBackend() {
            @Override
            public XState getState(String fileName) {
                String dirName = fileName;
                if (!dirName.endsWith("/"))
                    dirName += "/";
                if (mockDFS.contains(dirName)) {
                    File dirRepFile = new File(dirName);
                    LinkedList<String> out = new LinkedList<String>();
                    for (String v : mockFS.keySet())
                        listEntry(v, out, dirRepFile);
                    for (String v : mockDFS)
                        listEntry(v, out, dirRepFile);
                    return new DirectoryState(out.toArray(new String[0]));
                } else if (mockFS.containsKey(fileName)) {
                    return new FileState(mockFS.get(fileName).length);
                }
                return null;
            }

            @Override
            public String nameOf(String fileName) {
                return new File(fileName).getName();
            }

            @Override
            public String parentOf(String fileName) {
                return new File(fileName).getParent();
            }

            @Override
            public String absolutePathOf(String fileName) {
                return fileName;
            }

            private void listEntry(String st, LinkedList<String> str, File dirRepFile) {
                if (dirRepFile.equals(new File(st).getParentFile()))
                    str.add(nameOf(st));
            }

            @Override
            public InputStream openRead(String fileName) throws IOException {
                // System.out.println("openRead: " + fileName);
                byte[] data = mockFS.get(fileName);
                if (data == null) {
                    boolean relativeIntended = false;
                    if (fileName.startsWith("./")) {
                        fileName = fileName.substring(2);
                        relativeIntended = true;
                    }
                    InputStream inp = impl.getResource(fileName);
                    if (inp == null && !relativeIntended) {
                        try {
                            inp = new FileInputStream(fileName);
                        } catch (Exception ex) {
                            // oh well
                        }
                    }
                    return inp;
                }
                return new ByteArrayInputStream(data);
            }

            @Override
            public OutputStream openWrite(final String fileName) throws IOException {
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        mockFS.put(fileName, toByteArray());
                    }
                };
            }

            @Override
            public void changeTime(String fileName, long time) {
            }

            @Override
            public void delete(String fileName) {
                if (fileName.endsWith("/"))
                    throw new RuntimeException("Not valid");
                if (mockDFS.contains(fileName)) {
                    for (String st : new HashSet<String>(mockDFS))
                        if (st.startsWith(fileName + "/"))
                            mockDFS.remove(st);
                    mockDFS.remove(fileName);
                } else {
                    mockFS.remove(fileName);
                }
            }

            @Override
            public void mkdir(String fileName) {
                if (fileName.endsWith("/"))
                    throw new RuntimeException("Not valid");
                mockDFS.add(fileName);
            }
            
        };
        GaBIEn.internalFileBrowser = impl;
        GaBIEn.internalWindowing = impl;
        GaBIEn.setupNativesAndAssets();
        // Reset GaBIEn stuff
        new Config(false).applyUIGlobals();
    }

    public void resetODB(App app) {
        IObjectBackend backend = IObjectBackend.Factory.create(app.encoding, app.engine.odbBackend, app.rootPath, app.engine.dataPath, app.engine.dataExt);
        app.odb = new ObjectDB(app, backend, (s) -> {});
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
                public ITextEditingSession openTextEditingSession(@NonNull String text, boolean multiLine, int textHeight, @Nullable IFunction<String, String> feedback) {
                    return new ITextEditingSession() {
                        boolean sessionDeadYet = false;
                        @Override
                        public String maintain(int x, int y, int w, int h, String text) {
                            boolean settingNew = true;
                            if (internalMaintainText != null)
                                if (internalMaintainText.equals(text))
                                    settingNew = false;
                            if (settingNew) {
                                System.out.println("Maintain set= " + text);
                                internalMaintainText = text;
                                maintainText = text;
                            }
                            didMaintainThisFrame = true;
                            return maintainText;
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
                public String aroundTheBorderworldMaintain(TextboxMaintainer tm, int x, int y, int w, int h, String text) {
                    return text;
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
                ISupplier<Boolean> isb = waitingTestEntries.getFirst();
                if (!isb.get())
                    break;
                waitingTestEntries.removeFirst();
            }
        }
    }
}
