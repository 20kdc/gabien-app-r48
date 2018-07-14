/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import r48.imageio.ImageIOFormat;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created on 14th July 2018
 */
public class ImageEditorSaveLoadController {
    private String currentFileName;
    private ImageIOFormat currentFileFormat;

    public void newFile() {
        currentFileName = null;
        currentFileFormat = null;
    }

    public boolean canSimplySave(ImageEditorImage iei) {
        if (currentFileName == null)
            return false;
        if (currentFileFormat == null)
            return false;
        if (currentFileFormat.saveName(iei) == null)
            return false;
        return true;
    }

    public void didSuccessfulLoadSave(String name, ImageIOFormat format) {
        currentFileName = name;
        currentFileFormat = format;
    }

    public void simpleSave(ImageEditorImage iei) throws IOException {
        if (currentFileFormat.saveName(iei) == null)
            throw new IOException("File has become incompatible with the current internal format. Use the Save As option, to the right.");
        byte[] data = currentFileFormat.saveFile(iei);
        OutputStream os = GaBIEn.getOutFile(currentFileName);
        os.write(data);
        os.close();
    }
}
