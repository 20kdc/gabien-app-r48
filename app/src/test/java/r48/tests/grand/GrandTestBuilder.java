/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests.grand;

import gabien.MobilePeripherals;
import gabien.TestKickstart;
import gabien.ui.*;
import gabien.uslx.append.*;
import gabienapp.GrandLauncherUtils;
import gabienapp.Launcher;
import r48.io.IntUtils;
import r48.wm.GrandWindowManagerUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Created on March 28, 2019.
 */
public class GrandTestBuilder {
    public final TestKickstart kick;
    public GrandLauncherUtils lUtils;
    public GrandWindowManagerUtils wm;
    public GrandTestBuilder() {
        kick = new TestKickstart();
    }

    public void thenWaitFrame() {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            boolean waitFrame = true;

            @Override
            public Boolean get() {
                if (waitFrame) {
                    waitFrame = false;
                    return false;
                }
                return true;
            }
        });
    }

    public void thenSetPhase(final String phase) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                kick.currentTestPhase = phase;
                return true;
            }
        });
    }

    public void thenClick(final String id) {
        thenClick(id, 0, 0);
    }

    public void thenClick(final int i, final int i1) {
        thenClick(null, i, i1);
    }

    public void thenClick(final String id, final int ox, final int oy) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            boolean waitFrame = true;

            @Override
            public Boolean get() {
                if (waitFrame) {
                    Rect ctrl = wm.getControlRect(id);
                    kick.pointer = new MobilePeripherals.DummyPointer(ctrl.x + ox, ctrl.y + oy);
                    waitFrame = false;
                    return false;
                }
                kick.pointer = null;
                return true;
            }
        });
    }

    public void thenDrag(final int i, final int i1, final int i2, final int i3) {
        thenDrag(null, i, i1, i2, i3);
    }

    public void thenDrag(final String n1, final int i, final int i1, final int i2, final int i3) {
        thenDrag(n1, i, i1, n1, i2, i3);
    }

    public void thenDrag(final String n1, final int i, final int i1, final String n2, final int i2, final int i3) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            int waitFrame = 0;

            @Override
            public Boolean get() {
                if (waitFrame == 0) {
                    Rect ctrl = wm.getControlRect(n1);
                    kick.pointer = new MobilePeripherals.DummyPointer(ctrl.x + i, ctrl.y + i1);
                    waitFrame++;
                    return false;
                } else if (waitFrame == 1) {
                    Rect ctrl = wm.getControlRect(n2);
                    kick.pointer.x = ctrl.x + i2;
                    kick.pointer.y = ctrl.y + i3;
                    waitFrame++;
                    return false;
                }
                kick.pointer = null;
                return true;
            }
        });
    }

    public void thenScroll(final String n1, final String n2) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            boolean waitFrame = true;

            @Override
            public Boolean get() {
                if (waitFrame) {
                    UIScrollLayout ctrl = (UIScrollLayout) wm.getControl(n1);
                    GrandControlUtils.scroll(ctrl, wm.getControl(n2));
                    waitFrame = false;
                    return false;
                }
                return true;
            }
        });
    }

    public void thenWaitWC(final int wc) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return kick.windowCount == wc;
            }
        });
    }

    private UIElement getElement(String s) {
        UIElement[] w = wm.getAllWindows();
        for (UIElement uie : w) {
            if (uie.toString().contains(s))
                return uie;
        }
        System.err.println("---[");
        for (UIElement uie : w)
            System.err.println(uie.toString());
        System.err.println("---]");
        throw new GrandExecutionError("Unable to getElement " + s + " during phase " + kick.currentTestPhase);
    }

    public void thenIcon(final String title, final int idx) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            boolean waitFrame = true;

            @Override
            public Boolean get() {
                if (waitFrame) {
                    wm.clickIcon(getElement(title), idx);
                    waitFrame = false;
                    return false;
                }
                return true;
            }
        });
    }

    public void thenSelectTab(final String title) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            boolean waitFrame = true;

            @Override
            public Boolean get() {
                if (waitFrame) {
                    wm.selectTab(getElement(title));
                    waitFrame = false;
                    return false;
                }
                return true;
            }
        });
    }

    public void thenType(final String s) {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            boolean waitFrame = true;

            @Override
            public Boolean get() {
                if (waitFrame) {
                    kick.maintainText = s;
                    kick.maintainTextEnter = true;
                    waitFrame = false;
                    return false;
                }
                return true;
            }
        });
    }

    public void thenCloseWindow() {
        kick.waitingTestEntries.add(new Supplier<Boolean>() {
            boolean waitFrame = true;

            @Override
            public Boolean get() {
                if (waitFrame) {
                    kick.windows.getLast().shutdown();
                    waitFrame = false;
                    return false;
                }
                return true;
            }
        });
    }

    public void execute(long expectedChecksum) {
        try {
            kick.kickstartRFS();
            lUtils = new GrandLauncherUtils(new Launcher());
            wm = new GrandWindowManagerUtils(this);
            kick.gwmu = wm;
            lUtils.launcher.run();
            byte[] dat = createDump();
            long checksum = 0;
            for (byte b : dat)
                checksum += b & 0xFF;
            FileOutputStream fos = new FileOutputStream("test-out/debug" + checksum + ".pak");
            fos.write(dat);
            fos.close();
            // Checksum checking has been disabled due to general unreliability of the checksum algorithm over various changes.
            System.out.println("Test result: " + checksum);
            /*
            if (expectedChecksum != -1)
                if (expectedChecksum != checksum)
                    throw new RuntimeException("Checksum mismatch. Got " + checksum);
            */
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private byte[] createDump() throws IOException {
        LinkedList<DumpedLump> l = new LinkedList<DumpedLump>();

        LinkedList<String> lls = new LinkedList<String>(kick.mockFS.keySet());
        Collections.sort(lls);

        for (String s : lls)
            l.add(new DumpedLump(s, kick.mockFS.get(s)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int knownPos = 12 + (l.size() * 64);
        baos.write('P');
        baos.write('A');
        baos.write('C');
        baos.write('K');
        IntUtils.writeS32(baos, 12);
        IntUtils.writeS32(baos, l.size() * 64);
        for (DumpedLump dl : l) {
            byte[] baseName = new byte[56];
            int idx = 0;
            for (byte b : dl.name.getBytes("UTF-8"))
                baseName[idx++] = b;
            System.out.println(dl.name);
            baos.write(baseName);
            IntUtils.writeS32(baos, knownPos);
            IntUtils.writeS32(baos, dl.data.length);
            knownPos += dl.data.length;
        }
        for (DumpedLump dl : l)
            baos.write(dl.data);
        return baos.toByteArray();
    }

    private static class DumpedLump {
        String name;
        byte[] data;

        public DumpedLump(String file, byte[] bytes) {
            name = file;
            data = bytes;
        }
    }
}
