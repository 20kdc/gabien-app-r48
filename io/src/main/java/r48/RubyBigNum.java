/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48;

/**
 * Do you really want to know why this class exists?
 * ... didn't think so.
 * Too bad. Created on 1st December 2017 to cover BigNums.
 * Note that while true Ruby bignums are always positive data-wise,
 *  these bignums aren't (easier to write this way)
 */
public class RubyBigNum {
    // NOTE: To explain what's going on here:
    //       Ruby's BigNums are always represented as positive numbers.
    //       This is awful to handle math-wise, so this class handles bignums as a signed series of bytes.
    //       This has to be converted to/from Ruby's form as appropriate.
    //       Also, this can have a length of 0 (resulting number is 0)
    private final byte[] data;
    public static final RubyBigNum ZERO = new RubyBigNum(new byte[0], true);
    public static final RubyBigNum TEN = new RubyBigNum(10);
    private RubyBigNum cacheLShift = null;
    private RubyBigNum cacheRShift = null;
    private RubyBigNum cacheNegate = null;

    public RubyBigNum(byte[] data, boolean raw) {
        this.data = new byte[data.length];
        if (raw) {
            System.arraycopy(data, 0, this.data, 0, data.length);
        } else {
            this.data[0] = 0;
            System.arraycopy(data, 1, this.data, 1, data.length - 1);
            if (data[0] == '-') {
                // Luckily, the input given was in positive format, which means it's easy enough to negate.
                negateCore(this.data);
            }
        }
    }

