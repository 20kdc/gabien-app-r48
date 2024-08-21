/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.util;

import r48.dbs.ObjectRootHandle;
import r48.dbs.PathSyntax;
import r48.io.data.DMKey;
import r48.io.data.DMPath;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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
    public final SchemaPath parent;
    public final int windowDepth, depth;

    /**
     * Root handle of the object.
     * Responsible for managing modification notifications and saving.
     */
    public final @NonNull ObjectRootHandle root;

    // If editor is null, targetElement must be null, and vice versa.
    // Host may be there or not.
    public final @Nullable SchemaElement editor;
    public final @Nullable IRIO targetElement;

    // Should only ever be set to true by tagSEMonitor.
    // Implies editor and target.
    public boolean monitorsSubelements = false;

    // At the root object, this is guaranteed to be null.
    // Otherwise, it should propagate whenever unchanged.
    // lastArray does a similar thing, except it points to the object whose targetElement is the array/hash itself.
    // This allows "inside" elements to cause consistency checks.
    public final @Nullable DMKey lastArrayIndex;

    // Null for "no human readable index here".
    // The root is the intended ObjectID index, but this is a visual property only.
    public final String hrIndex;

    public final HashMap<String, SchemaElement> contextualSchemas = new HashMap<String, SchemaElement>();

    private SchemaPath(@NonNull SchemaPath sp, SchemaElement editor, IRIO targetElement, DMKey lastArrayIndex, String hrIndex) {
        parent = sp;
        depth = sp.depth + 1;
        windowDepth = parent.windowDepth + (editor != null ? 1 : 0);
        root = sp.root;
        this.editor = editor;
        this.targetElement = targetElement;
        this.lastArrayIndex = lastArrayIndex;
        this.hrIndex = hrIndex;
        contextualSchemas.putAll(sp.contextualSchemas);
    }

    // The basic root constructor.
    public SchemaPath(@NonNull ObjectRootHandle root) {
        this(verifyRootHasSchema(root), root);
    }

    // The advanced root constructor.
    public SchemaPath(@NonNull SchemaElement heldElement, @NonNull ObjectRootHandle root) {
        heldElement = weDoNotTrustOurCallers(heldElement, root);
        parent = null;
        depth = 0;
        windowDepth = 1;
        this.root = root;
        hrIndex = root.toString();
        editor = heldElement;
        targetElement = root.getObject();
        lastArrayIndex = null;
    }

    private static @NonNull SchemaElement verifyRootHasSchema(@NonNull ObjectRootHandle root) {
        SchemaElement se = root.rootSchema;
        if (se == null)
            throw new NullPointerException("Gah! Creating SchemaPath to " + root + " ; but it has no schema!");
        return se;
    }
    private static @NonNull SchemaElement weDoNotTrustOurCallers(@Nullable SchemaElement he, @NonNull ObjectRootHandle root) {
        if (he == null) {
            System.err.println("Gah! Creating SchemaPath to " + root + " with null SchemaElement.");
            return verifyRootHasSchema(root);
        }
        return he;
    }

    /**
     * Sets a default value without (by itself) triggering Schema-level side-effects.
     * It is best to think of the schema use here as an implementation detail.
     */
    public static void setDefaultValue(@NonNull IRIO target, @NonNull SchemaElement ise, @Nullable DMKey arrayIndex) {
        ObjectRootHandle dvRoot = new ObjectRootHandle.Isolated(ise, target, "setDefaultValue");
        ise.modifyVal(target, new SchemaPath(ise, dvRoot).arrayHashIndex(arrayIndex, "AnonObject"), true);
    }

    public String toString() {
        SchemaPath measuring = this;
        LinkedList<SchemaPath> pathOrder = new LinkedList<>();
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
        LinkedList<SchemaPath> pathOrder = new LinkedList<>();
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

    /**
     * Going upward, find the first 'window' SchemaPath.
     * Returns null if none found.
     */
    public @Nullable SchemaPath findFirstEditable() {
        if (editor != null)
            return this;
        if (parent == null)
            return null;
        return parent.findFirstEditable();
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

    // -- Important Stuff (always used) --

    public SchemaPath arrayHashIndex(DMKey index, String indexS) {
        return new SchemaPath(this, null, null, index, indexS);
    }

    // -- Display Stuff (used in buildHoldingEditor) --

    public SchemaPath newWindow(SchemaElement heldElement, IRIO target) {
        return new SchemaPath(this, heldElement, target, lastArrayIndex, null);
    }

    // Not so much used, and quite unimportant

    public SchemaPath otherIndex(String index) {
        return new SchemaPath(this, null, null, lastArrayIndex, index);
    }

    public SchemaPath tagSEMonitor(IRIO target, SchemaElement ise, boolean upwards) {
        if (upwards) {
            // This is for DisambiguatorSchemaElement to make sure the entire structure containing a disambiguator gets the tag.
            // This is so that edits to the thing being disambiguated on get caught properly.
            SchemaPath spp = this;
            SchemaPath sppLast = this;
            while ((target == spp.targetElement) || (target == null)) {
                sppLast = spp;
                spp = spp.parent;
                if (spp == null)
                    break;
            }
            if (sppLast != spp)
                sppLast.monitorsSubelements = true;
        }
        SchemaPath sp = new SchemaPath(this, ise, target, lastArrayIndex, null);
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

        if (!modifyVal)
            pokeHighestSubwatcherEditor();

        // Attempt to set a "changed flag".
        // This will also nudge the observers.
        root.objectRootModified(this);
    }

    public void pokeHighestSubwatcherEditor() {
        SchemaPath sw = findHighestSubwatcher();
        if (sw.editor != null)
            sw.editor.modifyVal(sw.targetElement, sw, false);
    }

    // If this is true, a temp dialog (unique UIElement) is in use and thus this can't be cloned.
    public boolean hasTempDialog() {
        if (editor instanceof TempDialogSchemaChoice)
            return true;
        if (parent != null)
            return parent.hasTempDialog();
        return false;
    }

    /**
     * Attaches a contextual schema element, which will be used when relevant. 
     */
    public SchemaPath contextSchema(String contextName, SchemaElement enumSchemaElement) {
        SchemaPath sp = new SchemaPath(this, null, null, lastArrayIndex, null);
        sp.contextualSchemas.put(contextName, enumSchemaElement);
        return sp;
    }

    /**
     * Returns the window title suffix, if any.
     */
    public @NonNull String windowTitleSuffix() {
        String parentSuffix = "";
        if (parent != null)
            parentSuffix = parent.windowTitleSuffix();
        if (editor != null) {
            String attemptedEditorSuffix = editor.windowTitleSuffix(this);
            if (attemptedEditorSuffix != null)
                return parentSuffix + ":" + attemptedEditorSuffix;
        }
        return parentSuffix;
    }

    /**
     * Attempt to get a window SchemaPath for a target object via a set of intermediate objects.
     * The set of intermediate objects must contain the target.
     * This is the core of what will probably be a key R48 maintainability feature going forward.
     * In particular, it helps keep Schema and Java logic decoupled.
     */
    public @Nullable SchemaPath traceRoute(RORIO tracertTarget, Set<RORIO> expected) {
        SchemaPath sp = findFirstEditable();
        if (sp == null)
            return null;
        // RPG_RT.ldb
        // @common_events:{11@list]5
        AtomicReference<SchemaPath> path = new AtomicReference<>();
        // 0: target not found
        // 1: target found but not as window
        // 2: target found as window
        AtomicInteger escalation = new AtomicInteger();
        sp.editor.visit(sp.targetElement, sp, (vElement, vTarget, vPath) -> {
            // Make sure we don't go off-track or else we'll visit the entire database.
            if (!expected.contains(vTarget))
                return false;
            SchemaPath oPath = vPath.findFirstEditable();
            if (oPath == null)
                return true;

            // Determine our escalation
            int ourEscalation = 0;
            if (oPath.editor.declaresSelfEditorOf(oPath.targetElement, tracertTarget)) {
                // declared by window
                ourEscalation = 2;
            } else if (vElement.declaresSelfEditorOf(vTarget, tracertTarget)) {
                // declared by invocation
                ourEscalation = 1;
            }

            // Main check...
            SchemaPath checkAgainst = path.get();
            int theirEscalation = escalation.get();
            if (checkAgainst == null) {
                path.set(oPath);
                escalation.set(ourEscalation);
                return true;
            }
            // Sort by best fit: (X > Y) followed by (X < Y)
            // ---
            if (ourEscalation > theirEscalation) {
                path.set(oPath);
                escalation.set(ourEscalation);
                return true;
            } else if (ourEscalation < theirEscalation) {
                // we do still want to seek out possible alternate methods of accessing the target
                return true;
            }
            // ---
            if (ourEscalation == 2) {
                // minimize depth in fully escalated nodes to prevent going into subwindows
                if (oPath.windowDepth < checkAgainst.windowDepth) {
                    path.set(oPath);
                    escalation.set(ourEscalation);
                    return true;
                } else if (oPath.windowDepth > checkAgainst.windowDepth) {
                    return true;
                }
                // ---
                if (oPath.depth < checkAgainst.depth) {
                    path.set(oPath);
                    escalation.set(ourEscalation);
                    return true;
                }
                // else unnecessary as this is last clause
                // ---
            } else {
                // both are not tracertTarget, maximize depth
                if (oPath.windowDepth > checkAgainst.windowDepth) {
                    path.set(oPath);
                    escalation.set(ourEscalation);
                    return true;
                } else if (oPath.windowDepth < checkAgainst.windowDepth) {
                    return true;
                }
                // ---
                if (oPath.depth > checkAgainst.depth) {
                    path.set(oPath);
                    escalation.set(ourEscalation);
                    return true;
                }
                // else unnecessary as this is last clause
                // ---
            }
            return true;
        }, true);
        return path.get();
    }

    /**
     * traceRoute for a given DMPath target.
     */
    public @Nullable SchemaPath tracePathRoute(@NonNull DMPath path) {
        SchemaPath sp = findFirstEditable();
        if (sp == null) {
            System.err.println("tracePathRoute failed: findFirstEditable failed");
            return null;
        }
        RORIO res = path.getRO(sp.targetElement);
        if (res == null) {
            System.err.println("tracePathRoute failed: path failed");
            return null;
        }
        HashSet<RORIO> mainSet = new PathSyntax(path).traceRO(sp.targetElement);
        // make sure these are here; traceRO misses the first
        mainSet.add(sp.targetElement);
        mainSet.add(res);
        return sp.traceRoute(res, mainSet);
    }
}
