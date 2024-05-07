/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import r48.io.data.DMKey;
import r48.io.data.IDM3Context;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Support for the mostly ASCII 'IMI' format
 * Note that this does NOT, as a rule, use UTF-anything.
 * Byte array format:
 * Starts with " for clarity.
 * There are three special characters:
 *  ": Ends the array
 *  \: Two hex digits follow - this is a raw byte.
 * This is meant to imitate the look of a string.
 * Outer Level Commands:
 * I2[dbBackend][dataPath][dataExt][encoding] : Must be the first command. Specifies details via UTF-8 byte arrays.
 * ~[file][IMI] : Patch a file (UTF-8 byte array path) with a given IMI segment
 * +[file][IMI] : Create a file (UTF-8 byte array path) with a given IMI segment
 *                (If the file already exists, a patch error has occurred)
 * F[file][data]: Write an assetfile (UTF-8 byte array path) with the following data (byte array)
 * (32, 9, 13, 10) : NOP
 * IMI Segment Commands:
 * (32, 9, 13, 10):  NOP
 * ?[char] : Enforces that the type is as given by character-byte, on pain of patch error.
 *           The reason it's done this way is because IMI's pretending to be human-readable.
 * ![char] : Reset current object, then change type *by character-byte*.
 *           Unless stated in a given type's field notes, the object continues, even for 0TF.
 *           Fields are given as appropriate:
 *            i: Semicolon-terminated number.
 *                *Ends object immediately.*
 *            l: Binary byte array w/ starting " in the slightly-odd format used internally by R48, that removes the length field.
 *                *Ends object immediately.*
 *            ": Byte array (with no starting '"'!)
 *            ':' / 'o': byte array with starting " for sym - syms are encoded in UTF-8.
 *            }: Default value *immediately* follows as a IMI segment.
 *            u: Byte array with starting " for sym (like 'o') followed by byte array for actual data.
 *            [: Colon-terminated number describing length (due to DM2. uses resize algorithm used by ArraySchemaElement/etc.).
 *
 * NOTE: As of DM2, a blank object may not, in fact, be blank.
 * Thus, the createIMIDump function is more complex.
 *
 * =[k][v]: Create or patch an instance variable. [k] is a UTF-8 byte array - [v] is another IMI segment.
 *          The default value is INVALID (new RubyIO())
 * >[k][v]: Add or patch a hash or array key.
 *          Both values are IMI segments - the first has to return integer for arrays.
 * -=[k]  : Delete an instance variable (name encoded as a UTF-8 byte array)
 * ->[k]  : Delete a hash key or array index
 * In the case of arrays, insertions/deletions act as one would expect for an array.
 * This means > is unable to patch, so:
 * ][k]:[v]: Patch an array key. k is a colon-terminated number, v is a IMI segment.
 * ;      : Exit object
 */