    public RubyBigNum(long i) {
        this(reduceCore(new byte[] {
                (byte) ((i >> 56) & 0xFF),
                (byte) ((i >> 48) & 0xFF),
                (byte) ((i >> 40) & 0xFF),
                (byte) ((i >> 32) & 0xFF),
                (byte) ((i >> 24) & 0xFF),
                (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        }), true);
    }

    public String toString() {
        String st = "BigNum ";
        for (int i = 0; i < data.length; i++) {
            String s = Integer.toHexString(data[i] & 0xFF);
            if (s.length() == 1)
                s = "0" + s;
            st += s;
        }
        return st;
    }

    public byte[] toRuby() {
        byte[] basis = data;
        boolean neg = isNegative();
        if (neg)
            basis = negate().data;
        basis = reduceCore(basis);
        byte[] finale = new byte[basis.length + 1];
        finale[0] = (byte) (neg ? '-' : '+');
        System.arraycopy(basis, 0, finale, 1, basis.length);
        return finale;
    }

    public long truncateToLong() {
        long p = 1;
        long res = 0;
        byte[] iData = signExtendCore(data, Math.max(8, data.length));
        for (int i = iData.length - 1; i >= 0; i--) {
            res |= p * (iData[i] & 0xFFL);
            p <<= 8;
            if (p == 0)
                return res;
        }
        return res;
    }

    private static void negateCore(byte[] data) {
        // Firstly, NOT everything.
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) ((~data[i]) & 0xFF);
        // Secondly, increment by 1, considering carry.
        // 0x00FF
        // 0x0100
        // vs.
        // 0xFFFF
        // 0x0000
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] != -1) {
                data[i]++;
                break;
            } else {
                data[i] = 0;
                // Carry...
            }
        }
    }

    public RubyBigNum negate() {
        if (cacheNegate != null)
            return cacheNegate;
        // The +1 is "just in case", and is countered by reduceCore
        byte[] newData = new byte[data.length + 1];
        if (data.length > 0)
            if (data[0] < 0)
                newData[0] = (byte) 0xFF;
        System.arraycopy(data, 0, newData, 1, data.length);
        negateCore(newData);
        return cacheNegate = new RubyBigNum(reduceCore(newData), true);
    }

    public RubyBigNum add(RubyBigNum other) {
        byte[] ba = signExtendCore(data, Math.max(data.length + 1, other.data.length + 1));
        byte[] bb = signExtendCore(other.data, Math.max(data.length + 1, other.data.length + 1));
        // If I screw up this code, I have officially failed mental arithmetic. - 20kdc
        boolean carry = false;
        for (int i = ba.length - 1; i >= 0; i--) {
            // This is easier to understand unsigned.
            // If the total of A, B, and carry is >= 0x100,
            // then it's clearly carrying.
            int a = ba[i] & 0xFF;
            int b = bb[i] & 0xFF;
            int c = a + b;
            if (carry)
                c++;
            boolean ncarry = c >= 0x100;
            c &= 0xFF;
            ba[i] = (byte) c;
            carry = ncarry;
        }
        return new RubyBigNum(reduceCore(ba), true);
    }

    public int compare(RubyBigNum other) {
        RubyBigNum r2 = other.negate().add(this);
        if (r2.data.length == 0)
            return 0;
        if (r2.data[0] < 0)
            return -1;
        for (int i = 0; i < r2.data.length; i++)
            if (r2.data[i] != 0)
                return 1;
        throw new RuntimeException("Reducer isn't working properly. At this point the data should definitely be 0, but reducer didn't reduce it to 0 bytes.");
    }

    // Long division! Returns [div, mod].
    public RubyBigNum[] divide(RubyBigNum rbn) {
        // work out negative/positive stuff
        // NOTE: Semantics are from testing in Lua,
        //  so if you want an idea of what a given division will return, look there
        if (rbn.isZero())
            throw new RuntimeException("Attempt to divide by 0");
        if (isZero())
            return new RubyBigNum[]{
                    ZERO, ZERO
            };
        if (rbn.data[0] < 0) {
            RubyBigNum[] res = divide(rbn.negate());
            return new RubyBigNum[] {
                    res[0].negate(),
                    res[1].negate()
            };
        } else if (data[0] < 0) {
            RubyBigNum[] res = negate().divide(rbn);
            return new RubyBigNum[] {
                    res[0].negate(),
                    res[1]
            };
        }
        // Ok, this and the divider are now >= 0
        RubyBigNum div, mod, power;
        div = ZERO;
        mod = this;
        power = new RubyBigNum(new byte[] {1}, true);
        // This mustn't change despite mod changing.
        int mdl8 = mod.data.length * 8;
        for (int i = 0; i < mdl8; i++) {
            rbn = rbn.shiftL(false);
            power = power.shiftL(false);
        }
        // <= because it has to run a final round after it's completely shifted
        for (int i = 0; i <= mdl8; i++) {
            RubyBigNum without = mod.add(rbn.negate());
            boolean zero = without.isZero();
            if (!(zero || without.isNegative())) {
                // 'without' not <= 0 means without is > 0,
                // which means this division can occur
                mod = without;
                div = div.add(power);
            } else if (zero) {
                div = div.add(power);
                return new RubyBigNum[] {
                        div,
                        ZERO
                };
            }
            rbn = rbn.shiftR(false);
            power = power.shiftR(false);
        }
        return new RubyBigNum[] {
                div,
                mod
        };
    }

    public RubyBigNum shiftR(boolean arithmetic) {
        if (!arithmetic)
            if (cacheRShift != null)
                return cacheRShift;
        byte[] newData = new byte[data.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        for (int i = newData.length - 1; i >= 0; i--) {
            int nextBit = 0;
            if (i > 0) {
                nextBit = (newData[i - 1] & 1) << 7;
            } else if (arithmetic) {
                nextBit = newData[i] & 0x80;
            }
            newData[i] = (byte) (((newData[i] >> 1) & 0x7F) | nextBit);
        }
        RubyBigNum res = new RubyBigNum(newData, true);
        if (!arithmetic)
            cacheRShift = res;
        return res;
    }

    public RubyBigNum shiftL(boolean arithmetic) {
        if (!arithmetic)
            if (cacheLShift != null)
                return cacheLShift;
        byte[] newData = new byte[data.length + 1];
        if (arithmetic)
            if (data.length != 0)
                if (data[0] < 0)
                    newData[0] = (byte) 0xFF;
        System.arraycopy(data, 0, newData, 1, data.length);
        for (int i = 0; i < newData.length; i++) {
            int nextBit = 0;
            if (i < newData.length - 1)
                nextBit = (newData[i + 1] & 0x80) >> 7;
            newData[i] = (byte) ((newData[i] << 1) | nextBit);
        }
        RubyBigNum res = new RubyBigNum(reduceCore(newData), true);
        if (!arithmetic)
            cacheLShift = res;
        return res;
    }

    // This is allowed to return the original array
    private static byte[] reduceCore(byte[] ba) {
        int removable = 0;
        boolean signSet = false;
        boolean sign = false;
        for (int i = 0; i < ba.length; i++) {
            boolean nSign = ba[i] < 0;
            if (!signSet) {
                sign = nSign;
                signSet = true;
            }
            byte expected = sign ? (byte) -1 : 0;
            if (expected != ba[i]) {
                break;
            } else {
                removable++;
            }
        }
        if (removable == 0)
            return ba;
        // Make sure sign is preserved.
        if (removable == ba.length) {
            if (!sign) {
                // Positive, and apparently removable, so it's 0
                return new byte[0];
            } else {
                // Negative yet removable, this is -1
                return new byte[] {
                        (byte) 0xFF
                };
            }
        } else {
            // Implies non-zero length.
            // Ensure first non-removable byte has the expected sign.
            // If removable = 0, then sign should *always* be equal
            if ((ba[removable] < 0) != sign)
                removable--;
            if (removable < 0)
                throw new RuntimeException("Should be impossible : removable was moved under 0 when sign was unequal.");
        }
        byte[] res = new byte[ba.length - removable];
        System.arraycopy(ba, removable, res, 0, res.length);
        return res;
    }

    // This is expected to always return a new array
    private static byte[] signExtendCore(byte[] data, int max) {
        byte[] mx = new byte[max];
        if (data.length > max)
            throw new RuntimeException("sign extend used incorrectly, max must be at least the input length");
        System.arraycopy(data, 0, mx, max - data.length, data.length);
        if (data.length > 0)
            if (data[0] < 0)
                for (int i = 0; i < max - data.length; i++)
                    mx[i] = -1;
        return mx;
    }

    public boolean isNegative() {
        if (data.length == 0)
            return false;
        return data[0] < 0;
    }

    public boolean isZero() {
        if (data.length == 0)
            return true;
        for (int i = 0; i < data.length; i++)
            if (data[i] != 0)
                return false;
        return true;
    }
}
