/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.BooleanR2kStruct;
import r48.io.r2k.chunks.IntegerR2kStruct;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * COPY jun6-2017
 * June 8th, 2017 : The last object in the Database.
 * Finishing this makes R48 feature-complete for the R2k backend, as far as I know.
 * There might be some rough edges that need to be smoothed (animation editor, maybe work out how to allow the Root MI without breakage...), but it'll be SOMETHING.
 * Stuff is done, for fun. Even though writing this serialization code took two weeks.
 * <p/>
 * -- A MESSAGE TO USERS --
 * When EasyRPG Editor is as complete as this, use it.
 * Abandon this decaying codebase. It works. That's it.
 * This thing was designed to handle Ruby objects. It does NOT handle Lucifer databases well, as this ton of classes proves.
 * <p/>
 * -- A MESSAGE TO EASYRPG EDITOR --
 * This is a message to EasyRPG Editor devs.
 * <p/>
 * Firstly, please stop importing everything to XML! Yes, it's nicer, but it breaks compatibility.
 * You aren't going to be extending (majorly) yet, and don't try until you have everything sorted out.
 * <p/>
 * Secondly, "Why didn't you contribute to EasyRPG Editor instead?"
 * I'm not that good with Qt and C++. At all. Or most UI frameworks. ^.^;
 * (Keep in mind my entire UI framework is full of N.I.H, then you should understand...)
 * Plus, see 1. Importing to XML makes testing rather... difficult. Even more so before I started using Ib as a test subject.
 * <p/>
 * But the datafiles on the command lists, at least, you should be able to port those!
 * And I hope you do. This editor SUCKS. I wrote it because it's something.
 * And also maybe ~~a little~~ a lot of wanting to write something actually useful for people.
 * <p/>
 * I'm going to embed a link to this note in the release tagline, accessible with the Konami Code,
 * or via looking at the final JAR's data.
 * My editor has a lot of rough corners, but I'm going to take a rest.
 * Either use this, or do better than me - you have everything I wrote at your disposal.
 */
public class Attribute extends R2kObject {

    public StringR2kStruct name = new StringR2kStruct();
    public BooleanR2kStruct magical = new BooleanR2kStruct(false);
    public IntegerR2kStruct aRate = new IntegerR2kStruct(300);
    public IntegerR2kStruct bRate = new IntegerR2kStruct(200);
    public IntegerR2kStruct cRate = new IntegerR2kStruct(100);
    public IntegerR2kStruct dRate = new IntegerR2kStruct(50);
    public IntegerR2kStruct eRate = new IntegerR2kStruct(0);

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, name, "@name"),
                new Index(0x02, magical, "@magical"),
                new Index(0x0B, aRate, "@a_rate"),
                new Index(0x0C, bRate, "@b_rate"),
                new Index(0x0D, cRate, "@c_rate"),
                new Index(0x0E, dRate, "@d_rate"),
                new Index(0x0F, eRate, "@e_rate"),
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO rio = new RubyIO().setSymlike("RPG::Attribute", true);
        asRIOISF(rio);
        return rio;
    }

    @Override
    public void fromRIO(RubyIO src) {
        fromRIOISF(src);
    }
}
