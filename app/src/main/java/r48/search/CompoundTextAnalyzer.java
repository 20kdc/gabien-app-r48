/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import r48.R48;

/**
 * Used as the root text analyzer.
 * Created October 25th, 2023.
 */
public enum CompoundTextAnalyzer implements ITextAnalyzer {
    I;

    @Override
    public String getName(R48 app) {
        return app.t.u.ccs_textAnalyzer;
    }

    @Override
    public Instance instance(R48 app) {
        final ITextAnalyzer[] ents = app.textAnalyzers.toArray(new ITextAnalyzer[0]);
        return new CCCI(app, ents, ents[0]);
    }

    private final class CCCI extends CompoundClassifierish<ITextAnalyzer, ITextAnalyzer.Instance> implements ITextAnalyzer.Instance {
        private CCCI(R48 ac, ITextAnalyzer[] e, ITextAnalyzer de) {
            super(ac, e, de, false, BooleanChainOperator.Or);
        }

        @Override
        public boolean matches(String text) {
            boolean value = true;
            boolean first = true;
            for (Entry ent : entries) {
                boolean m = ((ITextAnalyzer.Instance) ent.cInstance).matches(text);
                if (first)
                    value = m;
                else
                    value = ent.chain.evaluate(value, m);
                first = false;
            }
            return value;
        }
    }
}
