/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.LinkedList;
import java.util.List;

import gabien.datum.DatumSymbol;
import r48.minivm.MVMU;
import r48.minivm.MVMEnv;
import r48.minivm.MVMType;

/**
 * MiniVM standard library.
 * Created 2nd March 2023.
 */
public class MVMStringLibrary {
    public static void add(MVMEnv ctx) {
        // strings
        ctx.defineSlot(new DatumSymbol("string-append"), new Add()
                .attachHelp("(string-append V...) : Appends items into a big string."));
        ctx.defLib("string-length", MVMType.I64, MVMType.STR, (a0) -> (long) ((String) a0).length())
            .attachHelp("(string-length V) : Returns the length of a string.");
        ctx.defineSlot(new DatumSymbol("number->string"), new N2S()
                .attachHelp("(number->string V [R]) : Converts a number to a string with possible conversion."));
        ctx.defineSlot(new DatumSymbol("string->number"), new S2N()
                .attachHelp("(string->number V [R]) : Converts a string to a number."));
        ctx.defLib("string-ref", MVMType.CHAR, MVMType.STR, MVMType.I64, (a0, a1) -> ((String) a0).charAt(MVMU.cInt(a1)))
            .attachHelp("(string-ref V K) : Returns a character from a string.");
        ctx.defLib("string->list", new MVMType.TypedList(MVMType.CHAR), MVMType.STR, (a0) -> {
            LinkedList<Character> llc = new LinkedList<>();
            String s = (String) a0;
            int sl = s.length();
            for (int i = 0; i < sl; i++)
                llc.add(s.charAt(i));
            return llc;
        }).attachHelp("(string->list V) : String to list of characters.");
        ctx.defLib("list->string", MVMType.STR, new MVMType.TypedList(MVMType.CHAR), (a0) -> {
            @SuppressWarnings("unchecked")
            List<Character> lc = (List<Character>) a0;
            char[] chars = new char[lc.size()];
            int ptr = 0;
            for (Character ch : lc)
                chars[ptr++] = ch;
            return new String(chars);
        }).attachHelp("(list->string V) : List of characters to string.");
        ctx.defLib("string->symbol", MVMType.SYM, MVMType.STR, (a0) -> new DatumSymbol((String) a0))
            .attachHelp("(string->symbol V) : String to symbol.");
        ctx.defLib("symbol->string", MVMType.STR, MVMType.SYM, (a0) -> ((DatumSymbol) a0).id)
            .attachHelp("(symbol->string V) : Symbol to string.");
        ctx.defineSlot(new DatumSymbol("substring"), new Sub()
                .attachHelp("(substring S START END) : Substring."));
        ctx.defLib("value->string", MVMType.STR, MVMType.ANY, (a0) -> String.valueOf(a0))
            .attachHelp("(value->string V) : Converts any value to a string.");
        // chars
        ctx.defLib("char->integer", MVMType.I64, MVMType.CHAR, (a0) -> (long) (Character) a0)
            .attachHelp("(char->integer V) : Character to integer.");
        ctx.defLib("integer->char", MVMType.CHAR, MVMType.I64, (a0) -> (char) MVMU.cInt(a0))
            .attachHelp("(integer->char V) : Integer to Character.");
    }

    public static final class Add extends MVMFn {
        public Add() {
            super(new MVMType.Fn(MVMType.STR, 0, new MVMType[0], MVMType.STR), "string-append");
        }

        @Override
        protected Object callDirect() {
            return "";
        }
        @Override
        protected Object callDirect(Object a0) {
            return (String) a0;
        }
        @Override
        protected Object callDirect(Object a0, Object a1) {
            String x0 = (String) a0;
            int l0 = x0.length();
            String x1 = (String) a1;
            int l1 = x1.length();
            char[] total = new char[l0 + l1];
            int ptr = 0;
            x0.getChars(0, l0, total, 0);
            ptr += l0;
            x1.getChars(0, l1, total, ptr);
            return new String(total);
        }
        @Override
        protected Object callDirect(Object a0, Object a1, Object a2) {
            String x0 = (String) a0;
            int l0 = x0.length();
            String x1 = (String) a1;
            int l1 = x1.length();
            String x2 = (String) a2;
            int l2 = x2.length();
            char[] total = new char[l0 + l1 + l2];
            int ptr = 0;
            x0.getChars(0, l0, total, 0);
            ptr += l0;
            x1.getChars(0, l1, total, ptr);
            ptr += l1;
            x2.getChars(0, l2, total, ptr);
            return new String(total);
        }
        @Override
        protected Object callDirect(Object a0, Object a1, Object a2, Object a3) {
            String x0 = (String) a0;
            int l0 = x0.length();
            String x1 = (String) a1;
            int l1 = x1.length();
            String x2 = (String) a2;
            int l2 = x2.length();
            String x3 = (String) a3;
            int l3 = x3.length();
            char[] total = new char[l0 + l1 + l2 + l3];
            int ptr = 0;
            x0.getChars(0, l0, total, 0);
            ptr += l0;
            x1.getChars(0, l1, total, ptr);
            ptr += l1;
            x2.getChars(0, l2, total, ptr);
            ptr += l2;
            x3.getChars(0, l3, total, ptr);
            return new String(total);
        }

        @Override
        protected Object callIndirect(Object[] args) {
            int len = 0;
            for (Object obj : args)
                len += ((String) obj).length();
            char[] total = new char[len];
            int ptr = 0;
            for (Object obj : args) {
                String s = (String) obj;
                s.getChars(0, s.length(), total, ptr);
                ptr += s.length();
            }
            return new String(total);
        }
    }

    public static final class N2S extends MVMFn.Fixed {
        public N2S() {
            super(new MVMType.Fn(MVMType.STR, 1, new MVMType[] {MVMType.NUM, MVMType.I64}, null), "number->string");
        }

        @Override
        public Object callDirect(Object a0) {
            return core((Number) a0, 10);
        }

        @Override
        public Object callDirect(Object a0, Object a1) {
            return core((Number) a0, MVMU.cInt(a1));
        }

        public String core(Number a0, int radix) {
            if (a0 instanceof Double) {
                if (radix != 10)
                    throw new RuntimeException("can't convert float/double to non-decimal");
                return Double.toString(a0.doubleValue());
            }
            return Long.toString((Long) a0, radix);
        }
    }

    public static final class S2N extends MVMFn.Fixed {
        public S2N() {
            super(new MVMType.Fn(MVMType.NUM, 1, new MVMType[] {MVMType.STR, MVMType.I64}, null), "string->number");
        }

        @Override
        public Object callDirect(Object a0) {
            return core((String) a0, 10);
        }

        @Override
        public Object callDirect(Object a0, Object a1) {
            return core((String) a0, MVMU.cInt(a1));
        }

        public Object core(String s, int radix) {
            try {
                return Long.parseLong(s, radix);
            } catch (Exception ex) {
            }
            if (radix != 10) {
                try {
                    return Double.parseDouble(s);
                } catch (Exception ex) {
                }
            }
            return false;
        }
    }

    public static final class Sub extends MVMFn.Fixed {
        public Sub() {
            super(MVMType.Fn.simple(MVMType.STR, MVMType.STR, MVMType.I64, MVMType.I64), "substring");
        }

        @Override
        public Object callDirect(Object a0, Object a1, Object a2) {
            return ((String) a0).substring(MVMU.cInt(a1), MVMU.cInt(a2));
        }
    }
}
