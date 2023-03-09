/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tr;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import gabien.GaBIEn;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;

/**
 * Class that contains an instance of a dynamic translation database.
 * This is to try and figure out a plan to deprecate parts of TXDB.
 * Extracted on 27th February 2023.
 */
public class Translator implements ITranslator {
    private static HashMap<String, Context> contexts = new HashMap<>();
    public final String langID;
    public Translator(String langID) {
        this.langID = langID;
    }

    private Context ensureContext(String context) {
        Context ctx = contexts.get(context);
        if (ctx == null) {
            ctx = new Context();
            contexts.put(context, ctx);
        }
        return ctx;
    }

    @Override
    public String tr(String context, String text) {
        return ensureContext(context).get(text);
    }

    @Override
    public boolean has(String context, String text) {
        return ensureContext(context).has(text);
    }

    public void put(String context, String text, String res) {
        ensureContext(context).put(text, res);
    }

    @Override
    public void dump(String fnPrefix, String ctxPrefix) {
        PrintStream psA = null;
        try {
            psA = new PrintStream(GaBIEn.getOutFile(fnPrefix + langID + ".txt"), false, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        LinkedList<String> ctxs = new LinkedList<String>(contexts.keySet());
        Collections.sort(ctxs);
        for (String ctxK : ctxs) {
            String ctxKA = ctxK + "/";
            if (ctxKA.startsWith(ctxPrefix)) {
                String adjustedKP = ctxKA.substring(ctxPrefix.length());
                Context ctx = contexts.get(ctxK);
                LinkedList<String> keys = new LinkedList<>(ctx.union);
                Collections.sort(keys);
                for (String key : keys) {
                    psA.println("x \"" + adjustedKP + key + "\"");
                    if (ctx.has(key)) {
                        psA.println("y \"" + ctx.get(key) + "\"");
                    } else {
                        psA.println(" TODO");
                        psA.println("y \"" + key + "\"");
                    }
                }
            }
        }
        psA.close();
    }

    @Override
    public void read(String fn, String pfx) {
        DBLoader.readFile(null, fn, new LangLoadDatabase(pfx));
    }

    private class LangLoadDatabase implements IDatabase {
        String target = "";

        String impliedPrefix;

        public LangLoadDatabase(String s) {
            impliedPrefix = s;
        }

        @Override
        public void newObj(int objId, String objName) throws IOException {

        }

        @Override
        public void execCmd(char c, String[] args) throws IOException {
            if (c == 'x')
                target = impliedPrefix + mergeArgs(args);
            if (c == 'y') {
                int splitIdx = target.indexOf('/');
                String key = target.substring(splitIdx + 1);
                String ctx = target.substring(0, splitIdx);
                put(ctx, key, mergeArgs(args));
            }
        }

        private String mergeArgs(String[] args) throws IOException {
            if (args.length != 1)
                throw new IOException("Expected only one argument per line!");
            return args[0];
        }
    }

    private static class Context {
        final HashMap<String, String> hm = new HashMap<>();
        final HashSet<String> union = new HashSet<>();

        void put(String text, String newText) {
            hm.put(text, newText);
            union.add(text);
        }

        String get(String text) {
            String res = hm.get(text);
            if (res != null)
                return res;
            // oops
            union.add(text);
            return ":NT:" + text;
        }

        boolean has(String text) {
            return hm.containsKey(text);
        }
    }
}
