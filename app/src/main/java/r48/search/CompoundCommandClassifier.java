/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import org.eclipse.jdt.annotation.Nullable;

import r48.App;
import r48.dbs.RPGCommand;
import r48.io.data.RORIO;

/**
 * Used as the root command classifier.
 * Created October 25th, 2023.
 */
public enum CompoundCommandClassifier implements ICommandClassifier {
    I;

    @Override
    public String getName(App app) {
        return "Compound classifier [INTERNAL]";
    }

    @Override
    public Instance instance(App app) {
        final ICommandClassifier[] ents = app.cmdClassifiers.toArray(new ICommandClassifier[0]);
        ICommandClassifier de = app.commandTags.get("translatable");        
        return new CCCI(app, ents, de);
    }

    private final class CCCI extends CompoundClassifierish<ICommandClassifier, ICommandClassifier.Instance> implements ICommandClassifier.Instance {
        private CCCI(App ac, ICommandClassifier[] e, ICommandClassifier de) {
            super(ac, e, de, true, BooleanChainOperator.And);
        }

        @Override
        public boolean matches(@Nullable RPGCommand target, @Nullable RORIO data) {
            boolean value = true;
            boolean first = true;
            for (Entry ent : entries) {
                boolean m = ((ICommandClassifier.Instance) ent.cInstance).matches(target, data);
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
