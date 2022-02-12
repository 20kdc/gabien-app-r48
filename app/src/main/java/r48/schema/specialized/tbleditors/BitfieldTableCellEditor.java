/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.tbleditors;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.TSDBChoiceIntegerSchemaElement;
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
    public Runnable createEditor(final UIScrollLayout base, final int[] planes, final Runnable changeOccurred) {
        final IConsumer<Integer> editor1 = installEditor(flags, new IConsumer<UIElement>() {
            @Override
            public void accept(UIElement element) {
                base.panelsAdd(element);
            }
        }, new AtomicReference<IConsumer<Integer>>(new IConsumer<Integer>() {
            @Override
            public void accept(Integer t) {
                planes[0] = t;
                changeOccurred.run();
            }
        }));
        editor1.accept(planes[0]);
        base.panelsAdd(new UILabel(TXDB.get("Manual Edit:"), FontSizes.tableElementTextHeight));
        final Runnable editor2 = new DefaultTableCellEditor().createEditor(base, planes, changeOccurred);
        
        return new Runnable() {
            public void run() {
                editor1.accept(planes[0]);
                editor2.run();
            }
        };
    }

    // Returns 'update' runnable (which you should immediately run when ready).
    // Calls callbacks for various reasons.
    public static IConsumer<Integer> installEditor(final String[] flags, final IConsumer<UIElement> panelAdder, final AtomicReference<IConsumer<Integer>> set) {
        int bit = 1;
        // When the value changes, all of these are called.
        final Runnable[] flagStates = new Runnable[flags.length];
        final AtomicInteger currentState = new AtomicInteger();
        for (int i = 0; i < flags.length; i++) {
            final int thisBit = bit;
            if (!flags[i].equals(".")) {
                if (flags[i].startsWith("[")) {
                    // Int-field
                    String[] a = flags[i].split(";");
                    final String name = a[0].substring(1);
                    final int len = Integer.parseInt(a[1]);
                    final int pwr = 1 << len;
                    IntegerSchemaElement ise = new IntegerSchemaElement(0);
                    if (a.length > 2) {
                        if (a[2].startsWith("tsv=")) {
                            ise = new TSDBChoiceIntegerSchemaElement(0, a[2].substring(4), pwr);
                        } else {
                            throw new RuntimeException("bitfield table syntax error, unknown ISE type " + a[2]);
                        }
                    }
                    final IntegerSchemaElement finalIse = ise;
                    final IntegerSchemaElement.ActiveInteger iai = ise.buildIntegerEditor(0, new IntegerSchemaElement.IIntegerContext() {
                        @Override
                        public void update(long n) {
                            n = finalIse.filter(n);
                            int r = currentState.get();
                            r &= ~(thisBit * (pwr - 1));
                            r |= thisBit * (n & (pwr - 1));
                            currentState.set(r);
                            set.get().accept(r);
                        }

                        @Override
                        public UIScrollLayout newSVL() {
                            return new UIScrollLayout(true, FontSizes.generalScrollersize);
                        }
                    });

                    panelAdder.accept(new UISplitterLayout(new UILabel(name, FontSizes.tableElementTextHeight), iai.uie, false, 1));
                    flagStates[i] = new Runnable() {
                        @Override
                        public void run() {
                            int v = currentState.get();
                            iai.onValueChange.accept((long) ((v / thisBit) & (pwr - 1)));
                        }
                    };
                    bit <<= len;
                } else {
                    // Bool-field
                    final UITextButton flag = new UITextButton(Integer.toHexString(thisBit) + ": " + flags[i], FontSizes.tableElementTextHeight, null).togglable(false);
                    panelAdder.accept(flag);
                    flagStates[i] = new Runnable() {
                        @Override
                        public void run() {
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
                flagStates[i] = new Runnable() {
                    @Override
                    public void run() {
                    }
                };
                bit <<= 1;
            }
        }
        return new IConsumer<Integer>() {
            @Override
            public void accept(Integer i2) {
                currentState.set(i2);
                for (int i = 0; i < flagStates.length; i++)
                    flagStates[i].run();
            }
        };
    }
}
