/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.io;

import gabien.GaBIEn;
import gabienapp.Application;
import r48.RubyIO;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;

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
            inp = GaBIEn.getFile(root + filename + ext);
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
            return new RubyIO().setString(n.substring(1));
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
        RubyIO str = new RubyIO().setString(n);
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
                // String
                String s = "\"";
                c = r.read();
                while (c != -1) {
                    if (c == '"')
                        break;
                    if (c == '\\') {
                        c = r.read();
                        if (c == -1)
                            break;
                        if (c == 'u') {
                            int v = 0;
                            for (int i = 0; i < 4; i++) {
                                c = r.read();
                                v = (v << 4) | handleHexDig(c);
                                if (c == -1)
                                    break;
                            }
                            s += (char) v;
                        } else if (c == '"') {
                            s += "\"";
                        } else if (c == '\\') {
                            s += "\\";
                        } else if (c == '/') {
                            s += "/";
                        } else if (c == 'b') {
                            s += "\b";
                        } else if (c == 'f') {
                            s += "\f";
                        } else if (c == 'n') {
                            s += "\n";
                        } else if (c == 'r') {
                            s += "\r";
                        } else if (c == 't') {
                            s += "\t";
                        } else {
                            throw new IOException("Unknown escape " + c);
                        }
                    } else {
                        s += (char) c;
                    }
                    c = r.read();
                }
                if (c == -1)
                    throw new IOException("String terminated too early");
                tokens.add(s);
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

    private int handleHexDig(int c) throws IOException {
        if (c == '0')
            return 0;
        if (c == '1')
            return 1;
        if (c == '2')
            return 2;
        if (c == '3')
            return 3;
        if (c == '4')
            return 4;
        if (c == '5')
            return 5;
        if (c == '6')
            return 6;
        if (c == '7')
            return 7;
        if (c == '8')
            return 8;
        if (c == '9')
            return 9;
        if ((c == 'A') || (c == 'a'))
            return 10;
        if ((c == 'B') || (c == 'b'))
            return 11;
        if ((c == 'C') || (c == 'c'))
            return 12;
        if ((c == 'D') || (c == 'd'))
            return 13;
        if ((c == 'E') || (c == 'e'))
            return 14;
        if ((c == 'F') || (c == 'f'))
            return 15;
        throw new IOException("Unknown hex char");
    }

    @Override
    public void saveObjectToFile(String filename, RubyIO object) throws IOException {
        OutputStream oup = GaBIEn.getOutFile(Application.autoDetectWindows(root + filename + ext));
        if (oup == null)
            throw new IOException("Unable to open file!");
        DataOutputStream dos = new DataOutputStream(oup);
        saveValue(dos, object);
        dos.close();
    }

    private void saveValue(DataOutputStream dos, RubyIO object) throws IOException {
        boolean first = true; // for collections
        switch (object.type) {
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
                dos.writeBytes(Long.toString(object.fixnumVal));
                break;
            case 'f':
                // Eep.
                dos.writeBytes(object.decString());
                break;
            case '"':
                // Unlike other backends, this has a definitive encoding which cannot change, in theory.
                // In this case system encoding is actually ignored in favour of Java encoding (as it's the same as JSON)
                dos.write('\"');
                for (char c : object.decString().toCharArray()) {
                    // can't let any messups happen
                    if ((c < 32) || (c > 126)) {
                        String pad4 = Integer.toHexString(c);
                        while (pad4.length() < 4)
                            pad4 = "0" + pad4;
                        dos.writeBytes("\\u" + pad4);
                    } else if ((c == '\"') || (c == '\\')) {
                        dos.write('\\');
                        dos.write(c);
                    } else {
                        dos.write(c);
                    }
                }
                dos.write('\"');
                break;
            case '{':
                dos.write('{');
                for (Map.Entry<RubyIO, RubyIO> kv : object.hashVal.entrySet()) {
                    if (!first)
                        dos.write(',');
                    saveValue(dos, kv.getKey());
                    dos.write(':');
                    saveValue(dos, kv.getValue());
                    first = false;
                }
                dos.write('}');
                break;
            case '[':
                dos.write('[');
                for (RubyIO rio : object.arrVal) {
                    if (!first)
                        dos.write(',');
                    saveValue(dos, rio);
                    first = false;
                }
                dos.write(']');
                break;
            default:
                throw new RuntimeException("Cannot convert OT: " + object.type);
        }
    }
}
