/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.imi;

import gabien.GaBIEn;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.io.IMIUtils;
import r48.io.IObjectBackend;
import r48.io.PathUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.zip.GZIPOutputStream;

/**
 * This handles the actual logic of the IMI assembly process.
 * It is controlled by the IMIAssemblyController.
 * Constructing this implies that everything is ready for IMI assembly.
 * The assembly will run in a semi-background state for UI-responsiveness.
 */
public class IMIAssemblyProcess {
    public LinkedList<Runnable> imiAssemblyTasks = new LinkedList<Runnable>();
    private Runnable taskRunner;
    private DataOutputStream dos;
    // For display
    public LinkedList<String> imiCreatedFiles = new LinkedList<String>();
    public LinkedList<String> imiModdedFiles = new LinkedList<String>();

    public IMIAssemblyProcess(String s, final Runnable updatedList) {
        taskRunner = new Runnable() {
            @Override
            public void run() {
                if (dos == null)
                    return;
                double n = GaBIEn.getTime();
                while (GaBIEn.getTime() < (n + 0.1d)) {
                    if (imiAssemblyTasks.size() == 0)
                        break;
                    imiAssemblyTasks.removeFirst().run();
                }
                if (imiAssemblyTasks.size() > 0)
                    AppMain.pendingRunnables.add(taskRunner);
            }
        };
        AppMain.pendingRunnables.add(taskRunner);

        try {
            FileOutputStream fos = new FileOutputStream(PathUtils.autoDetectWindows(AppMain.rootPath + "imi.txt.gz"));
            dos = new DataOutputStream(new GZIPOutputStream(fos));
            dos.writeBytes("I1\"");
            IMIUtils.writeIMIStringBody(dos, AppMain.odbBackend.getBytes("UTF-8"), false);
            dos.writeBytes("\"");
            IMIUtils.writeIMIStringBody(dos, AppMain.dataPath.getBytes("UTF-8"), false);
            dos.writeBytes("\"");
            IMIUtils.writeIMIStringBody(dos, AppMain.dataExt.getBytes("UTF-8"), false);
            dos.writeByte('\n');
            IMIUtils.writeIMIStringBody(dos, IObjectBackend.Factory.encoding.getBytes("UTF-8"), false);
            dos.writeByte('\n');

            LinkedList<String> objs = AppMain.getAllObjects();
            final IObjectBackend oldGameAccess = IObjectBackend.Factory.create(AppMain.odbBackend, s, AppMain.dataPath, AppMain.dataExt);
            for (final String s2 : objs) {
                imiAssemblyTasks.add(new Runnable() {
                    @Override
                    public void run() {
                        if (dos == null)
                            return;
                        // NOTE: This has no Schema access, which is intentional
                        RubyIO a = oldGameAccess.loadObjectFromFile(s2);
                        //
                        RubyIO b = AppMain.objectDB.getObject(s2, null);
                        try {
                            if (a != null) {
                                if (b != null) {
                                    byte[] diff = IMIUtils.createIMIData(a, b, "");
                                    if (diff != null) {
                                        imiModdedFiles.add(s2);
                                        updatedList.run();
                                        dos.writeBytes("~\"");
                                        IMIUtils.writeIMIStringBody(dos, s2.getBytes("UTF-8"), false);
                                        dos.writeByte('\n');
                                        dos.write(diff);
                                    }
                                }
                            } else {
                                if (b != null) {
                                    imiCreatedFiles.add(s2);
                                    updatedList.run();
                                    dos.writeBytes("+\"");
                                    IMIUtils.writeIMIStringBody(dos, s2.getBytes("UTF-8"), false);
                                    dos.writeByte('\n');
                                    IMIUtils.createIMIDump(dos, b, "");
                                }
                            }
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }
                });
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public void injectFiles(LinkedList<String> files) {
        if (dos == null)
            return;
        String someIssues = null;
        try {
            for (String s : files) {
                InputStream source = GaBIEn.getInFile(PathUtils.autoDetectWindows(AppMain.rootPath + s));
                if (source != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    while (true) {
                        int l = source.read(data);
                        if (l <= 0)
                            break;
                        baos.write(data, 0, l);
                    }
                    source.close();
                    // Use false for this, because this is supposed to not be human-readable
                    dos.writeBytes("F\"");
                    IMIUtils.writeIMIStringBody(dos, s.getBytes("UTF-8"), false);
                    dos.write('\"');
                    IMIUtils.writeIMIStringBody(dos, baos.toByteArray(), false);
                    dos.write(10);
                } else {
                    if (someIssues == null)
                        someIssues = "";
                    someIssues += "\n" + s;
                }
            }
            dos.close();
            dos = null;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        if (someIssues != null) {
            AppMain.launchDialog(TXDB.get("Created patchfile 'imi.txt.gz'. Some asset files were not retrievable:") + someIssues);
        } else {
            AppMain.launchDialog(TXDB.get("Created patchfile 'imi.txt.gz'."));
        }
    }

    public void windowLost() {
        if (dos != null) {
            try {
                dos.close();
            } catch (Exception e) {
            }
            dos = null;
        }
    }
}
