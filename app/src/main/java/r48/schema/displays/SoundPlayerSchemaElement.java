/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.displays;

import gabien.ui.UIElement;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.audioplayer.UIAudioPlayer;

/**
 * Sound player element
 * Created on 2nd August 2022
 */
public class SoundPlayerSchemaElement extends SchemaElement {
    public final String prefix;

    public SoundPlayerSchemaElement(String pfx) {
        prefix = pfx;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        return UIAudioPlayer.create(prefix + target.decString());
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // none, informative element only
    }
}
