/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.tbleditors;

import gabien.ui.*;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.uslx.append.EmptyLambdas;
import r48.R48;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.TSDBChoiceIntegerSchemaElement;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Yay bitfields!
 * (As of sep-19-2017, has static methods for use by BitfieldSchemaElement, which uses the same syntax)
 * Created on 2/18/17.
 */
public class BitfieldTableCellEditor extends R48.Svc implements ITableCellEditor {
    public final String[] flags;

    public BitfieldTableCellEditor(R48 app, String[] strings) {
        super(app);
        flags = strings;
    }

    @Override
    public Runnable createEditor(final LinkedList<UIElement> base, final int[] planes, final Runnable changeOccurred) {
        final Consumer<Integer> editor1 = installEditor(app, flags, (element) -> {
            base.add(element);
        }, new AtomicReference<Consumer<Integer>>((t) -> {
            planes[0] = t;
            changeOccurred.run();
        }));
        editor1.accept(planes[0]);
        base.add(new UILabel(T.s.manualEdit, app.f.tableElementTH));
        final Runnable editor2 = new DefaultTableCellEditor(app).createEditor(base, planes, changeOccurred);
        
        return () -> {
            editor1.accept(planes[0]);
            editor2.run();
        };
    }

    // Returns 'update' runnable (which you should immediately run when ready).
    // Calls callbacks for various reasons.
    public static Consumer<Integer> installEditor(R48 app, final String[] flags, final Consumer<UIElement> panelAdder, final AtomicReference<Consumer<Integer>> set) {
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
                    IntegerSchemaElement ise = new IntegerSchemaElement(app, 0);
                    if (a.length > 2) {
                        if (a[2].startsWith("tsv=")) {
                            ise = new TSDBChoiceIntegerSchemaElement(app, 0, a[2].substring(4), pwr);
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
                            return new UIScrollLayout(true, app.f.generalS);
                        }
                    });

                    panelAdder.accept(new UISplitterLayout(new UILabel(name, app.f.tableElementTH), iai.uie, false, 1));
                    flagStates[i] = () -> {
                        int v = currentState.get();
                        iai.onValueChange.accept((long) ((v / thisBit) & (pwr - 1)));
                    };
                    bit <<= len;
                } else {
                    // Bool-field
                    final UITextButton flag = new UITextButton(Integer.toHexString(thisBit) + ": " + flags[i], app.f.tableElementTH, null).togglable(false);
                    panelAdder.accept(flag);
                    flagStates[i] = () -> {
                        flag.state = ((currentState.get() & thisBit) != 0);
                        flag.onClick = new Runnable() {
                            @Override
                            public void run() {
                                int v = currentState.get() ^ thisBit;
                                currentState.set(v);
                                set.get().accept(v);
                            }
                        };
                    };
                    bit <<= 1;
                }
            } else {
                // Do-nothing-this-just-uses-up-one-bit-without-bothering-the-user-field
                flagStates[i] = EmptyLambdas.emptyRunnable;
                bit <<= 1;
            }
        }
        return (i2) -> {
            currentState.set(i2);
            for (int i = 0; i < flagStates.length; i++)
                flagStates[i].run();
        };
    }
}
