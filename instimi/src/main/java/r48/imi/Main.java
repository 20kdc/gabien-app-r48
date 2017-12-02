/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.imi;

import gabien.GaBIEn;
import gabien.ui.IConsumer;
import r48.io.IMIUtils;
import r48.io.PathUtils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * I.M.I.
 * "The sequence has begun - It's too late.
 *  The machine comes alive - It's awake."
 * Created on December 1st.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e3) {
                try {
                    // This can't be trusted to actually do anything on OpenJDK as far as I can tell,
                    //  which is why it's last
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e4) {

                }
            }
        }
        Branding.init();
        final JFrame imi = new JFrame(Branding.lines[0]);
        imi.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BufferedImage brand = GaBIEn.getImage("branding.png");
        imi.setIconImage(brand);
        int scale = 1;
        while ((brand.getHeight() * scale) < 240)
            scale++;
        if (scale != 1) {
            BufferedImage b2 = new BufferedImage(brand.getWidth() * scale, brand.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
            b2.getGraphics().drawImage(brand, 0, 0, b2.getWidth(), b2.getHeight(), null);
            brand = b2;
        }
        final BufferedImage brand2 = brand;
        InputStream modI = GaBIEn.getResource("imi.gz");
        if (modI == null)
            if (args.length == 2)
                if (args[0].equals("loadFile"))
                    modI = GaBIEn.getFile(PathUtils.autoDetectWindows(args[1]));
        final InputStream mod = modI;
        if (mod == null) {
            prepDialog(Branding.lines[1].split("#"), Branding.lines[6], new Runnable() {
                boolean didFileDialog = false;
                @Override
                public void run() {
                    if (didFileDialog)
                        return;
                    didFileDialog = true;
                    LinkedList<String> classes = Branding.copyThis();
                    if (classes == null) {
                        prepDialog(Branding.lines[5].split("#"), null, null, brand2, imi);
                        return;
                    }
                    // Deliberately try to use system-native file dialog.
                    // Swing is used for other stuff because it's better at buttons/etc. integration,
                    //  but in this case it's better to bring in the 'real' file dialog because *that's* better at integration here.
                    FileDialog fd = new FileDialog(imi, Branding.lines[3], FileDialog.LOAD);
                    fd.setVisible(true);
                    String sf = fd.getFile();
                    if (sf == null) {
                        didFileDialog = false;
                    } else {
                        try {
                            OutputStream os = GaBIEn.getOutFile("imi-finalized.jar");
                            ZipOutputStream os2 = new ZipOutputStream(os);
                            for (String s : classes) {
                                InputStream inp = GaBIEn.getResource(s);
                                System.err.println(s);
                                putZipEnt(os2, inp, s);
                            }
                            putZipEnt(os2, new FileInputStream(fd.getDirectory() + "/" + sf), "imi.gz");
                            putZipEnt(os2, GaBIEn.getResource("manifest.txt"), "META-INF/MANIFEST.MF");
                            os2.close();
                            os.close();
                            prepDialog(Branding.lines[2].split("#"), null, null, brand2, imi);
                        } catch (Exception e) {
                            e.printStackTrace();
                            prepDialog(new String[] {Branding.lines[4], e.toString(), e.getMessage()}, null, null, brand2, imi);
                        }
                    }
                }

                private void putZipEnt(ZipOutputStream os2, InputStream inp, String s) throws IOException {
                    os2.putNextEntry(new ZipEntry(s));
                    while (true) {
                        byte[] d = new byte[512];
                        int c = inp.read(d);
                        if (c <= 0)
                            break;
                        os2.write(d, 0, c);
                    }
                    inp.close();
                    os2.closeEntry();
                }
            }, brand, imi);
        } else {
            prepDialog(Branding.lines[7].split("#"), Branding.lines[8], new Runnable() {
                boolean hasIMIAwoken = false;
                @Override
                public void run() {
                    if (hasIMIAwoken)
                        return;
                    hasIMIAwoken = true;
                    // Awaken it.
                    final IConsumer<String> status = new IConsumer<String>() {
                        String errorPostfix = "";
                        boolean didError = false;
                        @Override
                        public void accept(String s) {
                            if (s == null) {
                                didError = true;
                                errorPostfix = Branding.lines[16] + errorPostfix;
                                acceptCore(errorPostfix);
                            } else {
                                if (didError) {
                                    errorPostfix += "#" + s;
                                    acceptCore(errorPostfix);
                                } else {
                                    errorPostfix = Branding.lines[14] + s;
                                    acceptCore(Branding.lines[9] + s);
                                }
                            }
                        }

                        private void acceptCore(final String s) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    prepDialog(s.split("#"), null, null, brand2, imi);
                                }
                            });
                        }
                    };
                    new Thread() {
                        @Override
                        public void run() {
                            status.accept("");
                            try {
                                IMIUtils.runIMIFile(new GZIPInputStream(mod), "", new IConsumer<String>() {
                                    @Override
                                    public void accept(String s) {
                                        String c = s.substring(1);
                                        char ch = s.charAt(0);
                                        int ln = 0;
                                        switch (ch) {
                                            case 'R':
                                                ln = 10;
                                                break;
                                            case '~':
                                                ln = 11;
                                                break;
                                            case 'W':
                                                ln = 12;
                                                break;
                                            case 'A':
                                                ln = 13;
                                                break;
                                        }
                                        status.accept(Branding.lines[ln].replace("$", c));
                                    }
                                });
                                status.accept(Branding.lines[15]);
                            } catch (Exception e) {
                                status.accept(null);
                                StringWriter sw = new StringWriter();
                                e.printStackTrace();
                                e.printStackTrace(new PrintWriter(sw));
                                for (String s2 : sw.toString().split("\n"))
                                    status.accept(s2.replace("\r", ""));
                            }
                        }
                    }.start();
                }
            }, brand, imi);
        }
        imi.setResizable(false);
        imi.setVisible(true);
    }

    private static void prepDialog(String[] first, String buttonText, final Runnable button, Image brand, JFrame imi) {
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(BorderLayout.WEST, new JLabel(new ImageIcon(brand)));
        JPanel jp2 = new JPanel();
        jp2.setLayout(new BoxLayout(jp2, BoxLayout.Y_AXIS));
        jp2.setBorder(new BevelBorder(BevelBorder.RAISED));
        for (String s : first) {
            JLabel jl = new JLabel(s);
            jl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            jp2.add(jl);
        }
        if (button != null) {
            JButton jButton = new JButton(buttonText);
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    button.run();
                }
            });
            jp2.add(jButton);
        }
        jp.add(BorderLayout.EAST, jp2);
        imi.setContentPane(jp);
        imi.pack();
    }

    private static class Branding {
        public static String[] lines;

        public static void init() {
            lines = new String[17];
            for (int i = 0; i < lines.length; i++)
                lines[i] = "I just don't know what went wrong...";
            try {
                InputStream inp = GaBIEn.getResource("branding.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(inp, "UTF-8"));
                for (int i = 0; i < lines.length; i++)
                    lines[i] = br.readLine();
                inp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static LinkedList<String> copyThis() {
            LinkedList<String> lls = new LinkedList<String>();
            try {
                InputStream inp = GaBIEn.getResource("world.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(inp, "UTF-8"));
                while (br.ready())
                    lls.add(br.readLine().substring(2));
                inp.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            lls.add("CREDITS.txt");
            lls.add("COPYING.txt");
            lls.add("branding.png");
            lls.add("branding.txt");
            return lls;
        }
    }
}
