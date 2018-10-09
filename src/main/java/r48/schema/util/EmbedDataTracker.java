/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.util;

import r48.RubyIO;
import r48.schema.SchemaElement;

import java.util.HashMap;
import java.util.Stack;
import java.util.WeakHashMap;

/**
 * Tracks scroll values and such.
 * Created on October 09, 2018.
 */
public class EmbedDataTracker {
    public WeakHashMap<SchemaPath, HashMap<String, Object>> mapTree = new WeakHashMap<SchemaPath, HashMap<String, Object>>();

    public EmbedDataTracker() {
    }

    public EmbedDataTracker(Stack<SchemaPath> paths, EmbedDataTracker other) {
        for (SchemaPath sp : paths) {
            HashMap<String, Object> sph = other.mapTree.get(sp);
            if (sph != null)
                mapTree.put(sp, new HashMap<String, Object>(sph));
        }
    }

    public void setEmbed(SchemaPath current, SchemaElement source, RubyIO target, String prop, Object val) {
        HashMap<String, Object> hm = mapTree.get(current);
        if (hm == null) {
            hm = new HashMap<String, Object>();
            mapTree.put(current, hm);
        }
        hm.put(source.hashCode() + "/" + target.hashCode() + "/" + prop, val);
    }

    public Object getEmbed(SchemaPath current, SchemaElement source, RubyIO target, String prop, Object def) {
        HashMap<String, Object> hm = mapTree.get(current);
        if (hm == null)
            return def;
        Object o = hm.get(source.hashCode() + "/" + target.hashCode() + "/" + prop);
        if (o == null)
            return def;
        return o;
    }
}