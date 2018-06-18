/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.GaBIEn;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.io.PathUtils;
import r48.maptools.UIMTBase;
import r48.ui.UIAppendButton;

import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Responsible for assembling an IMI patchfile.
 * Created on 29th November 2017.
 */
public class IMIAssemblyController {
    private UIScrollLayout fileList;
    public UISplitterLayout outerSplit;
    private LinkedList<String> files = new LinkedList<String>();
    private IMIAssemblyProcess assembler;
    private boolean fileBrowserOpen = false;

    public IMIAssemblyController(String s, final IConsumer<UIElement> wm) {
        assembler = new IMIAssemblyProcess(s, new Runnable() {
            @Override
            public void run() {
                rebuildFL();
            }
        });
        fileList = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public void setWantedSize(Size size) {
                int minH = FontSizes.imiAsmAssetTextHeight * 8;
                if (size.height < minH) {
                    super.setWantedSize(new Size(size.width, minH));
                } else {
                    super.setWantedSize(size);
                }
            }
        };
        UITextButton bAA = new UITextButton(TXDB.get("Add Asset"), FontSizes.imiAsmButtonsTextHeight, new Runnable() {
            @Override
            public void run() {
                if (fileBrowserOpen)
                    return;

                GaBIEn.startFileBrowser(TXDB.get("Add IMI Asset"), false, "", new IConsumer<String>() {
                    @Override
                    public void accept(String s) {
                        fileBrowserOpen = false;
                        if (s != null) {
                            s = PathUtils.autoDetectWindows(s);
                            int idx = s.toLowerCase().indexOf(AppMain.rootPath.toLowerCase());
                            if (idx <= -1) {
                                AppMain.launchDialog(TXDB.get("The asset doesn't appear to be inside the game. Should this cause issues, run R48 from within the modded game's directory."));
                                return;
                            }
                            InputStream inp = GaBIEn.getInFile(s);
                            if (inp == null)
                                AppMain.launchDialog(TXDB.get("The file appears to be inaccessible."));
                            s = s.substring(idx + AppMain.rootPath.length());
                            if (s.startsWith("/"))
                                s = s.substring(1);
                            try {
                                inp.close();
                            } catch (IOException ioe) {
                            }
                            for (String s2 : files) {
                                // lowest-common-denominator - assume case insensitive
                                if (s2.equalsIgnoreCase(s)) {
                                    AppMain.launchDialog(TXDB.get("This asset has already been added."));
                                    return;
                                }
                            }
                            files.add(s);
                            rebuildFL();
                        }
                    }
                });
                fileBrowserOpen = true;
            }
        });
        UITextButton bSM = new UITextButton(TXDB.get("Save Asset List"), FontSizes.imiAsmButtonsTextHeight, new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream os = GaBIEn.getOutFile(PathUtils.autoDetectWindows(AppMain.rootPath + "imiManifest.txt"));
                    PrintStream ps = new PrintStream(os, false, "UTF-8");
                    for (String s : files)
                        ps.println(s);
                    ps.flush();
                    os.close();
                    AppMain.launchDialog(TXDB.get("imiManifest.txt has been saved for later use."));
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        });
        UISplitterLayout toolbar = new UISplitterLayout(bAA, bSM, false, 0.5d);
        final AtomicBoolean baseCloser = new AtomicBoolean(false);
        UIAppendButton appender = new UIAppendButton(TXDB.get("Finish"), toolbar, new Runnable() {
            @Override
            public void run() {
                if (fileBrowserOpen)
                    return;
                if (assembler.imiAssemblyTasks.size() > 0) {
                    AppMain.launchDialog(TXDB.get("The IMI assembler is still working on the game data."));
                } else {
                    // IMI assembler ready
                    assembler.injectFiles(files);
                    baseCloser.set(true);
                }
            }
        }, FontSizes.imiAsmButtonsTextHeight);
        outerSplit = new UISplitterLayout(fileList, appender, true, 1.0d) {
            @Override
            public String toString() {
                String base = TXDB.get("IMI Assembler");
                if (assembler.imiAssemblyTasks.size() > 0)
                    return base + " (" + assembler.imiAssemblyTasks.size() + ")";
                return base;
            }
        };
        final UIMTBase base = UIMTBase.wrapWithCloseCallback(null, outerSplit, baseCloser, new Runnable() {
            @Override
            public void run() {
                assembler.windowLost();
            }
        });
        importSavedManifest();
        rebuildFL();
        wm.accept(base);
    }

    private void importSavedManifest() {
        try {
            InputStream inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(AppMain.rootPath + "imiManifest.txt"));
            BufferedReader br = new BufferedReader(new InputStreamReader(inp, "UTF-8"));
            while (br.ready())
                files.add(br.readLine());
            inp.close();
        } catch (Exception e) {
        }
    }

    public void rebuildFL() {
        fileList.panelsClear();
        for (String s2 : assembler.imiCreatedFiles) {
            UILabel fileName = new UILabel(TXDB.get("Create Data: ") + s2, FontSizes.imiAsmAssetTextHeight);
            fileList.panelsAdd(fileName);
        }
        for (String s2 : assembler.imiModdedFiles) {
            UILabel fileName = new UILabel(TXDB.get("Modify Data: ") + s2, FontSizes.imiAsmAssetTextHeight);
            fileList.panelsAdd(fileName);
        }
        for (final String s : files) {
            UILabel fileName = new UILabel(TXDB.get("Copy Asset: ") + s, FontSizes.imiAsmAssetTextHeight);
            fileList.panelsAdd(new UIAppendButton(TXDB.get("Cancel"), fileName, new Runnable() {
                @Override
                public void run() {
                    files.remove(s);
                    rebuildFL();
                }
            }, FontSizes.imiAsmAssetTextHeight));
        }
    }
}
