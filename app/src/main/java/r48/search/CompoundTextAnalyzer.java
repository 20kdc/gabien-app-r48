/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import r48.App;

/**
 * Used as the root text analyzer.
 * Created October 25th, 2023.
 */
public class CompoundTextAnalyzer extends App.Svc implements ITextAnalyzer {
    public CompoundTextAnalyzer(App ac) {
        super(ac);
    }

    @Override
    public String getName() {
        return "Compound text analyzer [INTERNAL]";
    }

    @Override
    public Instance instance() {
        final ITextAnalyzer[] ents = app.textAnalyzers.toArray(new ITextAnalyzer[0]);
        return new CCCI(app, ents, ents[0]);
    }

    private final class CCCI extends CompoundClassifierish<ITextAnalyzer, ITextAnalyzer.Instance> implements ITextAnalyzer.Instance {
        private CCCI(App ac, ITextAnalyzer[] e, ITextAnalyzer de) {
            super(ac, e, de, true);
        }

        @Override
        public boolean matches(String text) {
            boolean value = true;
            for (Entry ent : entries)
                value = ent.chain.evaluate(value, ((ITextAnalyzer.Instance) ent.cInstance).matches(text));
            return value;
        }
    }
}
