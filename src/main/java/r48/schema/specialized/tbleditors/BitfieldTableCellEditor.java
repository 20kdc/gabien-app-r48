/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.tbleditors;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.ui.UIGrid;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Yay bitfields!
 * (As of sep-19-2017, has static methods for use by BitfieldSchemaElement, which uses the same syntax)
 * Created on 2/18/17.
 */
public class BitfieldTableCellEditor implements ITableCellEditor {
    public final String[] flags;

    public BitfieldTableCellEditor(String[] strings) {
        flags = strings;
    }

    @Override
    public Runnable createEditor(final UIScrollLayout panel, final RubyIO targV, final UIGrid uig, final Runnable changeOccurred) {
        final RubyTable targ = new RubyTable(targV.userVal);
        final AtomicReference<IConsumer<Integer>> setter = new AtomicReference<IConsumer<Integer>>();
        final IConsumer<Integer> wtm = installEditor(flags, new IConsumer<UIElement>() {
            @Override
            public void accept(UIElement element) {
                panel.panels.add(element);
            }
        }, setter);
        panel.panels.add(new UILabel(TXDB.get("Manual Edit:"), FontSizes.tableElementTextHeight));
        final Runnable manualControl = new DefaultTableCellEditor().createEditor(panel, targV, uig, changeOccurred);
        return new Runnable() {
            @Override
            public void run() {
                int sel = uig.getSelected();
                final int selX = sel % targ.width;
                final int selY = sel / targ.width;
                setter.set(new IConsumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        targ.setTiletype(selX, selY, 0, (short) (int) integer);
                    }
                });
                wtm.accept((int) targ.getTiletype(selX, selY, 0));
                manualControl.run();
            }
        };
    }

    // Returns 'update' runnable (which you should immediately run when ready).
    // Calls callbacks for various reasons.
    public static IConsumer<Integer> installEditor(final String[] flags, final IConsumer<UIElement> panelAdder, final AtomicReference<IConsumer<Integer>> set) {
        int bit = 1;
        // Java: "generic array creation warning!"
        // me: adds specifier
        // Java: "invalid syntax deadbeat".
        // This contains a bunch of consumers, the idea being that each is responsible for it's own section (they may not overlap!),
        //  and the consumers take an AtomicInteger as a common workspace.
        // They then call on the "confirmButton" (used to be an actual button) to save.
        // The result should be simple and easy to use, but now I know why Windows is so broken.
        // GOOD UI PROGRAMMING RESULTS IN LIVING IN A GIANT GAME OF MOUSETRAP.
        // (Alternatively, a ton of abstraction, but I already abstracted enough today.)
        final IConsumer<AtomicInteger>[] flagStates = new IConsumer[flags.length];
        for (int i = 0; i < flags.length; i++) {
            final int thisBit = bit;
            if (!flags[i].equals(".")) {
                if (flags[i].startsWith("[")) {
                    // Int-field
                    String[] a = flags[i].split(";");
                    final String name = a[0].substring(1);
                    final int len = Integer.parseInt(a[1]);
                    final int pwr = 1 << len;
                    final UINumberBox number = new UINumberBox(FontSizes.tableElementTextHeight);
                    panelAdder.accept(new UISplitterLayout(new UILabel(name, FontSizes.tableElementTextHeight), number, false, 3, 4));
                    flagStates[i] = new IConsumer<AtomicInteger>() {
                        @Override
                        public void accept(final AtomicInteger currentState) {
                            int v = currentState.get();
                            number.number = (v / thisBit) & (pwr - 1);
                            number.onEdit = new Runnable() {
                                @Override
                                public void run() {
                                    int r = currentState.get();
                                    r &= ~(thisBit * (pwr - 1));
                                    r |= thisBit * (number.number & (pwr - 1));
                                    currentState.set(r);
                                    set.get().accept(r);
                                }
                            };
                        }
                    };
                    bit <<= len;
                } else {
                    // Bool-field
                    final UITextButton flag = new UITextButton(FontSizes.tableElementTextHeight, Integer.toHexString(thisBit) + ": " + flags[i], null).togglable();
                    panelAdder.accept(flag);
                    flagStates[i] = new IConsumer<AtomicInteger>() {
                        @Override
                        public void accept(final AtomicInteger currentState) {
                            flag.state = ((currentState.get() & thisBit) != 0);
                            flag.onClick = new Runnable() {
                                @Override
                                public void run() {
                                    int v = currentState.get() ^ thisBit;
                                    currentState.set(v);
                                    set.get().accept(v);
                                }
                            };
                        }
                    };
                    bit <<= 1;
                }
            } else {
                // Do-nothing-this-just-uses-up-one-bit-without-bothering-the-user-field
                flagStates[i] = new IConsumer<AtomicInteger>() {
                    @Override
                    public void accept(AtomicInteger atomicInteger) {
                    }
                };
                bit <<= 1;
            }
        }
        return new IConsumer<Integer>() {
            @Override
            public void accept(Integer i2) {
                final AtomicInteger currentState = new AtomicInteger(i2);
                for (int i = 0; i < flagStates.length; i++)
                    flagStates[i].accept(currentState);
            }
        };
    }
}
