/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.ioplus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import r48.io.data.IDM3Data;

/**
 *              [THE TIME MACHINE]
 * This code is at the core of the undo/redo system.
 * Recording is broken into 'sessions', and sessions are broken into 'cycles'.
 * A cycle is a frame, basically, but let's call it a cycle in case it changes.
 * Each cycle, if nothing has changed since last cycle, and if there's anything in the recording buffer, an undo point is created. 
 *
 * Created 10th May, 2024.
 */
public final class TimeMachine {
    public final TimeMachine.Host host;

    public static final int MAX_STEPS = 4;

    /**
     * Undo stack. First is considered top-of-stack.
     */
    private final LinkedList<Recording> undoStack = new LinkedList<>();

    /**
     * Redo stack. First is considered top-of-stack.
     */
    private final LinkedList<Recording> redoStack = new LinkedList<>();

    /**
     * Changes are recorded here.
     */
    private Recording recording = new Recording();

    /**
     * Fresh objects are recorded here.
     */
    private final HashSet<IDM3Data> fresh = new HashSet<>();

    private boolean hasRecordBeenCalledThisCycle = false;

    public TimeMachine(Host host) {
        this.host = host;
    }

    /**
     * Records into the time machine.
     */
    public void record(IDM3Data data, TimeMachineChangeSource src) {
        hasRecordBeenCalledThisCycle = true;
        if (!recording.data.containsKey(data))
            recording.data.put(data, data.saveState());
        recording.sources.add(src);
    }

    /**
     * Marks fresh data. Fresh data objects get marked clean but don't 'count'.
     */
    public void recordFresh(IDM3Data irioData) {
        fresh.add(irioData);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public int undoSnapshots() {
        return undoStack.size();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public int redoSnapshots() {
        return redoStack.size();
    }

    private void markFreshObjectsClean() {
        // right, you're all Officially Initialized now
        for (IDM3Data f : fresh)
            f.trackingMarkClean();
        fresh.clear();
    }

    /**
     * Auto-creates undo points.
     */
    public void doCycle() {
        // clean all data to ensure anything new will be caught
        recording.cleanAll();
        markFreshObjectsClean();
        // continue...
        if (!hasRecordBeenCalledThisCycle) {
            // data is in a stable state, commit if there's anything there
            if (!recording.isEmpty()) {
                System.err.println("TimeMachine: COMMIT");
                undoStack.addFirst(recording);
                while (undoStack.size() > MAX_STEPS)
                    undoStack.removeLast();
                recording = new Recording();
                redoStack.clear();
            }
        }
        // new cycle...
        hasRecordBeenCalledThisCycle = false;
    }

    /**
     * Undo everything to after the last thing that can be 'officially' undone.
     */
    private void revertToRecording(HashSet<TimeMachineChangeSource> sourceTrack) {
        markFreshObjectsClean();
        Recording tmp = recording;
        recording = new Recording();
        tmp.invoke(sourceTrack);
        if (!recording.isEmpty())
            throw new RuntimeException("State reversion caused recordings");
    }

    /**
     * Actually undo.
     */
    public void undo() {
        System.err.println("TimeMachine: UNDO");
        HashSet<TimeMachineChangeSource> sourceTrack = new HashSet<TimeMachineChangeSource>();
        revertToRecording(sourceTrack);
        recording = undoStack.removeFirst();
        // prepare redo from all the stuff that is about to be reverted
        redoStack.addFirst(recording.createCounterRecording());
        while (redoStack.size() > MAX_STEPS)
            redoStack.removeLast();
        // and revert it
        revertToRecording(sourceTrack);
        updateRestOfWorld(sourceTrack);
    }

    /**
     * Actually redo.
     */
    public void redo() {
        System.err.println("TimeMachine: REDO");
        HashSet<TimeMachineChangeSource> sourceTrack = new HashSet<TimeMachineChangeSource>();
        revertToRecording(sourceTrack);
        recording = redoStack.removeFirst();
        // prepare undo from all the stuff that is about to be advanced
        undoStack.addFirst(recording.createCounterRecording());
        // and revert it
        revertToRecording(sourceTrack);
        updateRestOfWorld(sourceTrack);
    }

    /**
     * Clears the undo/redo buffers.
     */
    public void clearUndoRedo() {
        undoStack.clear();
        redoStack.clear();
    }

    private void updateRestOfWorld(HashSet<TimeMachineChangeSource> sources) {
        for (TimeMachineChangeSource tmcs : sources)
            tmcs.onTimeTravel();
        host.timeMachineHostOnTimeTravel();
    }

    private static class Recording {
        final HashMap<IDM3Data, Runnable> data = new HashMap<>();
        final HashSet<TimeMachineChangeSource> sources = new HashSet<>();

        // be sure to add sources!
        void invoke(HashSet<TimeMachineChangeSource> sourceTrack) {
            sourceTrack.addAll(sources);
            for (Map.Entry<IDM3Data, Runnable> r : data.entrySet()) {
                r.getValue().run();
                r.getKey().trackingMarkClean();
            }
        }

        /**
         * Creates a current snapshot of all data objects this recording contains (for redo).
         */
        Recording createCounterRecording() {
            Recording r = new Recording();
            r.sources.addAll(sources);
            for (Map.Entry<IDM3Data, Runnable> e : data.entrySet())
                r.data.put(e.getKey(), e.getKey().saveState());
            return r;
        }

        /**
         * Marks everything clean so that it will make noise later.
         */
        void cleanAll() {
            for (IDM3Data k : data.keySet())
                k.trackingMarkClean();
        }

        public boolean isEmpty() {
            return data.isEmpty();
        }
    }

    /**
     * Host proxy for TimeMachine.
     */
    public static interface Host {
        /**
         * Should do things like kicking all dictionaries.
         */
        void timeMachineHostOnTimeTravel();
    }
}
