/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.IntUtils;
import r48.io.data.*;
import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DM2Optional;
import r48.io.data.obj.IRIOFixedObject;
import r48.io.r2k.R2kUtil;
import r48.io.r2k.chunks.IR2kInterpretable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * What is this again?
 * Created on 31/05/17.
 */
public class EventCommand extends IRIOFixedObject implements IR2kInterpretable {
    @DM2FXOBinding("@code")
    public IRIOFixnum code;

    @DM2FXOBinding("@indent")
    public IRIOFixnum indent;

    @DM2FXOBinding("@parameters")
    public ParameterArray parameters;

    @DM2Optional @DM2FXOBinding("@move_commands")
    public IRIOFixedArray<MoveCommand> moveCommands;

    public EventCommand(DM2Context ctx) {
        super(ctx, "RPG::EventCommand");
    }

    @Override
    public IRIO addIVar(String sym) {
        if (sym.equals("@code"))
            return code = new IRIOFixnum(context, 0);
        if (sym.equals("@indent"))
            return indent = new IRIOFixnum(context, 0);
        if (sym.equals("@parameters"))
            return parameters = new ParameterArray(dm2Ctx);
        if (sym.equals("@move_commands"))
            return moveCommands = new IRIOFixedArray<MoveCommand>(context) {
                @Override
                public MoveCommand newValue() {
                    return new MoveCommand(dm2Ctx);
                }
            };
        return null;
    }

    @Override
    public void importData(InputStream bais) throws IOException {
        code.val = R2kUtil.readLcfVLI(bais);
        indent.val = R2kUtil.readLcfVLI(bais);
        parameters.text.data = IntUtils.readBytes(bais, R2kUtil.readLcfVLI(bais));
        if (code.val != 11330) {
            moveCommands = null;
            parameters.arrVal = new IRIO[R2kUtil.readLcfVLI(bais)];
            for (int i = 0; i < parameters.arrVal.length; i++)
                parameters.arrVal[i] = new IRIOFixnum(context, R2kUtil.readLcfVLI(bais));
        } else {
            // SPECIAL CASE!!!
            // This does a bunch of scary stuff which doesn't work for fixed-format commands,
            //  and thus really needs special logic.
            parameters.arrVal = new IRIO[4];
            int[] remainingStream = new int[R2kUtil.readLcfVLI(bais) - 4];
            for (int i = 0; i < parameters.arrVal.length; i++)
                parameters.arrVal[i] = new IRIOFixnum(context, R2kUtil.readLcfVLI(bais));
            for (int i = 0; i < remainingStream.length; i++)
                remainingStream[i] = R2kUtil.readLcfVLI(bais);
            addIVar("@move_commands");
            moveCommands.arrVal = MoveCommand.fromEmbeddedData(dm2Ctx, remainingStream);
        }
    }

    @Override
    public boolean canOmitChunk() {
        return false;
    }

    @Override
    public void exportData(OutputStream baos) throws IOException {
        R2kUtil.writeLcfVLI(baos, (int) code.val);
        R2kUtil.writeLcfVLI(baos, (int) indent.val);
        R2kUtil.writeLcfVLI(baos, parameters.text.data.length);
        baos.write(parameters.text.data);
        if (code.val != 11330) {
            R2kUtil.writeLcfVLI(baos, parameters.arrVal.length);
            for (int i = 0; i < parameters.arrVal.length; i++)
                R2kUtil.writeLcfVLI(baos, (int) parameters.arrVal[i].getFX());
        } else {
            int[] encoded = MoveCommand.toEmbeddedData(moveCommands);
            R2kUtil.writeLcfVLI(baos, encoded.length + 4);
            for (int i = 0; i < 4; i++)
                R2kUtil.writeLcfVLI(baos, (int) parameters.arrVal[i].getFX());
            for (int i = 0; i < encoded.length; i++)
                R2kUtil.writeLcfVLI(baos, encoded[i]);
        }
    }
}
