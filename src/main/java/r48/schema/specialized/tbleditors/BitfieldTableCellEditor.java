/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.tbleditors;

import gabien.ui.IConsumer;
import gabien.ui.UILabel;
import gabien.ui.UINumberBox;
import gabien.ui.UITextButton;
import r48.FontSizes;
import r48.RubyIO;
import r48.RubyTable;
import r48.ui.UIGrid;
import r48.ui.UIHHalfsplit;
import gabien.ui.UIScrollLayout;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Yay bitfields!
 * Created on 2/18/17.
 */
public class BitfieldTableCellEditor implements ITableCellEditor {
    public final String[] flags;

    public BitfieldTableCellEditor(String[] strings) {
        flags = strings;
    }

    @Override
    public Runnable createEditor(UIScrollLayout panel, final RubyIO targV, final UIGrid uig, final Runnable changeOccurred) {
        final RubyTable targ = new RubyTable(targV.userVal);
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
        final AtomicReference<Runnable> confirmButton = new AtomicReference<Runnable>();
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
                    panel.panels.add(new UIHHalfsplit(3, 4, new UILabel(name, FontSizes.tableElementTextHeight), number));
                    flagStates[i] = new IConsumer<AtomicInteger>() {
                        @Override
                        public void accept(final AtomicInteger currentState) {
                            int v = currentState.get();
                            number.number = (v / thisBit) & (pwr - 1);
                            number.onEdit = new Runnable() {
                                @Override
                                public void run() {
                                    int r = currentState.get();
                                    r &= 0xFFFF ^ (thisBit * (pwr - 1));
                                    r |= thisBit * (number.number & (pwr - 1));
                                    currentState.set(r);
                                    confirmButton.get().run();
                                }
                            };
                        }
                    };
                    bit <<= len;
                } else {
                    // Bool-field
                    final UITextButton flag = new UITextButton(FontSizes.tableElementTextHeight, Integer.toHexString(thisBit) + ": " + flags[i], null).togglable();
                    panel.panels.add(flag);
                    flagStates[i] = new IConsumer<AtomicInteger>() {
                        @Override
                        public void accept(final AtomicInteger currentState) {
                            flag.state = ((currentState.get() & thisBit) != 0);
                            flag.OnClick = new Runnable() {
                                @Override
                                public void run() {
                                    currentState.set(currentState.get() ^ thisBit);
                                    confirmButton.get().run();
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
        setButtonStates(flagStates, 0, 0, targ, confirmButton, changeOccurred);
        return new Runnable() {
            @Override
            public void run() {
                int sel = uig.getSelected();
                int selX = sel % targ.width;
                int selY = sel / targ.width;
                boolean oob = targ.outOfBounds(selX, selY);
                if (oob) {
                    setButtonStates(flagStates, 0, 0, targ, confirmButton, changeOccurred);
                } else {
                    setButtonStates(flagStates, selX, selY, targ, confirmButton, changeOccurred);
                }
            }
        };
    }

    private void setButtonStates(IConsumer<AtomicInteger>[] flagStates, final int x, final int y, final RubyTable rt, final AtomicReference<Runnable> confirm, final Runnable onChange) {
        final AtomicInteger currentState = new AtomicInteger(rt.getTiletype(x, y, 0) & 0xFFFF);
        confirm.set(new Runnable() {
            @Override
            public void run() {
                rt.setTiletype(x, y, 0, (short) currentState.get());
                onChange.run();
            }
        });
        for (int i = 0; i < flagStates.length; i++)
            flagStates[i].accept(currentState);
    }

}
