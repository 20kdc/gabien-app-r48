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
import r48.schema.SchemaElementIOP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
    public final Page pathRoot;
    public final int windowDepth, depth;

    /**
     * Root handle of the object.
     * Responsible for managing modification notifications and saving.
     */
    public final @NonNull ObjectRootHandle root;

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

    public final HashMap<String, SchemaElementIOP> contextualSchemas = new HashMap<>();

    protected SchemaPath(@NonNull SchemaPath sp, DMKey lastArrayIndex, String hrIndex) {
        parent = sp;
        pathRoot = sp.pathRoot;
        depth = sp.depth + 1;
        windowDepth = parent.windowDepth + ((this instanceof Page) ? 1 : 0);
        root = sp.root;
        this.lastArrayIndex = lastArrayIndex;
        this.hrIndex = hrIndex;
        contextualSchemas.putAll(sp.contextualSchemas);
    }

    // Internal root constructor. Only call from Page constructor.
    protected SchemaPath(@NonNull ObjectRootHandle root) {
        parent = null;
        pathRoot = (SchemaPath.Page) this;
        depth = 0;
        windowDepth = 1;
        this.root = root;
        hrIndex = root.toString();
        lastArrayIndex = null;
    }

    protected static @NonNull SchemaElementIOP verifyRootHasSchema(@NonNull ObjectRootHandle root) {
        SchemaElementIOP se = root.rootSchema;
        if (se == null)
            throw new NullPointerException("Gah! Creating SchemaPath to " + root + " ; but it has no schema!");
        return se;
    }
    protected static @NonNull SchemaElementIOP weDoNotTrustOurCallers(@Nullable SchemaElementIOP he, @NonNull ObjectRootHandle root) {
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
    public static void setDefaultValue(@NonNull IRIO target, @NonNull SchemaElementIOP ise, @Nullable DMKey arrayIndex) {
        setDefaultValue(target, ise, arrayIndex, null);
    }

    /**
     * Sets a default value without (by itself) triggering Schema-level side-effects.
     * It is best to think of the schema use here as an implementation detail.
     * Still, the 'adjustments' parameter allows for setting disambiguators and things like that.
     * It provides a Runnable which runs the autocorrect, so you don't have to manually autocorrect the whole thing.
     */
    public static void setDefaultValue(@NonNull IRIO target, @NonNull SchemaElementIOP ise, @Nullable DMKey arrayIndex, @Nullable Consumer<Runnable> adjustments) {
        ObjectRootHandle dvRoot = new ObjectRootHandle.Isolated(ise, target, "setDefaultValue");
        SchemaPath adjuster = new Page(ise, dvRoot).arrayHashIndex(arrayIndex, "AnonObject");
        ise.modifyVal(target, adjuster, true);
        if (adjustments != null)
            adjustments.accept(() -> ise.modifyVal(target, adjuster, false));
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

    /**
     * Going upward, find the first 'window' SchemaPath.
     * Returns null if none found.
     */
    public @NonNull Page findFirstEditable() {
        if (parent == null) {
            // this should be impossible *frowns*
            return pathRoot;
        }
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
        return new SchemaPath(this, index, indexS);
    }

    // -- Display Stuff (used in buildHoldingEditor) --

    public Page newWindow(SchemaElementIOP heldElement, IRIO target) {
        return new Page(this, heldElement, target, lastArrayIndex, null);
    }

    // Not so much used, and quite unimportant

    public SchemaPath otherIndex(String index) {
        return new SchemaPath(this, lastArrayIndex, index);
    }

    public SchemaPath tagSEMonitor(@NonNull IRIO target, SchemaElementIOP ise, boolean upwards) {
        if (upwards) {
            // This is for DisambiguatorSchemaElement to make sure the entire structure containing a disambiguator gets the tag.
            // This is so that edits to the thing being disambiguated on get caught properly.
            // (To be honest, I have long since forgotten how this actually worked.)
            SchemaPath spp = this;
            SchemaPath sppLast = this;
            while ((spp instanceof Page) && target == ((Page) spp).targetElement) {
                sppLast = spp;
                spp = spp.parent;
                if (spp == null)
                    break;
            }
            if (sppLast != spp)
                sppLast.monitorsSubelements = true;
        }
        Page sp = new Page(this, ise, target, lastArrayIndex, null);
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
        if (sw instanceof Page)
            ((Page) sw).editor.modifyVal(((Page) sw).targetElement, sw, false);
    }

    // If this is true, a temp dialog (unique UIElement) is in use and thus this can't be cloned.
    public boolean hasTempDialog() {
        if (parent != null)
            return parent.hasTempDialog();
        return false;
    }

    /**
     * Attaches a contextual schema element, which will be used when relevant. 
     */
    public SchemaPath contextSchema(String contextName, SchemaElementIOP enumSchemaElement) {
        SchemaPath sp = new SchemaPath(this, lastArrayIndex, null);
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
        return parentSuffix;
    }

    /**
     * Attempt to get a window SchemaPath for a target object via a set of intermediate objects.
     * The set of intermediate objects must contain the target.
     * This is the core of what will probably be a key R48 maintainability feature going forward.
     * In particular, it helps keep Schema and Java logic decoupled.
     */
    public @Nullable SchemaPath traceRoute(RORIO tracertTarget, Set<RORIO> expected) {
        Page sp = findFirstEditable();
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
            Page oPath = vPath.findFirstEditable();

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
        Page sp = findFirstEditable();
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

    /**
     * Specifically for those schema paths with actual pages in them.
     * Created 4th February, 2026 (just)
     */
    public static class Page extends SchemaPath {
        /**
         * Editor element.
         */
        public final @NonNull SchemaElementIOP editor;

        /**
         * Target element.
         */
        public final @NonNull IRIO targetElement;

        private Page(@NonNull SchemaPath sp, @NonNull SchemaElementIOP editor, @NonNull IRIO targetElement, @Nullable DMKey lastArrayIndex, @Nullable String hrIndex) {
            super(sp, lastArrayIndex, hrIndex);

            this.editor = editor;
            this.targetElement = targetElement;
        }

        // The basic root constructor.
        public Page(@NonNull ObjectRootHandle root) {
            this(verifyRootHasSchema(root), root);
        }

        // The advanced root constructor.
        public Page(@NonNull SchemaElementIOP heldElement, @NonNull ObjectRootHandle root) {
            super(root);
            heldElement = weDoNotTrustOurCallers(heldElement, root);
            editor = heldElement;
            targetElement = root.getObject();
        }

        @Override
        public Page findFirstEditable() {
            return this;
        }

        @Override
        public boolean hasTempDialog() {
            if (editor.isTempDialog())
                return true;
            return super.hasTempDialog();
        }

        @Override
        public String windowTitleSuffix() {
            String parentSuffix = super.windowTitleSuffix();
            String attemptedEditorSuffix = editor.windowTitleSuffix(this);
            if (attemptedEditorSuffix != null)
                return parentSuffix + ":" + attemptedEditorSuffix;
            return parentSuffix;
        }
    }
}
