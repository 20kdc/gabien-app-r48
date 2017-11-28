/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import r48.RubyIO;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Support for the mostly ASCII 'IMI' format
 * Note that this does NOT, as a rule, use UTF-anything.
 * Byte array format:
 * Starts with " for clarity.
 * There are three special characters:
 *  ": Ends the array
 *  \: Two hex digits follow - "0x" is prepended for parsing.
 * This is meant to imitate the look of a string.
 * Commands:
 * (32, 13, 10):  NOP
 * ?[num]: : Enforces that the type is as given *by colon terminated integer*, on pain of patch error.
 *           The reason it's done this way is because IMI's pretending to be human-readable.
 * ![char] : Reset current object, then change type *by character-byte*.
 *          (fields are given as appropriate:
 *           i: Semicolon-terminated number which ends object, implemented by the T ending object
 *           ": Byte array (with no starting '"'!)
 *           ':' / 'o': byte array with starting " for sym - syms are encoded in UTF-8.
 *
 *          )
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
    public static byte[] createIMIData(RubyIO source, RubyIO target, String indent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        if (!RubyIO.rubyTypeEquals(source, target)) {
            createIMIDump(dos, target, indent);
            return baos.toByteArray();
        }
        if (RubyIO.rubyEquals(source, target))
            return null;
        String id2 = incrementIndent(indent);
        // NOTE: Most types are not patchable. Don't try to patch them.
        boolean flagMod = false; // did enough change to bother returning the result
        switch (source.type) {
            case 'o':
                if (!source.symVal.equals(target.symVal)) {
                    // unreachable with rubyTypeEquals but just in case
                    flagMod = true;
                    createIMIDump(dos, target, indent);
                } else {
                    // Objects are "equivalent", run patchIVs()
                    dos.writeBytes("?" + target.type + ":\n");
                    flagMod |= patchIVs(dos, source, target, id2);
                }
                break;
            case 'u':
                if (!source.symVal.equals(target.symVal)) {
                    // unreachable with rubyTypeEquals but just in case
                    flagMod = true;
                    createIMIDump(dos, target, indent);
                } else {
                    // check data
                    boolean dataEq = true;
                    if (source.userVal.length == target.userVal.length) {
                        for (int i = 0; i < source.userVal.length; i++) {
                            if (source.userVal[i] != target.userVal[i]) {
                                dataEq = true;
                                break;
                            }
                        }
                    } else {
                        dataEq = false;
                    }
                    if (dataEq) {
                        dos.writeBytes("?" + target.type + ":\n");
                        flagMod |= patchIVs(dos, source, target, id2);
                    } else {
                        flagMod = true;
                        createIMIDump(dos, target, indent);
                    }
                }
                break;
            case '[':
                dos.writeBytes("?" + target.type + ":\n");
                flagMod |= patchArray(dos, source, target, id2);
                flagMod |= patchIVs(dos, source, target, id2);
                break;
            case '{':
                dos.writeBytes("?" + target.type + ":\n");
                for (RubyIO rk : source.hashVal.keySet())
                    if (target.getHashVal(rk) == null) {
                        dos.writeBytes(id2 + "->");
                        createIMIDump(dos, target, id2);
                        flagMod = true;
                    }
                for (Map.Entry<RubyIO, RubyIO> me : target.hashVal.entrySet()) {
                    RubyIO rio = source.getHashVal(me.getKey());
                    if (rio == null) {
                        flagMod = true;
                        dos.writeBytes(id2 + ">");
                        createIMIDump(dos, me.getKey(), id2);
                        dos.writeBytes(id2);
                        createIMIDump(dos, me.getValue(), id2);
                    } else {
                        byte[] diff = createIMIData(rio, me.getValue(), id2);
                        if (diff != null) {
                            flagMod = true;
                            dos.writeBytes(id2 + ">");
                            createIMIDump(dos, me.getKey(), id2);
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

    private static boolean patchArray(DataOutputStream dos, RubyIO source, RubyIO target, String id2) throws IOException {
        int[] mappingsTarget = new int[target.arrVal.length];
        int lowestUsedIndex = -1;
        for (int i = 0; i < mappingsTarget.length; i++)
            mappingsTarget[i] = -1;
        for (int i = 0; i < source.arrVal.length; i++) {
            int bestCompatibility = -1;
            int bestCompatibilityIndex = -1;
            int thresholdIncompatible = 0x40000000;
            for (int j = lowestUsedIndex + 1; j < target.arrVal.length; j++) {
                int compat = imiCompatibilityIndex(source.arrVal[i], target.arrVal[j]);
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
        for (int i = 0; i < target.arrVal.length; i++) {
            if (mappingsTarget[i] != -1) {
                if (mappingsTarget[i] < currentSourceIndex)
                    throw new IOException("Impossible mapping!");
                while (mappingsTarget[i] != currentSourceIndex) {
                    mod = true;
                    dos.writeBytes(id2 + "->");
                    createIMIDump(dos, new RubyIO().setFX(i), id2);
                    currentSourceIndex++;
                }
                byte[] patch = createIMIData(source.arrVal[currentSourceIndex], target.arrVal[i], incrementIndent(id2));
                if (patch != null) {
                    mod = true;
                    dos.writeBytes(id2 + "]" + i + ":");
                    dos.write(patch);
                }
                currentSourceIndex++;
            } else {
                mod = true;
                dos.writeBytes(id2 + ">");
                createIMIDump(dos, new RubyIO().setFX(i), id2);
                dos.writeBytes(id2);
                createIMIDump(dos, target.arrVal[i], incrementIndent(id2));
            }
        }
        // cleanup
        while (currentSourceIndex < source.arrVal.length) {
            mod = true;
            dos.writeBytes(id2 + "->");
            createIMIDump(dos, new RubyIO().setFX(target.arrVal.length), id2);
            dos.writeBytes("\n");
            currentSourceIndex++;
        }
        return mod;
    }

    // Note: 0 means that the objects should NOT be patched.
    // This is used to try and make sane diffs of arrays.
    // Note that this is only a heuristic.
    private static int imiCompatibilityIndex(RubyIO source, RubyIO target) {
        if (!RubyIO.rubyTypeEquals(source, target))
            return 0;
        if (RubyIO.rubyEquals(source, target))
            return 0x7FFFFFFF;
        // Particularly common case (cmd params)
        if (source.type == '[') {
            if (source.arrVal.length == target.arrVal.length) {
                // The Array Compatibility Game Show
                if (source.arrVal.length == 0)
                    return 0x7FFFFFFF;
                long points = 0;
                for (int i = 0; i < source.arrVal.length; i++)
                    points += imiCompatibilityIndex(source.arrVal[i], target.arrVal[i]);
                points /= source.arrVal.length;
                return (int) points;
            } else {
                return 0x20000000;
            }
        }
        if (source.iVarKeys == null)
            return 0x7FFFFFFF;
        if (source.iVarKeys.length == 0)
            return 0x7FFFFFFF;
        // We have no clue what to do, so now for the IVar Compatibility Game Show!
        // Scoring is between 0 through 0x7FFFFFFF.
        long points = 0;
        for (int i = 0; i < source.iVarKeys.length; i++) {
            String name = source.iVarKeys[i];
            RubyIO present = source.iVarVals[i];
            RubyIO other = target.getInstVarBySymbol(name);
            if (other != null)
                points += imiCompatibilityIndex(present, other);
        }
        points /= source.iVarKeys.length;
        return (int) points;
    }

    // assumes the current line has no indent
    private static boolean patchIVs(DataOutputStream dos, RubyIO source, RubyIO target, String indent) throws IOException {
        boolean important = false;
        boolean oughtToNL = false;
        if (source.iVarKeys != null) {
            for (int i = 0; i < source.iVarKeys.length; i++) {
                String name = source.iVarKeys[i];
                RubyIO present = source.iVarVals[i];
                RubyIO other = target.getInstVarBySymbol(name);
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
        }
        // ignore oughtToNL for this
        dos.writeBytes(";\n");
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
    public static void createIMIDump(DataOutputStream dos, RubyIO target, String indent) throws IOException {
        // Retype
        dos.writeBytes("!" + ((char) target.type));
        boolean didNLYet = false;
        int idx = 0;
        switch (target.type) {
            case 'i':
                dos.writeBytes(Long.toString(target.fixnumVal) + ";\n");
                return;
            case '0':
            case 'T':
            case 'F':
                break;
            case '{':
                for (Map.Entry<RubyIO, RubyIO> e : target.hashVal.entrySet()) {
                    if (!didNLYet) {
                        dos.writeByte('\n');
                        didNLYet = true;
                    }
                    dos.writeBytes(indent + ">");
                    createIMIDump(dos, e.getKey(), incrementIndent(indent));
                    dos.writeBytes(incrementIndent(indent));
                    createIMIDump(dos, e.getValue(), incrementIndent(indent));
                }
                break;
            case '[':
                for (RubyIO rio : target.arrVal) {
                    if (!didNLYet) {
                        dos.writeByte('\n');
                        didNLYet = true;
                    }
                    dos.writeBytes(indent + ">");
                    String nxtIndent = incrementIndent(indent);
                    createIMIDump(dos, new RubyIO().setFX(idx++), nxtIndent);
                    dos.writeBytes(nxtIndent);
                    createIMIDump(dos, rio, nxtIndent);
                }
                break;
            case ':':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.symVal.getBytes("UTF-8"), false);
                break;
            case 'u':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.symVal.getBytes("UTF-8"), false);
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.userVal, true);
                break;
            case 'o':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.symVal.getBytes("UTF-8"), false);
                break;
            case '"':
                // '"' implied
                writeIMIStringBody(dos, target.strVal, false);
                break;
            case 'f':
                dos.writeBytes("\"");
                writeIMIStringBody(dos, target.strVal, false);
                break;
            default:
                throw new IOException("IMI cannot represent this type: " + target.type + " (" + ((char) target.type) + ")");
        }
        // Add IVars
        if (target.iVarKeys != null) {
            int ivi = 0;
            for (String s : target.iVarKeys) {
                if (!didNLYet) {
                    dos.writeByte('\n');
                    didNLYet = true;
                }
                dos.writeBytes(indent);
                dos.writeByte('=');
                dos.writeByte('"');
                writeIMIStringBody(dos, s.getBytes("UTF-8"), false);
                createIMIDump(dos, target.iVarVals[ivi++], incrementIndent(indent));
            }
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
}
