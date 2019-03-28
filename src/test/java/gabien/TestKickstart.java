/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import gabien.ui.IConsumer;
import gabien.ui.IFunction;
import gabien.ui.IPointer;
import gabien.ui.ISupplier;
import r48.AppMain;
import r48.dbs.ObjectDB;
import r48.io.IObjectBackend;
import r48.tests.grand.GrandExecutionError;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Ties into gabien-javase to create a test workbench.
 * Unusable outside of it.
 * Created on November 19, 2018.
 */
public class TestKickstart {
    public static LinkedList<ISupplier<Boolean>> waitingTestEntries = new LinkedList<ISupplier<Boolean>>();
    public static IConsumer<String> waitingFileDialog = null;

    public static HashMap<String, byte[]> mockFS = new HashMap<String, byte[]>();
    public static HashSet<String> mockDFS = new HashSet<String>();


    public static LinkedList<TestGrInDriver> windows = new LinkedList<TestGrInDriver>();
    public static MobilePeripherals.DummyPointer pointer;
    public static String maintainText = null;
    public static boolean maintainTextEnter = false;
    public static int windowCount = 0;

    public static void kickstart(final String s2, final String encoding, final String schema) {
        kickstartRFS();
        // In case unset.
        IObjectBackend.Factory.encoding = encoding;
        AppMain.initializeCore(s2, schema);
    }

    public static void kickstartRFS() {
        FontManager.fontOverride = null;
        FontManager.fontOverrideUE8 = false;
        FontManager.fontsReady = true;
        GaBIEnImpl.mobileEmulation = true;
        mockFS.clear();
        mockDFS.clear();
        GaBIEn.internal = new GaBIEnImpl(false) {

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
            public void rmFile(String s) {
                if (s.endsWith("/"))
                    throw new RuntimeException("Not valid");
                if (mockDFS.contains(s)) {
                    for (String st : new HashSet<String>(mockDFS))
                        if (st.startsWith(s + "/"))
                            mockDFS.remove(st);
                    mockDFS.remove(s);
                } else {
                    mockFS.remove(s);
                }
            }

            @Override
            public void makeDirectories(String s) {
                if (s.endsWith("/"))
                    throw new RuntimeException("Not valid");
                String dn = GaBIEn.dirname(s);
                if (!dn.equals(""))
                    makeDirectories(dn);
                mockDFS.add(s);
            }

            @Override
            public boolean fileOrDirExists(String s) {
                if (s.endsWith("/"))
                    throw new RuntimeException("Not valid");
                return mockDFS.contains(s) || mockFS.containsKey(s);
            }

            @Override
            public boolean dirExists(String s) {
                if (s.endsWith("/"))
                    throw new RuntimeException("Not valid");
                return mockDFS.contains(s);
            }

            // NOT a completely compliant result!!!
            @Override
            public String[] listEntries(String s) {
                if (!s.endsWith("/"))
                    s += "/";
                LinkedList<String> str = new LinkedList<String>();
                for (String st : mockDFS)
                    listEntry(st, str, s);
                for (String st : mockFS.keySet())
                    listEntry(st, str, s);
                return str.toArray(new String[0]);
            }

            private void listEntry(String st, LinkedList<String> str, String s) {
                String dn = GaBIEn.dirname(st);
                String bn = GaBIEn.basename(st);
                if (dn.equals(s))
                    str.add(bn);
            }

            @Override
            public InputStream getFile(String FDialog) {
                byte[] data = mockFS.get(FDialog);
                if (data == null) {
                    if (FDialog.startsWith("./"))
                        FDialog = FDialog.substring(2);
                    return getResource(FDialog);
                }
                return new ByteArrayInputStream(data);
            }

            @Override
            public OutputStream getOutFile(final String FDialog) {
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        mockFS.put(FDialog, toByteArray());
                    }
                };
            }

            @Override
            public IGrInDriver makeGrIn(String name, int w, int h, WindowSpecs ws) {
                ws.resizable = false;
                w = 960;
                h = 540;
                return new TestGrInDriver(name, ws, makeOffscreenBufferInt(w, h, false));
            }

            @Override
            public void startFileBrowser(String text, boolean saving, String exts, IConsumer<String> result) {
                waitingFileDialog = result;
            }

            @Override
            public int measureText(int i, String text) {
                return 16;
            }
        };
        // Cleanup any possible contamination of application state between tests.
        AppMain.shutdown();
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
            peripherals = new IPeripherals() {
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
                public String maintain(int x, int y, int width, String text, IFunction<String, String> feedback) {
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
            };
            windows.add(this);
        }

        @Override
        public boolean flush() {
            try {
                FileOutputStream debugOut = new FileOutputStream("test-debug.png");
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
                if (waitingTestEntries.size() == 0)
                    throw new GrandExecutionError("Ran out of test sequence data.");
                ISupplier<Boolean> isb = waitingTestEntries.getFirst();
                if (!isb.get())
                    break;
                waitingTestEntries.removeFirst();
            }
            return b;
        }
    }
}