public class IMIUtils {
    // Return a comparison between two objects.
    // If this returns null, nothing important happened.
    public static byte[] createIMIData(IRIO source, IRIO target, String indent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        if (!IRIO.rubyTypeEquals(source, target)) {
            createIMIDump(dos, target, indent);
            return baos.toByteArray();
        }
        if (IRIO.rubyEquals(source, target))
            return null;
        String id2 = incrementIndent(indent);
        // NOTE: Most types are not patchable. Don't try to patch them.
        boolean flagMod = false; // did enough change to bother returning the result
        switch (source.getType()) {
            case 'o':
                if (!source.getSymbol().equals(target.getSymbol())) {
                    // unreachable with rubyTypeEquals but just in case
                    flagMod = true;
                    createIMIDump(dos, target, indent);
                } else {
                    // Objects are "equivalent", run patchIVs()
                    dos.writeBytes("?" + ((char) target.getType()) + "\n");
                    flagMod |= patchIVs(dos, source, target, id2);
                }
                break;
            case 'u':
                if (!source.getSymbol().equals(target.getSymbol())) {
                    // unreachable with rubyTypeEquals but just in case
                    flagMod = true;
                    createIMIDump(dos, target, indent);
                } else {
                    // check data
                    boolean dataEq = true;
                    byte[] suv = source.getBuffer();
                    byte[] duv = target.getBuffer();
                    if (suv.length == duv.length) {
                        for (int i = 0; i < suv.length; i++) {
                            if (suv[i] != duv[i]) {
                                dataEq = true;
                                break;
                            }
                        }
                    } else {
                        dataEq = false;
                    }
                    if (dataEq) {
                        dos.writeBytes("?" + ((char) target.getType()) + "\n");
                        flagMod |= patchIVs(dos, source, target, id2);
                    } else {
                        flagMod = true;
                        createIMIDump(dos, target, indent);
                    }
                }
                break;
            case '[':
                if (source.getAFixedFormat() || target.getAFixedFormat()) {
                    // 'Fixed-format' objects *cannot* be safely rearranged without causing potential crashes in IRIO code.
                    int sal = source.getALen();
                    if (sal == target.getALen()) {
                        // Used to ensure that it doesn't patch 100% of the time
                        dos.writeBytes("?" + ((char) target.getType()) + "\n");
                        for (int i = 0; i < sal; i++) {
                            byte[] modData = createIMIData(source.getAElem(i), target.getAElem(i), id2);
                            if (modData != null) {
                                dos.writeBytes(id2 + "]" + target.getType() + ":\n");
                                dos.write(modData);
                                flagMod = true;
                            }
                        }
                        flagMod |= patchIVs(dos, source, target, id2);
                    } else {
                        flagMod = true;
                        createIMIDump(dos, target, indent);
                    }
                } else {
                    dos.writeBytes("?" + ((char) target.getType()) + "\n");
                    flagMod |= patchArray(dos, source, target, id2);
                    flagMod |= patchIVs(dos, source, target, id2);
                }
                break;
            case '{':
                dos.writeBytes("?" + ((char) target.getType()) + "\n");
                for (DMKey rk : source.getHashKeys())
                    if (target.getHashVal(rk) == null) {
                        dos.writeBytes(id2 + "->");
                        createIMIDump(dos, target, id2);
                        flagMod = true;
                    }
                for (DMKey meKey : target.getHashKeys()) {
                    IRIO meVal = target.getHashVal(meKey);
                    IRIO rio = source.getHashVal(meKey);
                    if (rio == null) {
                        flagMod = true;
                        dos.writeBytes(id2 + ">");
                        createIMIDump(dos, meKey, id2);
                        dos.writeBytes(id2);
                        createIMIDump(dos, meVal, id2);
                    } else {
                        byte[] diff = createIMIData(rio, meVal, id2);
                        if (diff != null) {
                            flagMod = true;
                            dos.writeBytes(id2 + ">");
                            createIMIDump(dos, meKey, id2);
                            dos.writeBytes(id2);
                            dos.write(diff);
                        }
                    }
                }
                flagMod |= patchIVs(dos, source, target, id2);
                break;
            default:
                flagMod = true;
                createIMIDump(dos, target, indent);
        }
        if (!flagMod)
            return null;
        return baos.toByteArray();
    }

    private static boolean patchArray(DataOutputStream dos, IRIO source, IRIO target, String id2) throws IOException {
        int srcAVL = source.getALen();
        int tgtAVL = target.getALen();

        // Maps target indexes to source indexes (or -1 if not compatible).
        // Source indexes must be in ascending order, but may be skipped.
        int[] mappingsTarget = new int[tgtAVL];
        int lowestUsedIndex = -1;
        for (int i = 0; i < mappingsTarget.length; i++)
            mappingsTarget[i] = -1;

        for (int i = 0; i < srcAVL; i++) {
            int bestCompatibility = -1;
            int bestCompatibilityIndex = -1;
            int thresholdIncompatible = 0x40000000;
            for (int j = lowestUsedIndex + 1; j < tgtAVL; j++) {
                int compat = imiCompatibilityIndex(source.getAElem(i), target.getAElem(j));
                if (compat > bestCompatibility) {
                    bestCompatibility = compat;
                    bestCompatibilityIndex = j;
                }
            }
            // Used so that, say: ["A", "B", "C"] -> ["A", "D", "B", "C"]
            //  results in >i1;D;
            // Otherwise, 0 compatibility would be 'good enough'
            if (bestCompatibility < thresholdIncompatible)
                bestCompatibilityIndex = -1;
            if (bestCompatibilityIndex != -1) {
                mappingsTarget[bestCompatibilityIndex] = i;
                lowestUsedIndex = bestCompatibilityIndex;
            }
        }
        int currentSourceIndex = 0;
        boolean mod = false;
        for (int i = 0; i < tgtAVL; i++) {
            if (mappingsTarget[i] != -1) {
                if (mappingsTarget[i] < currentSourceIndex)
                    throw new IOException("Impossible mapping!");
                while (mappingsTarget[i] != currentSourceIndex) {
                    mod = true;
                    dos.writeBytes(id2 + "->");
                    createIMIDump(dos, DMKey.of(i), id2);
                    currentSourceIndex++;
                }
                byte[] patch = createIMIData(source.getAElem(currentSourceIndex), target.getAElem(i), incrementIndent(id2));
                if (patch != null) {
                    mod = true;
                    dos.writeBytes(id2 + "]" + i + ":");
                    dos.write(patch);
                }
                currentSourceIndex++;
            } else {
                mod = true;
                dos.writeBytes(id2 + ">");
                createIMIDump(dos, DMKey.of(i), id2);
                dos.writeBytes(id2);
                createIMIDump(dos, target.getAElem(i), incrementIndent(id2));
            }
        }
        // cleanup
        while (currentSourceIndex < srcAVL) {
            mod = true;
            dos.writeBytes(id2 + "->");
            createIMIDump(dos, DMKey.of(tgtAVL), id2);
            dos.writeBytes("\n");
            currentSourceIndex++;
        }
        return mod;
    }

