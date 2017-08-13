/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema.util;

import r48.AppMain;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.WeakHashMap;

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
    public SchemaPath parent;

    // If editor is null, targetElement must be null, and vice versa.
    // Host may be there or not.
    public SchemaElement editor;
    public RubyIO targetElement;

    // Should only ever be set to true by tagSEMonitor. Implies editor and target.
    public boolean monitorsSubelements = false;

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

    public WeakHashMap<ISchemaHost, HashMap<EmbedDataKey, Double>> embedData = new WeakHashMap<ISchemaHost, HashMap<EmbedDataKey, Double>>();

    private SchemaPath() {
    }

    // The basic constructor.
    public SchemaPath(SchemaElement heldElement, RubyIO target, ISchemaHost launcher) {
        lastArrayIndex = target;
        hrIndex = AppMain.objectDB.getIdByObject(target);
        if (hrIndex == null)
            hrIndex = "AnonObject";
        editor = heldElement;
        targetElement = target;
    }

    // Used for default value setup bootstrapping.
    private SchemaPath(RubyIO target, RubyIO lai) {
        lastArrayIndex = lai;
        hrIndex = AppMain.objectDB.getIdByObject(target);
        if (hrIndex == null)
            hrIndex = "AnonObject";
    }

    public static RubyIO createDefaultValue(SchemaElement ise, RubyIO arrayIndex) {
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

    public SchemaPath findHighestSubwatcher() {
        SchemaPath mod = this;
        SchemaPath root = this;
        while (root.parent != null) {
            root = root.parent;
            if (root.monitorsSubelements)
                mod = root;
        }
        return mod;
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

    // Similar to findBack, but includes the current node too.
    // Specifically for scroll value storage.
    public SchemaPath findLast() {
        if (hasBeenUsed)
            return this;
        return findBack();
    }

    // -- Important Stuff (always used) --

    public SchemaPath arrayHashIndex(RubyIO index, String indexS) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.lastArrayIndex = index;
        sp.hrIndex = indexS;
        return sp;
    }

    // -- Display Stuff (used in buildHoldingEditor) --

    public SchemaPath newWindow(SchemaElement heldElement, RubyIO target) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.lastArrayIndex = lastArrayIndex;
        sp.editor = heldElement;
        sp.targetElement = target;
        return sp;
    }

    // Not so much used, and quite unimportant

    public SchemaPath otherIndex(String index) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.lastArrayIndex = lastArrayIndex;
        sp.hrIndex = index;
        return sp;
    }

    public SchemaPath tagSEMonitor(RubyIO target, SchemaElement ise) {
        SchemaPath sp = new SchemaPath();
        sp.parent = this;
        sp.targetElement = target;
        sp.editor = ise;
        sp.monitorsSubelements = true;
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
        AppMain.objectDB.objectRootModified(p.targetElement, this);
        // Run an autocorrect on just about everything.
        // Could cause a lagspike, but this is *after* modification anyway,
        //  and worth it for the consistency benefits.
        // Not having a broken game > lag.
        // --Later-- HAHA NO I WAS WRONG THIS IS LAGGY.
        // Unfortunately it's still laggy no matter what I do.
        // I suspect this is related to whatever is making IDEA LAG SO FLIPPING MUCH at present.
        // --Later Still--
        // Ok, so it's somehow a bit laggy even when contained.
        // Something to do with it being exponential on new objects.
        // runs through -> object changes -> begins running through again?
        // Ok, no, that's not it either...?
        // It's not even recursion. At all.
        // -- EVEN LATER --
        // Ok, so what was going wrong was that the notification handlers were kind of...
        // ...massively performance-draining when spammed over and over again. Oops.
        // As it is, notification handlers are always stuff that can be delayed until later,
        // so it's being delayed to EOF. Using a HashSet.

        if (!modifyVal) {
            SchemaPath sw = findHighestSubwatcher();
            if (sw.editor != null)
                sw.editor.modifyVal(sw.targetElement, sw, false);
        }
    }

    // If this is true, a temp dialog (unique UIElement) is in use and thus this can't be cloned.
    public boolean hasTempDialog() {
        if (editor instanceof TempDialogSchemaChoice)
            return true;
        if (parent != null)
            return parent.hasTempDialog();
        return false;
    }

    public HashMap<EmbedDataKey, Double> getEmbedMap(ISchemaHost host) {
        HashMap<EmbedDataKey, Double> map = embedData.get(host);
        if (map == null) {
            map = new HashMap<EmbedDataKey, Double>();
            embedData.put(host, map);
        }
        return map;
    }

    public double getEmbedSP(ISchemaHost host, EmbedDataKey myKey) {
        Double d = getEmbedMap(host).get(myKey);
        if (d == null)
            return 0;
        return d;
    }

    public static class EmbedDataKey {
        public final SchemaElement key1;
        public final RubyIO key2;

        public EmbedDataKey(SchemaElement key1, RubyIO key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EmbedDataKey))
                return false;
            EmbedDataKey edk = (EmbedDataKey) o;
            return (edk.key1 == key1) && (edk.key2 == key2);
        }

        @Override
        public int hashCode() {
            return key1.hashCode() + key2.hashCode();
        }
    }
}
