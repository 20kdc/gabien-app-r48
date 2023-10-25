/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.search;

import r48.App;

/**
 * This is definitely a good idea. Yup.
 * Created October 25th, 2023.
 */
public enum BooleanChainOperator {
    And, AndNot, Or, OrNot;

    private BooleanChainOperator() {
    }

    public String getTranslatedName(App app) {
        switch (this) {
        case And:
            return app.t.u.ccs_and;
        case AndNot:
            return app.t.u.ccs_andNot;
        case Or:
            return app.t.u.ccs_or;
        case OrNot:
            return app.t.u.ccs_orNot;
        }
        // is this even possible
        return name();
    }

    public boolean evaluate(boolean old, boolean incoming) {
        switch (this) {
        case And:
            return old && incoming;
        case AndNot:
            return old && !incoming;
        case Or:
            return old || incoming;
        case OrNot:
            return old || !incoming;
        }
        // is this even possible
        return old;
    }
}