    // Note: 0 means that the objects should NOT be patched.
    // This is used to try and make sane diffs of arrays.
    // Note that this is only a heuristic.
    private static int imiCompatibilityIndex(IRIO source, IRIO target) {
        if (!IRIO.rubyTypeEquals(source, target))
            return 0;
        if (IRIO.rubyEquals(source, target))
            return 0x7FFFFFFF;
        // Particularly common case (cmd params)
        if (source.getType() == '[') {
            int srcAL = source.getALen();
            if (srcAL == target.getALen()) {
                // The Array Compatibility Game Show
                if (srcAL == 0)
                    return 0x7FFFFFFF;
                long points = 0;
                for (int i = 0; i < srcAL; i++)
                    points += imiCompatibilityIndex(source.getAElem(i), target.getAElem(i));
                points /= srcAL;
                return (int) points;
            } else {
                return 0x20000000;
            }
        }
        String[] ivks = source.getIVars();
        if (ivks.length == 0)
            return 0x7FFFFFFF;
        // We have no clue what to do, so now for the IVar Compatibility Game Show!
        // Scoring is between 0 through 0x7FFFFFFF.
        long points = 0;
        for (int i = 0; i < ivks.length; i++) {
            String name = ivks[i];
            IRIO present = source.getIVar(ivks[i]);
            IRIO other = target.getIVar(name);
            if (other != null)
                points += imiCompatibilityIndex(present, other);
        }
        points /= ivks.length;
        return (int) points;
    }

    // assumes the current line has no indent
    private static boolean patchIVs(DataOutputStream dos, IRIO source, IRIO target, String indent) throws IOException {
        boolean important = false;
        boolean oughtToNL = false;
        for (String name : source.getIVars()) {
            IRIO present = source.getIVar(name);
            IRIO other = target.getIVar(name);
            if (other != null) {
                byte[] d = createIMIData(present, other, incrementIndent(indent));
                if (d != null) {
                    important = true;
                    if (oughtToNL) {
                        dos.writeByte('\n');
                        oughtToNL = false;
                    }
                    dos.writeBytes(indent + "=\"");
                    writeIMIStringBody(dos, name.getBytes("UTF-8"), false);
                    dos.write(d);
                }
            } else {
                if (oughtToNL)
                    dos.writeByte('\n');
                dos.writeBytes(indent + "-=");
                writeIMIStringBody(dos, name.getBytes("UTF-8"), false);
                oughtToNL = true;
                important = true;
            }
        }
        // ignore oughtToNL for this
        dos.writeBytes(indent + ";\n");
        return important;
    }

    public static void writeIMIStringBody(DataOutputStream dos, byte[] data, boolean binary) throws IOException {
        //dos.writeByte('\"'); // This is added by the caller.
        for (int i = 0; i < data.length; i++) {
            boolean escape = binary;
            if (data[i] < 32) {
                escape = true;
            } else if (data[i] == '\"') {
                escape = true;
            } else if (data[i] == '\\') {
                escape = true;
            }
            if (escape) {
                String b = Integer.toHexString(data[i] & 0xFF);
                if (b.length() == 1) {
                    b = "\\0" + b;
                } else {
                    b = "\\" + b;
                }
                dos.writeBytes(b);
            } else {
                dos.writeByte(data[i]);
            }
        }
        dos.writeByte('\"');
    }

