/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.events;

import r48.RubyIO;
import r48.dbs.ValueSyntax;

import java.util.LinkedList;

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
    public LinkedList<RubyIO> getEventKeys() {
        LinkedList<RubyIO> newKeys = new LinkedList<RubyIO>();
        for (int i = 0; i < accesses.length; i++)
            for (RubyIO key : accesses[i].getEventKeys())
                newKeys.add(convertEventKey(i, key));
        return newKeys;
    }

    private RubyIO convertEventKey(int i, RubyIO key) {
        return new RubyIO().setString(((char) ('0' + i)) + ValueSyntax.encode(key, true), true);
    }

    @Override
    public RubyIO getEvent(RubyIO key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEvent(ValueSyntax.decode(ks.substring(1), true));
    }

    @Override
    public void delEvent(RubyIO key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        accesses[in].delEvent(ValueSyntax.decode(ks.substring(1), true));
    }

    @Override
    public String[] eventTypes() {
        return eventTypes;
    }

    @Override
    public RubyIO addEvent(RubyIO eve, int type) {
        int accessId = eventTypesToAccess[type];
        IEventAccess iea = accesses[accessId];
        type -= accessEVTBases[accessId];
        RubyIO res = iea.addEvent(eve, type);
        if (res == null)
            return null;
        return convertEventKey(accessId, res);
    }

    @Override
    public String[] getEventSchema(RubyIO key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventSchema(ValueSyntax.decode(ks.substring(1), true));
    }

    @Override
    public int getEventType(RubyIO key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventType(ValueSyntax.decode(ks.substring(1), true)) + accessEVTBases[in];
    }

    @Override
    public Runnable hasSync(RubyIO key) {
        String ks = key.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].hasSync(ValueSyntax.decode(ks.substring(1), true));
    }

    @Override
    public String customEventsName() {
        return tabName;
    }

    @Override
    public long getEventX(RubyIO a) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventX(ValueSyntax.decode(ks.substring(1), true));
    }

    @Override
    public long getEventY(RubyIO a) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventY(ValueSyntax.decode(ks.substring(1), true));
    }

    @Override
    public void setEventXY(RubyIO a, long x, long y) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        accesses[in].setEventXY(ValueSyntax.decode(ks.substring(1), true), x, y);
    }

    @Override
    public String getEventName(RubyIO a) {
        String ks = a.decString();
        int in = ks.charAt(0) - '0';
        return accesses[in].getEventName(ValueSyntax.decode(ks.substring(1), true));
    }
}