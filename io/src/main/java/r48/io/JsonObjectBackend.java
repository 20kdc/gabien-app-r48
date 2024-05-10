/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import gabien.uslx.vfs.FSBackend;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.io.data.RORIO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Because everybody needs (yet another) public domain JSON parser.
 * October 9th, 2017
 */
public class JsonObjectBackend extends OldObjectBackend<RORIO, IRIO> {
    public String root, ext;
    public final FSBackend fs;

    public JsonObjectBackend(FSBackend fs, String rootPath, String dataExt) {
        this.fs = fs;
        root = rootPath;
        ext = dataExt;
    }

    @Override
    public IRIO newObjectO(String n, @NonNull DMContext context) {
        return new IRIOGeneric(context);
    }

    @Override
    public IRIO loadObjectFromFile(String filename, @NonNull DMContext context) {
        try (InputStream inp = fs.intoPath(root + filename + ext).openRead()) {
            return loadJSONFromStream(context, inp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static IRIO loadJSONFromStream(@NonNull DMContext context, InputStream inp) throws IOException {
        LinkedList<String> tokens = new LinkedList<String>();
        Reader r = new InputStreamReader(inp, StandardCharsets.UTF_8);
        tokenize(tokens, r);
        return loadFromTokens(context, tokens);
    }

    private static IRIO loadFromTokens(@NonNull DMContext context, LinkedList<String> tokens) {
        String n = tokens.removeFirst();
        if (n.startsWith("\""))
            return new IRIOGeneric(context).setString(n.substring(1));
        if (n.equals("{")) {
            IRIO hash = new IRIOGeneric(context).setHash();
            // comma policy is very liberal here since it's never ambiguous
            while (true) {
                n = tokens.getFirst();
                if (n.equals(",")) {
                    tokens.removeFirst();
                    continue;
                }
                if (n.equals("}")) {
                    tokens.removeFirst();
                    return hash;
                }
                IRIO key = loadFromTokens(context, tokens);
                if (!tokens.removeFirst().equals(":"))
                    throw new RuntimeException("Couldn't find KV separator");
                IRIO val = loadFromTokens(context, tokens);
                hash.addHashVal(key.asKey()).setDeepClone(val);
            }
        }
        if (n.equals("[")) {
            IRIO array = new IRIOGeneric(context).setArray();
            // comma policy is very liberal here since it's never ambiguous
            while (true) {
                n = tokens.getFirst();
                if (n.equals(",")) {
                    tokens.removeFirst();
                    continue;
                }
                if (n.equals("]")) {
                    tokens.removeFirst();
                    return array;
                }
                array.addAElem(array.getALen()).setDeepClone(loadFromTokens(context, tokens));
            }
        }
        if (n.equals("true"))
            return new IRIOGeneric(context).setBool(true);
        if (n.equals("false"))
            return new IRIOGeneric(context).setBool(false);
        if (n.equals("null"))
            return new IRIOGeneric(context).setNull();
        // Number. Please see "3.10.2. Floating-Point Literals" for an explaination,
        //  and see the relevant notes on parseFloat for why spaces had to be removed during tokenization.
        float f = Float.parseFloat(n);
        if ((((long) f) == f) && (!n.contains(".")))
            return new IRIOGeneric(context).setFX((long) f);
        IRIOGeneric str = new IRIOGeneric(context);
        str.setFloat(n.getBytes(StandardCharsets.UTF_8));
        return str;
    }

    private static void tokenize(LinkedList<String> tokens, Reader r) throws IOException {
        int c = r.read();
        while (c != -1) {
            boolean next = false;
            if (singleCharBreaker(c)) {
                tokens.add(Character.toString((char) c));
                next = true;
            } else if (((c >= '0') && (c <= '9')) || (c == '-')) {
                String s = "";
                while (((c >= '0') && (c <= '9')) || (c == '.') || (c == '-') || (c == '+') || (c == 'e') || (c == 'E')) {
                    s += (char) c;
                    c = r.read();
                    while (c < 33) {
                        if (c == -1)
                            break;
                        c = r.read();
                    }
                }
                tokens.add(s);
            } else if (c == '"') {
                String s = JsonStringIO.readString(r);
                // The '\"' is just a prefix, as readString returns the unprefixed string contents
                tokens.add("\"" + s);
                next = true;
            } else if (c < 33) {
                // Whitespace...
                next = true;
            } else {
                // just be insanely liberal about what is accepted here
                String s = "";
                while (c >= 33) {
                    if (singleCharBreaker(c))
                        break;
                    s += (char) c;
                    c = r.read(); // if -1, this will still work fine)
                }
                tokens.add(s);
            }
            if (next)
                c = r.read();
        }
    }

    private static boolean singleCharBreaker(int c) {
        return (c == '[') || (c == ']') || (c == ':') || (c == '{') || (c == '}') || (c == ',');
    }

    @Override
    public void saveObjectToFile(String filename, RORIO object) throws IOException {
        try (OutputStream oup = fs.intoPath(root + filename + ext).openWrite()) {
            saveJSONToStream(oup, object);
        }
    }

    public static void saveJSONToStream(OutputStream oup, RORIO object) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(oup)) {
            saveValue(dos, object);
        }
    }
    private static void saveValue(DataOutputStream dos, RORIO object) throws IOException {
        boolean first = true; // for collections
        int alen; // for arrays
        switch (object.getType()) {
            case 'T':
                dos.writeBytes("true");
                break;
            case 'F':
                dos.writeBytes("false");
                break;
            case '0':
                dos.writeBytes("null");
                break;
            case 'i':
                dos.writeBytes(Long.toString(object.getFX()));
                break;
            case 'f':
                // Eep.
                dos.writeBytes(object.decString());
                break;
            case '"':
                dos.writeBytes(JsonStringIO.getStringAsASCII(object.decString()));
                break;
            case '{':
                dos.write('{');
                for (DMKey kv : object.getHashKeys()) {
                    if (!first)
                        dos.write(',');
                    saveValue(dos, kv);
                    dos.write(':');
                    saveValue(dos, object.getHashVal(kv));
                    first = false;
                }
                dos.write('}');
                break;
            case '[':
                dos.write('[');
                alen = object.getALen();
                for (int i = 0; i < alen; i++) {
                    if (!first)
                        dos.write(',');
                    saveValue(dos, object.getAElem(i));
                    first = false;
                }
                dos.write(']');
                break;
            default:
                throw new RuntimeException("Cannot convert OT: " + object.getType());
        }
    }
}
