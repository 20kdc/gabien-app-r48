/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.util;

import r48.AppMain;
import r48.RubyIO;
import r48.schema.ISchemaElement;

import java.util.LinkedList;

/**
 * Generic schema path object used to keep references to things being edited in play,
 * and allow "go to the parent object" functionality.
 * <p/>
 * These have a dual purpose, and due to that,
 * the paths created can differ for, say, SubwindowSchemaElement
 * (...which has no reason to create a newWindow element for modifyDefaultVal, but has to for actual editing UI building)
 * <p/>
 * One of these should be created on array indexes, or when a navigation event occurs.
 * If it's solely for the array index data purpose, then the editor element should be null -
 * this makes navigation slightly more user-friendly.
 * Created on 12/29/16.
 */
public class SchemaPath {
    public SchemaPath parent, lastArray;

    // If editor is null, targetElement must be null, and vice versa.
    public ISchemaElement editor;
    public ISchemaHost host;
    public RubyIO targetElement;
    // At the root object, this is guaranteed to be the object itself.
    // Otherwise, it should propagate whenever unchanged.
    // lastArray does a similar thing, except it points to the object whose targetElement is the array/hash itself.
    // This allows "inside" elements to cause consistency checks.
    public RubyIO lastArrayIndex;

    // Null for "no human readable index here".
    // The root is always the intended ObjectID index.
    public String hrIndex;

    // ISchemaHost has to set this to true for the breadcrumbs to work properly
    public boolean hasBeenUsed = false;

    // This is solely for use by SchemaHostImpl,
    //  to save scroll values.
    protected double scrollValue = 0.0d;

    private SchemaPath() {
    }

    // The basic constructor.
    public SchemaPath(ISchemaElement heldElement, RubyIO target, ISchemaHost launcher) {
        lastArrayIndex = target;
        hrIndex = AppMain.objectDB.getIdByObject(target);
        if (hrIndex == null)
            hrIndex = "AnonObject";
        editor = heldElement;
        host = launcher;
        targetElement = target;
    }

    // Used for default value setup bootstrapping.
    private SchemaPath(RubyIO target, RubyIO lai) {
        lastArrayIndex = lai;
        hrIndex = AppMain.objectDB.getIdByObject(target);
        if (hrIndex == null)
            hrIndex = "AnonObject";
    }

    public static RubyIO createDefaultValue(ISchemaElement ise, RubyIO arrayIndex) {
        RubyIO rio = new RubyIO();
        ise.modifyVal(rio, new SchemaPath(rio, arrayIndex), true);
        return rio;
    }

    public String toString() {
        SchemaPath measuring = this;
        LinkedList<SchemaPath> pathOrder = new LinkedList<SchemaPath>();
        while (measuring != null) {
            pathOrder.addFirst(measuring);
            measuring = measuring.parent;
        }
        String str = "";
        for (SchemaPath sp : pathOrder)
            if (sp.hrIndex != null)
                str += sp.hrIndex;
        return str;
    }

    public String toStringMissingRoot() {
        SchemaPath measuring = this;
        LinkedList<SchemaPath> pathOrder = new LinkedList<SchemaPath>();
        while (measuring != null) {
            pathOrder.addFirst(measuring);
            measuring = measuring.parent;
        }
        pathOrder.removeFirst();
        String str = "";
        for (SchemaPath sp : pathOrder)
            if (sp.hrIndex != null)
                str += sp.hrIndex;
        return str;
    }

    public SchemaPath findRoot() {
        SchemaPath root = this;
        while (root.parent != null)
            root = root.parent;
        return root;
    }

    // Cheat used to trace 'back' properly.
    // Used by Enum too, again to trace 'back'.
    public SchemaPath findBack() {
        SchemaPath root = this;
        while (root.parent != null) {
            root = root.parent;
            if (root.hasBeenUsed)
                return root;
        }
        return root;
    }

    // -- Important Stuff (always used) --

    public SchemaPath arrayHashIndex(RubyIO index, String indexS) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.lastArrayIndex = index;
        sp.lastArray = lastArray;
        sp.hrIndex = indexS;
        return sp;
    }

    // -- Display Stuff (used in buildHoldingEditor) --

    public SchemaPath newWindow(ISchemaElement heldElement, RubyIO target, ISchemaHost launcher) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.lastArrayIndex = lastArrayIndex;
        sp.lastArray = lastArray;
        sp.editor = heldElement;
        sp.host = launcher;
        sp.targetElement = target;
        return sp;
    }

    // Not so much used, and quite unimportant

    public SchemaPath otherIndex(String index) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.lastArrayIndex = lastArrayIndex;
        sp.lastArray = lastArray;
        sp.hrIndex = index;
        return sp;
    }

    // Always used at the beginning of array/hash to make absolutely sure there is an object arrayHashIndex can latch onto.
    public SchemaPath arrayEntry(RubyIO target, ISchemaElement monitor) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.editor = monitor;
        sp.lastArrayIndex = target;
        sp.lastArray = sp;
        sp.targetElement = target;
        sp.hrIndex = null;
        return sp;
    }

    // Used to remove some quirkiness from the system.
    // New rule: Call changeOccurred.
    // Notably, this method being used properly
    //  may allow multi-window support in SCHEMA.
    // Emphasis on the "may", though.
    // "modifyVal" should only be true if being called from modifyVal.
    public void changeOccurred(boolean modifyVal) {
        SchemaPath p = findRoot();
        // Attempt to set a "changed flag".
        // This will also nudge the observers.
        AppMain.objectDB.objectRootModified(p.targetElement);
        // Run an autocorrect on just about everything.
        // Could cause a lagspike, but this is *after* modification anyway,
        //  and worth it for the consistency benefits.
        // Not having a broken game > lag.
        if (!modifyVal)
            if (p.editor != null)
                p.editor.modifyVal(p.targetElement, p, false);
    }
}
