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
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.ObjectDB;
import r48.io.IObjectBackend;
import r48.tests.grand.GrandExecutionError;
import r48.wm.GrandWindowManagerUtils;

import java.io.*;
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
    public static String currentTestPhase;
    public static LinkedList<ISupplier<Boolean>> waitingTestEntries = new LinkedList<ISupplier<Boolean>>();
    public static IConsumer<String> waitingFileDialog = null;

    public static HashMap<String, byte[]> mockFS = new HashMap<String, byte[]>();
    public static HashSet<String> mockDFS = new HashSet<String>();


    public static LinkedList<TestGrInDriver> windows = new LinkedList<TestGrInDriver>();
    public static MobilePeripherals.DummyPointer pointer;
    public static String maintainText = null;
    public static boolean maintainTextEnter = false;
    public static int windowCount = 1337;

    public static void kickstart(final String s2, final String encoding, final String schema) {
        currentTestPhase = "Initial Phase";
        kickstartRFS();
        // In case unset.
        IObjectBackend.Factory.encoding = encoding;
        AppMain.initializeCore(s2, "", schema);
    }

    public static void kickstartRFS() {
        FontManager.fontsReady = true;
        GaBIEnImpl.mobileEmulation = true;
        windowCount = 0;
        mockFS.clear();
        mockDFS.clear();
        final GaBIEnImpl impl = new GaBIEnImpl(false, true) {

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
                return new TestGrInDriver(name, ws, makeOffscreenBufferInt(w, h, false));
            }

            @Override
            public void startFileBrowser(String text, boolean saving, String exts, IConsumer<String> result, String initialName) {
                waitingFileDialog = result;
            }

            @Override
            public int measureText(int i, String text) {
                return 16;
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
                byte[] data = mockFS.get(fileName);
                if (data == null) {
                    if (fileName.startsWith("./"))
                        fileName = fileName.substring(2);
                    return impl.getResource(fileName);
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
        // Cleanup any possible contamination of application state between tests.
        AppMain.shutdown();
        // Also resets FontManager because that's tied into config.
        FontSizes.reset();
    }

    public static void resetODB() {
        AppMain.objectDB = new ObjectDB(IObjectBackend.Factory.create(AppMain.odbBackend, AppMain.rootPath, AppMain.dataPath, AppMain.dataExt), new IConsumer<String>() {
            @Override
            public void accept(String s) {

            }
        });
    }

    public static class TestGrInDriver extends gabien.GrInDriver {
        private String internalMaintainText;
        private boolean didMaintainThisFrame;

        public TestGrInDriver(String name, WindowSpecs ws, IWindowGrBackend t) {
            super(name, ws, t);
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
        public boolean flush() {
            try {
                FileOutputStream debugOut = new FileOutputStream("test-out/debug.png");
                debugOut.write(createPNG());
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
            boolean b = super.flush();
            while (true) {
                // An entry returns false (which waits a frame) until it's done, then it returns true
                if (waitingTestEntries.size() == 0) {
                    GrandWindowManagerUtils.printTree();
                    throw new GrandExecutionError("Ran out of test sequence data.");
                }
                ISupplier<Boolean> isb = waitingTestEntries.getFirst();
                if (!isb.get())
                    break;
                waitingTestEntries.removeFirst();
            }
            return b;
        }
    }
}
