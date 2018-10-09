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
import java.util.LinkedList;

/**
 * Controls "background" systems: undo/redo buffers, save filename
 * Created on 14th July 2018
 */
public class ImageEditorEDS {
    private String currentFileName;
    private ImageIOFormat currentFileFormat;
    // Must be kept in sync with UIImageEditView
    public ImageEditorImage currentImage;
    private LinkedList<ImageEditorImage> undoBuffers = new LinkedList<ImageEditorImage>();
    private LinkedList<ImageEditorImage> redoBuffers = new LinkedList<ImageEditorImage>();
    // How many undos are we from the saved version?
    // Negative means redos are needed to reach the saved version.
    private int saveDepth;

    public void newFile() {
        currentFileName = null;
        currentFileFormat = null;
        undoBuffers.clear();
        redoBuffers.clear();
        saveDepth = 0;
    }

    public boolean canSimplySave() {
        if (currentFileName == null)
            return false;
        if (currentFileFormat == null)
            return false;
        if (currentFileFormat.saveName(currentImage) == null)
            return false;
        return true;
    }

    public void simpleSave() throws IOException {
        if (currentFileFormat.saveName(currentImage) == null)
            throw new IOException("File has become incompatible with the current internal format. Use the Save As option, to the right.");
        byte[] data = currentFileFormat.saveFile(currentImage);
        OutputStream os = GaBIEn.getOutFile(currentFileName);
        os.write(data);
        os.close();
        saveDepth = 0;
    }

    public void didSuccessfulLoad(String name, ImageIOFormat format) {
        newFile();
        didSuccessfulSave(name, format);
    }

    public void didSuccessfulSave(String name, ImageIOFormat format) {
        currentFileName = name;
        currentFileFormat = format;
        saveDepth = 0;
    }

    public void startSection() {
        if (saveDepth < 0) {
            // This means that redos were needed to get back to the saved version, but that lineage is about to be obliterated
            saveDepth = Integer.MIN_VALUE;
        }
        saveDepth++;
        redoBuffers.clear();
        undoBuffers.add(currentImage.clone());
    }

    public void endSection() {
    }

    public boolean hasUndo() {
        return undoBuffers.size() > 0;
    }

    public boolean hasRedo() {
        return redoBuffers.size() > 0;
    }

    // The image overwrite occurs from imageEditView.setImage.
    public ImageEditorImage performRedo() {
        saveDepth++;
        undoBuffers.add(currentImage);
        return redoBuffers.removeLast();
    }

    public ImageEditorImage performUndo() {
        saveDepth--;
        redoBuffers.add(currentImage);
        return undoBuffers.removeLast();
    }

    public boolean imageModified() {
        return saveDepth != 0;
    }
}
