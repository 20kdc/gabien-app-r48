/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import gabien.GaBIEn;
import r48.RubyIO;
import r48.io.data.IRIO;

import java.io.*;
import java.util.LinkedList;

/**
 * Because everybody needs a public domain JSON parser.
 * October 9th, 2017
 */
public class JsonObjectBackend implements IObjectBackend {
    public String root, ext;

    public JsonObjectBackend(String rootPath, String dataExt) {
        root = rootPath;
        ext = dataExt;
    }

    @Override
    public RubyIO loadObjectFromFile(String filename) {
        InputStream inp = null;
        try {
            inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(root + filename + ext));
            LinkedList<String> tokens = new LinkedList<String>();
            Reader r = new InputStreamReader(inp, "UTF-8");
            tokenize(tokens, r);
            inp.close();
            inp = null;
            return loadFromTokens(tokens);
        } catch (Exception e) {
            if (inp != null) {
                try {
                    inp.close();
                } catch (Exception e2) {

                }
            }
            e.printStackTrace();
            return null;
        }
    }

    private RubyIO loadFromTokens(LinkedList<String> tokens) {
        String n = tokens.removeFirst();
        if (n.startsWith("\""))
            return new RubyIO().setString(n.substring(1), true);
        if (n.equals("{")) {
            RubyIO hash = new RubyIO().setHash();
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
                RubyIO key = loadFromTokens(tokens);
                if (!tokens.removeFirst().equals(":"))
                    throw new RuntimeException("Couldn't find KV separator");
                RubyIO val = loadFromTokens(tokens);
                hash.hashVal.put(key, val);
            }
        }
        if (n.equals("[")) {
            LinkedList<RubyIO> array = new LinkedList<RubyIO>();
            // comma policy is very liberal here since it's never ambiguous
            while (true) {
                n = tokens.getFirst();
                if (n.equals(",")) {
                    tokens.removeFirst();
                    continue;
                }
                if (n.equals("]")) {
                    tokens.removeFirst();
                    RubyIO arr = new RubyIO().setNull();
                    arr.type = '[';
                    arr.arrVal = new RubyIO[array.size()];
                    for (int i = 0; i < arr.arrVal.length; i++)
                        arr.arrVal[i] = array.removeFirst();
                    return arr;
                }
                array.add(loadFromTokens(tokens));
            }
        }
        if (n.equals("true"))
            return new RubyIO().setBool(true);
        if (n.equals("false"))
            return new RubyIO().setBool(false);
        if (n.equals("null"))
            return new RubyIO().setNull();
        // Number. Please see "3.10.2. Floating-Point Literals" for an explaination,
        //  and see the relevant notes on parseFloat for why spaces had to be removed during tokenization.
        float f = Float.parseFloat(n);
        if ((((long) f) == f) && (!n.contains(".")))
            return new RubyIO().setFX((long) f);
        RubyIO str = new RubyIO().setString(n, true);
        str.type = 'f';
        return str;
    }

    private void tokenize(LinkedList<String> tokens, Reader r) throws IOException {
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

    private boolean singleCharBreaker(int c) {
        return (c == '[') || (c == ']') || (c == ':') || (c == '{') || (c == '}') || (c == ',');
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) throws IOException {
        OutputStream oup = GaBIEn.getOutFile(PathUtils.autoDetectWindows(root + filename + ext));
        if (oup == null)
            throw new IOException("Unable to open file!");
        DataOutputStream dos = new DataOutputStream(oup);
        saveValue(dos, object);
        dos.close();
    }

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }

    private void saveValue(DataOutputStream dos, IRIO object) throws IOException {
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
                for (IRIO kv : object.getHashKeys()) {
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
