/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.minivm.fn;

import java.util.List;

import r48.App;
import r48.minivm.MVMU;
import r48.minivm.harvester.Defun;
import r48.minivm.harvester.Example;
import r48.minivm.harvester.Help;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.SubwindowSchemaElement;
import r48.schema.UIOverrideSchemaElement;
import r48.schema.WithDefaultSchemaElement;
import r48.schema.displays.LabelSchemaElement;
import r48.schema.specialized.TenthRuleSchemaElement;
import r48.tr.TrPage.FF0;
import r48.tr.TrPage.FF1;

/**
 * MiniVM standard library.
 * Created 21st August 2024.
 */
public class MVMSDBElementsLibrary extends App.Svc {
    public MVMSDBElementsLibrary(App app) {
        super(app);
    }

    @Defun(n = "se-new-button", r = 2)
    @Help("Creates a button. A0: a 1-parameter name routine for the button's text, A1: a 3-parameter lambda (target launcher path).")
    public SchemaElement newButton(FF1 text, MVMFn fn) {
        return new TenthRuleSchemaElement(app, text, fn);
    }

    @Defun(n = "se-new-subwindow", r = 1)
    @Help("Creates a subwindow element. A0: interior schema element A1: optional: a 1-parameter name routine for the button's text")
    public SchemaElement newSubwindow(Object se, FF1 fn) {
        if (fn == null)
            return new SubwindowSchemaElement(coerceToElement(app, se));
        return new SubwindowSchemaElement(coerceToElement(app, se), (irio) -> fn.r(irio));
    }

    @Defun(n = "se-new-with-default", r = 2)
    @Help("Creates a with-default element. A0: interior schema element A1: default")
    public SchemaElement newWithDefault(Object se, Object fn) {
        return new WithDefaultSchemaElement(coerceToElement(app, se), MVMDMLibrary.dmKeyify(fn));
    }

    @Defun(n = "se-new-ui-override", r = 2)
    @Help("Creates a UI override element. A0: interior schema element A1: ui schema element")
    public SchemaElement newUIO(Object se1, Object se2) {
        return new UIOverrideSchemaElement(coerceToElement(app, se1), coerceToElement(app, se2));
    }

    @Defun(n = "se-new-label", r = 1)
    @Help("Creates a label element. A0: text")
    @Example("(ui-test-schema (se-new-label (define-tr test \"HI\")))")
    public SchemaElement newUILabel(FF0 se1) {
        return new LabelSchemaElement(app, se1);
    }

    @Defun(n = "se-new-aggregate", r = 0)
    @Help("Creates a new aggregate from the given elements.")
    public SchemaElement newAggregate(SchemaElement... elements) {
        return new AggregateSchemaElement(app, elements);
    }

    @Defun(n = "se-new-aggregate-list", r = 0)
    @Help("Creates a new aggregate from the given list of elements.")
    public SchemaElement newAggregate(List<SchemaElement> elements) {
        return new AggregateSchemaElement(app, elements.toArray(new SchemaElement[0]));
    }

    public static SchemaElement coerceToElement(App app, Object elm) {
        if (elm instanceof SchemaElement)
            return (SchemaElement) elm;
        return app.sdb.getSDBEntry(MVMU.coerceToString(elm));
    }
}
