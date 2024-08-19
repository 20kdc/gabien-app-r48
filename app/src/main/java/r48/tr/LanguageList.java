/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tr;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import datum.DatumDecodingVisitor;
import datum.DatumKVDVisitor;
import datum.DatumReaderTokenSource;
import datum.DatumSrcLoc;
import datum.DatumVisitor;
import gabien.GaBIEn;

/**
 * Intended to pull even more functionality out of TXDB.
 * Specifically, this provides the immutable parts of the static bits of TXDB.
 * Created 27th February 2023.
 */
public class LanguageList {
    /**
     * Default language ID. This is the fallback if the current language is not defined.
     */
    public static final String defaultLang = "eng";
    /**
     * Default language for legacy gettext-style translation APIs.
     */
    public static final String hardcodedLang = "eng";
    /**
     * Language the default/fallback help text is considered to be written in.
     */
    public static final String helpLang = "eng";

    private static final LangInfo[] languages = createLanguageList();

    private static void addFn(LinkedList<LangInfo> tgt, String fn) {
        try (InputStreamReader isr = GaBIEn.getTextResource(fn)) {
            if (isr == null)
                return;
            new DatumReaderTokenSource(fn, isr).visit(new DatumKVDVisitor() {
                @Override
                public DatumVisitor handle(String key) {
                    // sure!
                    return new DatumDecodingVisitor() {
                        @Override
                        public void visitEnd(DatumSrcLoc srcLoc) {
                        }
                        
                        @Override
                        public void visitTree(Object obj, DatumSrcLoc srcLoc) {
                            if (obj instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Object> lo = (List<Object>) obj;
                                if (lo.size() != 2)
                                    throw new RuntimeException("LangInfo struct needs 2 strings, check upstream terms/index.txt");
                                String sa = (String) lo.get(0);
                                String sb = (String) lo.get(1);
                                tgt.add(new LangInfo(key, sa, sb));
                            }
                        }
                    };
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static LangInfo[] createLanguageList() {
        final LinkedList<LangInfo> languageLL = new LinkedList<LangInfo>();
        addFn(languageLL, "terms/index.scm");
        addFn(languageLL, "terms/index.aux.scm");
        return languageLL.toArray(new LangInfo[0]);
    }

    public static LangInfo getNextLanguage(String language) {
        boolean nextIsNext = false;
        for (int i = 0; i < languages.length; i++) {
            if (nextIsNext)
                return languages[i];
            if (languages[i].id.equals(language))
                nextIsNext = true;
        }
        return languages[0];
    }

    public static @Nullable LangInfo getLangInfo(String language) {
        for (int i = 0; i < languages.length; i++)
            if (languages[i].id.equals(language))
                return languages[i];
        return null;
    }

    public static final class LangInfo {
        public final String id, globalName, localName;
        public LangInfo(String i, String gn, String ln) {
            id = i;
            globalName = gn;
            localName = ln;
        }
    }
}
