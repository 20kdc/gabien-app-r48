/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.layouts.UIScrollLayout;
import r48.App;
import r48.dbs.RPGCommand;
import r48.io.data.RORIO;

/**
 * UNDERSTAND: These are estimates ONLY, intended to aid in translation.
 * Created 26th October, 2023.
 */
public class TextAnalyzerCommandClassifier implements ICommandClassifier {
    public final ITextAnalyzer analyzer;

    public TextAnalyzerCommandClassifier(ITextAnalyzer inst) {
        this.analyzer = inst;
    }

    @Override
    public String getName(App app) {
        return analyzer.getName(app);
    }

    @Override
    @NonNull
    public Instance instance(App app) {
        final ITextAnalyzer.Instance instance = analyzer.instance(app);
        return new Instance() {
            @Override
            public void setupEditor(@NonNull UIScrollLayout usl, @NonNull Runnable onEdit) {
                instance.setupEditor(usl, onEdit);
            }

            @Override
            public boolean matches(@Nullable RPGCommand target, @Nullable RORIO data) {
                if (target == null)
                    return false;
                if (data == null)
                    return false;
                if (target.textArg == -1)
                    return false;
                return instance.matches(data.getIVar("@parameters").getAElem(target.textArg).decString());
            }
        };
    }
}
