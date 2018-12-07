/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io.r2k.struct;

import r48.io.r2k.chunks.StringR2kStruct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created on 05/06/17.
 */
public class SVStore extends StringR2kStruct {
    @Override
    public void importData(InputStream bais) throws IOException {
        Terms.importTermlike(bais, new int[] {1}, new StringR2kStruct[] {this});
    }

    @Override
    public boolean exportData(OutputStream baos) throws IOException {
        Terms.exportTermlike(baos, new int[] {1}, new StringR2kStruct[] {this});
        return false;
    }
}
