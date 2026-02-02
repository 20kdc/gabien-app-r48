/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.displays;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import r48.R48;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.AppUI;
import r48.ui.audioplayer.UIAudioPlayer;

/**
 * Sound player element
 * Created on 2nd August 2022
 */
public class SoundPlayerSchemaElement extends SchemaElement.Leaf {
    public final String prefix;
    public final PathSyntax namePath;
    public final @Nullable PathSyntax volumePath;
    public final @Nullable PathSyntax tempoPath;
    public final @Nullable PathSyntax balancePath;

    public SoundPlayerSchemaElement(R48 app, String pfx, PathSyntax nP, @Nullable PathSyntax vP, @Nullable PathSyntax tP, @Nullable PathSyntax bP) {
        super(app);
        prefix = pfx;
        namePath = nP;
        volumePath = vP;
        tempoPath = tP;
        balancePath = bP;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
        final AppUI U = launcher.getAppUI();
        RORIO nameObj = namePath.getRO(target);
        RORIO tempoObj = tempoPath == null ? null : tempoPath.getRO(target);
        double tempo = 1;
        if (tempoObj != null)
            tempo = tempoObj.getFX() / 100d;
        return UIAudioPlayer.create(U, prefix + nameObj.decString(), tempo);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // none, informative element only
    }
}