    // Always ends in a newline
    public static void createIMIDump(DataOutputStream dos, RORIO target, String indent) throws IOException {
        // Retype
        dos.writeBytes("!" + ((char) target.getType()));
        boolean didNLYet = false;
        int idx = 0;
        int alen = 0;
        switch (target.getType()) {
            case 'i':
                dos.writeBytes(Long.toString(target.getFX()) + ";\n");
                return;
            case '0':
            case 'T':
            case 'F':
                break;
            case '}':
                createIMIDump(dos, target.getHashDefVal(), incrementIndent(indent));
            case '{':
                for (DMKey key : target.getHashKeys()) {
                    if (!didNLYet) {
                        dos.writeByte('\n');
                        didNLYet = true;
                    }
                    dos.writeBytes(indent + ">");
                    String nxtIndent = incrementIndent(indent);
                    createIMIDump(dos, key, nxtIndent);
                    dos.writeBytes(nxtIndent);
                    createIMIDump(dos, target.getHashVal(key), nxtIndent);
                }
                break;
            case '[':
                alen = target.getALen();
                dos.writeBytes(Integer.toString(target.getALen()) + ":\n");
                didNLYet = true;
                for (idx = 0; idx < alen; idx++) {
                    String nxtIndent = incrementIndent(indent);
                    dos.writeBytes(indent + "]" + idx + ":");
                    createIMIDump(dos, target.getAElem(idx), nxtIndent);
                }
                break;
            case ':':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.getSymbol().getBytes("UTF-8"), false);
                break;
            case 'u':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.getSymbol().getBytes("UTF-8"), false);
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.getBuffer(), true);
                break;
            case 'o':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.getSymbol().getBytes("UTF-8"), false);
                break;
            case '"':
                // '"' implied
                writeIMIStringBody(dos, target.getBuffer(), false);
                break;
            case 'f':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.getBuffer(), false);
                break;
            case 'l':
                dos.writeByte('\"');
                writeIMIStringBody(dos, target.getBuffer(), true);
                return;
            default:
                throw new IOException("IMI cannot represent this type: " + target.getType() + " (" + ((char) target.getType()) + ")");
        }
        // Add IVars
        for (String s : target.getIVars()) {
            if (!didNLYet) {
                dos.writeByte('\n');
                didNLYet = true;
            }
            dos.writeBytes(indent);
            dos.writeByte('=');
            dos.writeByte('"');
            writeIMIStringBody(dos, s.getBytes("UTF-8"), false);
            createIMIDump(dos, target.getIVar(s), incrementIndent(indent));
        }
        if (didNLYet)
            dos.writeBytes(indent);
        dos.writeBytes(";\n");
    }

    private static String incrementIndent(String indent) {
        String n2 = indent + " ";
        if (n2.endsWith("    "))
            return n2.substring(0, n2.length() - 4) + "\t";
        return n2;
    }

    public static long readIMINumber(InputStream inp, int terminator) throws IOException {
        long val = 0;
        long mul = 1;
        while (true) {
            int p = inp.read();
            boolean ok = false;
            if (p >= '0') {
                if (p <= '9') {
                    val *= 10;
                    val += p - '0';
                    ok = true;
                }
            }
            if (p == '-') {
                mul *= -1;
                ok = true;
            }
            if (p == terminator)
                break;
            if (!ok)
                throw new IOException("Unknown char " + p);
        }
        return val * mul;
    }

    public static byte[] readIMIStringBody(InputStream inp) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int escapeState = 0;
        int escapeHex = 0;
        String hexStr = "0123456789abcdef";
        while (true) {
            int b = inp.read();
            if (b == -1)
                throw new IOException("Early termination during string.");
            int io = hexStr.indexOf(Character.toLowerCase(b));
            switch (escapeState) {
                case 0:
                    if (b == '\"') {
                        return baos.toByteArray();
                    } else if (b == '\\') {
                        escapeState = 1;
                    } else {
                        baos.write(b);
                    }
                    break;
                case 1:
                    if (io == -1)
                        throw new IOException("Cannot have -1 index on first hex digit");
                    escapeHex = io << 4;
                    escapeState = 2;
                    break;
                case 2:
                    if (io == -1)
                        throw new IOException("Cannot have -1 index on second hex digit");
                    escapeHex |= io;
                    baos.write(escapeHex);
                    escapeState = 0;
                    break;
            }
        }
    }

    public static void runIMISegment(InputStream inp, IRIO obj) throws IOException {
        while (true) {
            int cmd = inp.read();
            if (cmd == -1)
                throw new IOException("Early termination in IMI segment.");
            long tmp1 = 0;
            IRIO tmp2 = null;
            byte[] tmp3;
            int newType;
            switch (cmd) {
                case '?':
                    tmp1 = inp.read();
                    if (obj.getType() != tmp1)
                        throw new IOException("Object was expected to be of type " + tmp1 + " - it was " + obj.getType());
                    break;
                case '!':
                    newType = inp.read();
                    if (newType == -1)
                        throw new IOException("Early termination?");
                    // inconsistent formatter madness, just roll with it
                    switch (newType) {
                            case 'i':
                                obj.setFX(readIMINumber(inp, ';'));
                                // yes, do actually return
                                return;
                            case '0':
                                obj.setNull();
                                break;
                            case 'T':
                            case 'F':
                                obj.setBool(newType == 'T');
                                break;
                            case '}':
                                obj.setHashWithDef();
                                runIMISegment(inp, obj.getHashDefVal());
                                break;
                        case '{':
                            obj.setHash();
                                break;
                            case '[':
                                obj.setArray();
                                IntUtils.resizeArrayTo(obj, (int) readIMINumber(inp, ':'));
                                break;
                            case ':':
                                if (inp.read() != '\"')
                                    throw new IOException("Syntax error, symbol not followed by utf8barray");
                                obj.setSymbol(new String(readIMIStringBody(inp), "UTF-8"));
                                break;
                            case 'u':
                                if (inp.read() != '\"')
                                    throw new IOException("Syntax error, u not followed by utf8barray");
                                tmp3 = readIMIStringBody(inp);
                                if (inp.read() != '\"')
                                    throw new IOException("Syntax error, u-sym not followed by utf8barray 2");
                                obj.setUser(new String(tmp3, "UTF-8"), readIMIStringBody(inp));
                                break;
                            case 'o':
                                if (inp.read() != '\"')
                                    throw new IOException("Syntax error, o not followed by utf8barray");
                                obj.setObject(new String(readIMIStringBody(inp), "UTF-8"));
                                break;
                            case '\"':
                                // Force into default encoding, then change contents.
                                obj.setString("");
                                obj.putBuffer(readIMIStringBody(inp));
                                break;
                            case 'f':
                                if (inp.read() != '\"')
                                    throw new IOException("Syntax error, f not followed by utf8barray");
                                obj.setFloat(readIMIStringBody(inp));
                                break;
                            case 'l':
                                if (inp.read() != '\"')
                                    throw new IOException("Syntax error, l not followed by utf8barray");
                                obj.setBignum(readIMIStringBody(inp));
                                break;
                            default:
                                throw new IOException("Don't know how to initialize " + newType);
                    }
                    break;
                case ']':
                    runIMISegment(inp, obj.getAElem((int) readIMINumber(inp, ':')));
                    break;
                case '=':
                    if (inp.read() != '\"') {
                        throw new IOException("Expected quote after -=");
                    } else {
                        String iv = new String(readIMIStringBody(inp), "UTF-8");
                        tmp2 = obj.getIVar(iv);
                        if (tmp2 == null)
                            tmp2 = obj.addIVar(iv);
                        runIMISegment(inp, tmp2);
                    }
                    break;
                case '>':
                    tmp2 = new IRIOGeneric(IDM3Context.Null.DISPOSABLE, StandardCharsets.UTF_8);
                    runIMISegment(inp, tmp2);
                    if (obj.getType() == '[') {
                        if (tmp2.getType() != 'i')
                            throw new IOException("Expected integer index for removal from array");
                        runIMISegment(inp, obj.addAElem((int) tmp2.getFX()));
                    } else {
                        DMKey key = DMKey.of(tmp2);
                        IRIO newObj = obj.getHashVal(key);
                        if (newObj == null)
                            newObj = obj.addHashVal(key);
                        runIMISegment(inp, newObj);
                    }
                    break;
                case '-':
                    switch (inp.read()) {
                        case '=':
                            if (inp.read() != '\"') {
                                throw new IOException("Expected quote after -=");
                            } else {
                                obj.rmIVar(new String(readIMIStringBody(inp), "UTF-8"));
                            }
                            break;
                        case '>':
                            tmp2 = new IRIOGeneric(IDM3Context.Null.DISPOSABLE, StandardCharsets.UTF_8);
                            runIMISegment(inp, tmp2);
                            if (obj.getType() == '[') {
                                if (tmp2.getType() != 'i')
                                    throw new IOException("Expected integer index for removal from array");
                                obj.rmAElem((int) tmp2.getFX());
                            } else {
                                obj.removeHashVal(DMKey.of(tmp2));
                            }
                            break;
                        default:
                            throw new IOException("- followed by invalid char");
                    }
                    break;
                case ';':
                    return;
                case 13:
                case 10:
                case 9:
                case 32:
                    break;
                default:
                    throw new IOException("IMI segment interpreter encountered " + cmd + ", which is unknown.");
            }
        }
    }
}
