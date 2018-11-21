/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.obj.ldb;

import r48.RubyIO;
import r48.io.data.IRIO;
import r48.io.r2k.Index;
import r48.io.r2k.chunks.R2kObject;
import r48.io.r2k.chunks.StringR2kStruct;

/**
 * COPY jun6-2017
 */
public class Terms extends R2kObject {
    // This is MASSIVE. Luckily it's also 100% strings?
    // 380 to 506 incl.
    // 506 - 380 == 127
    public StringR2kStruct[] termArray = new StringR2kStruct[127];

    public Terms() {
        for (int i = 0; i < 127; i++)
            termArray[i] = new StringR2kStruct();
    }

    @Override
    public Index[] getIndices() {
        return new Index[] {
                new Index(0x01, termArray[0]),
                new Index(0x02, termArray[1]),
                new Index(0x03, termArray[2]),
                new Index(0x04, termArray[3]),
                new Index(0x05, termArray[4]),
                new Index(0x06, termArray[5]),
                new Index(0x07, termArray[6]),
                new Index(0x08, termArray[7]),
                new Index(0x09, termArray[8]),
                new Index(0x0A, termArray[9]),
                new Index(0x0B, termArray[10]),
                new Index(0x0C, termArray[11]),
                new Index(0x0D, termArray[12]),
                new Index(0x0E, termArray[13]),
                new Index(0x0F, termArray[14]),
                new Index(0x10, termArray[15]),
                new Index(0x11, termArray[16]),
                new Index(0x12, termArray[17]),
                new Index(0x13, termArray[18]),
                new Index(0x14, termArray[19]),
                new Index(0x15, termArray[20]),
                new Index(0x16, termArray[21]),
                new Index(0x17, termArray[22]),
                new Index(0x18, termArray[23]),
                new Index(0x19, termArray[24]),
                new Index(0x1A, termArray[25]),
                new Index(0x1B, termArray[26]),
                new Index(0x1C, termArray[27]),
                new Index(0x1D, termArray[28]),
                new Index(0x1E, termArray[29]),
                new Index(0x1F, termArray[30]),
                new Index(0x20, termArray[31]),
                new Index(0x21, termArray[32]),
                new Index(0x22, termArray[33]),
                new Index(0x23, termArray[34]),
                new Index(0x24, termArray[35]),
                new Index(0x25, termArray[36]),
                new Index(0x26, termArray[37]),
                new Index(0x27, termArray[38]),
                new Index(0x29, termArray[39]),
                new Index(0x2A, termArray[40]),
                new Index(0x2B, termArray[41]),
                new Index(0x2C, termArray[42]),
                new Index(0x2D, termArray[43]),
                new Index(0x2E, termArray[44]),
                new Index(0x2F, termArray[45]),
                new Index(0x30, termArray[46]),
                new Index(0x31, termArray[47]),
                new Index(0x32, termArray[48]),
                new Index(0x33, termArray[49]),
                new Index(0x36, termArray[50]),
                new Index(0x37, termArray[51]),
                new Index(0x38, termArray[52]),
                new Index(0x39, termArray[53]),
                new Index(0x3A, termArray[54]),
                new Index(0x3B, termArray[55]),
                new Index(0x3C, termArray[56]),
                new Index(0x3D, termArray[57]),
                new Index(0x3E, termArray[58]),
                new Index(0x3F, termArray[59]),
                new Index(0x40, termArray[60]),
                new Index(0x43, termArray[61]),
                new Index(0x44, termArray[62]),
                new Index(0x45, termArray[63]),
                new Index(0x46, termArray[64]),
                new Index(0x47, termArray[65]),
                new Index(0x48, termArray[66]),
                new Index(0x49, termArray[67]),
                new Index(0x4A, termArray[68]),
                new Index(0x4B, termArray[69]),
                new Index(0x4C, termArray[70]),
                new Index(0x4D, termArray[71]),
                new Index(0x50, termArray[72]),
                new Index(0x51, termArray[73]),
                new Index(0x52, termArray[74]),
                new Index(0x53, termArray[75]),
                new Index(0x54, termArray[76]),
                new Index(0x55, termArray[77]),
                new Index(0x56, termArray[78]),
                new Index(0x57, termArray[79]),
                new Index(0x58, termArray[80]),
                new Index(0x59, termArray[81]),
                new Index(0x5C, termArray[82]),
                new Index(0x5D, termArray[83]),
                new Index(0x5F, termArray[84]),
                new Index(0x65, termArray[85]),
                new Index(0x66, termArray[86]),
                new Index(0x67, termArray[87]),
                new Index(0x68, termArray[88]),
                new Index(0x69, termArray[89]),
                new Index(0x6A, termArray[90]),
                new Index(0x6B, termArray[91]),
                new Index(0x6C, termArray[92]),
                new Index(0x6E, termArray[93]),
                new Index(0x70, termArray[94]),
                new Index(0x72, termArray[95]),
                new Index(0x73, termArray[96]),
                new Index(0x75, termArray[97]),
                new Index(0x76, termArray[98]),
                new Index(0x77, termArray[99]),
                new Index(0x78, termArray[100]),
                new Index(0x79, termArray[101]),
                new Index(0x7A, termArray[102]),
                new Index(0x7B, termArray[103]),
                new Index(0x7C, termArray[104]),
                new Index(0x7D, termArray[105]),
                new Index(0x7E, termArray[106]),
                new Index(0x7F, termArray[107]),
                new Index(0x80, termArray[108]),
                new Index(0x81, termArray[109]),
                new Index(0x82, termArray[110]),
                new Index(0x83, termArray[111]),
                new Index(0x84, termArray[112]),
                new Index(0x85, termArray[113]),
                new Index(0x86, termArray[114]),
                new Index(0x87, termArray[115]),
                new Index(0x88, termArray[116]),
                new Index(0x89, termArray[117]),
                new Index(0x8A, termArray[118]),
                new Index(0x8B, termArray[119]),
                new Index(0x8C, termArray[120]),
                new Index(0x92, termArray[121]),
                new Index(0x93, termArray[122]),
                new Index(0x94, termArray[123]),
                new Index(0x97, termArray[124]),
                new Index(0x98, termArray[125]),
                new Index(0x99, termArray[126])
        };
    }

    @Override
    public RubyIO asRIO() {
        RubyIO a = new RubyIO();
        a.type = '[';
        a.arrVal = new RubyIO[termArray.length];
        for (int i = 0; i < termArray.length; i++)
            a.arrVal[i] = termArray[i].asRIO();
        asRIOISF(a);
        return a;
    }

    @Override
    public void fromRIO(IRIO src) {
        fromRIOISF(src);
        for (int i = 0; i < termArray.length; i++)
            termArray[i].fromRIO(src.getAElem(i));
    }
}
