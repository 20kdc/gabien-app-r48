/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos2;

/**
 * Created on October 10, 2018.
 */
public class GP2Cell {
    public final GP2CellType type;
    public final GP2Timeline[] fields;

    public GP2Cell(GP2CellType ty) {
        type = ty;
        fields = new GP2Timeline[ty.fieldTypes.length];
        for (int i = 0; i < fields.length; i++)
            fields[i] = new GP2Timeline(new GP2Timeline.TimePoint(i, ty.defaults[i]));
    }

}
