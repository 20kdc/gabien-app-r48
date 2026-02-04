/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map2d.events;

import r48.dbs.ValueSyntax;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.ioplus.Reporter;

import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Merges together multiple event accessors.
 * Relies on keys being opaque.
 * All external keys are strings.
 * The first character is a digit ('0'+) that indicates the source,
 * the remainder is an encoded value.
 * Created on May 20th, 2018
 */
public class MergingEventAccess implements IEventAccess {
    private final String tabName;
    private final IEventAccess[] accesses;
    private final int[] accessEVTBases;
    private final int[] eventTypesToAccess;
    private final String[] eventTypes;

    public MergingEventAccess(String panelName, IEventAccess... ac) {
        tabName = panelName;
        accesses = ac;
        LinkedList<String> evts = new LinkedList<String>();
        accessEVTBases = new int[accesses.length];
        int base = 0;
        for (int i = 0; i < accesses.length; i++) {
            accessEVTBases[i] = base;
            for (String s : accesses[i].eventTypes()) {
                evts.add(s);
                base++;
            }
        }
        eventTypes = evts.toArray(new String[0]);
        eventTypesToAccess = new int[eventTypes.length];
        for (int i = 0; i < eventTypesToAccess.length; i++)
            for (int j = 0; j < accessEVTBases.length; j++)
                if (i >= accessEVTBases[j])
                    eventTypesToAccess[i] = j;
    }

    @Override
    public LinkedList<DMKey> getEventKeys() {
        LinkedList<DMKey> newKeys = new LinkedList<>();
        for (int i = 0; i < accesses.length; i++)
            for (DMKey key : accesses[i].getEventKeys())
                newKeys.add(convertEventKey(i, key));
        return newKeys;
    }

    private DMKey convertEventKey(int i, DMKey key) {
        return DMKey.ofStr(((char) ('0' + i)) + ValueSyntax.encode(key));
    }

    @Override
    public IRIO getEvent(DMKey key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEvent(ValueSyntax.decode(ks.substring(1)));
    }

    @Override
    public void delEvent(DMKey key, Reporter reporter) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        accesses[in].delEvent(ValueSyntax.decode(ks.substring(1)), reporter);
    }

    @Override
    public String[] eventTypes() {
        return eventTypes;
    }

    @Override
    public @Nullable DMKey addEvent(@Nullable RORIO eve, int type, Reporter reporter) {
        int accessId = eventTypesToAccess[type];
        IEventAccess iea = accesses[accessId];
        type -= accessEVTBases[accessId];
        DMKey res = iea.addEvent(eve, type, reporter);
        if (res == null)
            return null;
        return convertEventKey(accessId, res);
    }

    @Override
    public @Nullable EventSchema getEventSchema(DMKey key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventSchema(ValueSyntax.decode(ks.substring(1)));
    }

    @Override
    public int getEventTypeFromKey(DMKey key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventTypeFromKey(ValueSyntax.decode(ks.substring(1))) + accessEVTBases[in];
    }

    @Override
    public Consumer<Reporter> hasSync(DMKey key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].hasSync(ValueSyntax.decode(ks.substring(1)));
    }

    @Override
    public String customEventsName() {
        return tabName;
    }

    @Override
    public long getEventX(DMKey a) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventX(ValueSyntax.decode(ks.substring(1)));
    }

    @Override
    public long getEventY(DMKey a) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventY(ValueSyntax.decode(ks.substring(1)));
    }

    @Override
    public void setEventXY(DMKey a, long x, long y, Reporter reporter) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        accesses[in].setEventXY(ValueSyntax.decode(ks.substring(1)), x, y, reporter);
    }

    @Override
    public String getEventName(DMKey a) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventName(ValueSyntax.decode(ks.substring(1)));
    }
}
