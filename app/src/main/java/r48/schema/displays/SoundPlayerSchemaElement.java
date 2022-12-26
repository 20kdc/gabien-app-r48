/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.displays;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import r48.dbs.PathSyntax;
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
    public final String namePath;
    public final @Nullable String volumePath;
    public final @Nullable String tempoPath;
    public final @Nullable String balancePath;

    public SoundPlayerSchemaElement(String pfx, String nP, @Nullable String vP, @Nullable String tP, @Nullable String bP) {
        prefix = pfx;
        namePath = nP;
        volumePath = vP;
        tempoPath = tP;
        balancePath = bP;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, ISchemaHost launcher, SchemaPath path) {
        IRIO nameObj = PathSyntax.parse(target, namePath);
        IRIO tempoObj = tempoPath == null ? null : PathSyntax.parse(target, tempoPath);
        double tempo = 1;
        if (tempoObj != null)
            tempo = tempoObj.getFX() / 100d;
        return UIAudioPlayer.create(prefix + nameObj.decString(), tempo);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // none, informative element only
    }
}
