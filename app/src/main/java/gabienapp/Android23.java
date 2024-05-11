/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import java.io.IOException;

import gabien.GaBIEn;
import gabien.pva.PVARenderer;
import gabien.render.IGrDriver;
import gabien.wsi.IGrInDriver;
import gabien.wsi.WindowSpecs;

/**
 * Created 23rd April, 2024.
 */
public class Android23 {
    public static void run() {
        int globalMS = 50;
        double compensationDT = 0.0d;
        WindowSpecs ws = GaBIEn.defaultWindowSpecs("storage permission error", 800, 600);
        ws.resizable = true;
        ws.fullscreen = false;
        ws.backgroundLight = false;
        IGrInDriver window = GaBIEn.makeGrIn("storage permission error", 800, 600, ws);
        PVARenderer pf;
        try {
            pf = new PVARenderer(GaBIEn.getResource("animations/androidStoragePermissionError.pva"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        IGrDriver backbufferA = null;
        IGrDriver backbufferB = null;
        while (window.stillRunning()) {
            // If the user has granted storage permission, then we 'only' lose gabien init resources by continuing
            if (GaBIEn.hasStoragePermission())
                return;
            double dTTarg = (globalMS / 1000d) - compensationDT;
            double dT = GaBIEn.endFrame(dTTarg);
            compensationDT = Math.min(dTTarg, dT - dTTarg);
            // ensure backbuffers
            backbufferA = window.ensureBackBuffer(backbufferA, 2);
            backbufferB = window.ensureBackBuffer(backbufferB, 1);
            // figure out scale
            float scale = (float) Math.min(backbufferA.width / ((double) pf.pvaFile.header.width), backbufferA.height / ((double) pf.pvaFile.header.height));
            float scaledW = pf.pvaFile.header.width * scale;
            float scaledH = pf.pvaFile.header.height * scale;
            // render
            pf.renderInline(pf.pvaFile.frames[pf.pvaFile.frameOfLooped(GaBIEn.getTime() * 1000)], backbufferA, (backbufferA.width - scaledW) / 2, (backbufferA.height - scaledH) / 2, scaledW, scaledH);
            backbufferB.drawScaledColoured(0, 0, backbufferA.width, backbufferA.height, 0, 0, backbufferB.width, backbufferB.height, backbufferA, IGrDriver.BLEND_NORMAL, 0, 0.25f, 0.25f, 0.25f, 1);
            backbufferB.drawScaledColoured(1, 0, backbufferA.width, backbufferA.height, 0, 0, backbufferB.width, backbufferB.height, backbufferA, IGrDriver.BLEND_ADD, 0, 0.25f, 0.25f, 0.25f, 1);
            backbufferB.drawScaledColoured(0, 1, backbufferA.width, backbufferA.height, 0, 0, backbufferB.width, backbufferB.height, backbufferA, IGrDriver.BLEND_ADD, 0, 0.25f, 0.25f, 0.25f, 1);
            backbufferB.drawScaledColoured(1, 1, backbufferA.width, backbufferA.height, 0, 0, backbufferB.width, backbufferB.height, backbufferA, IGrDriver.BLEND_ADD, 0, 0.25f, 0.25f, 0.25f, 1);
            // swap
            window.flush(backbufferB);
            GaBIEn.runCallbacks();
        }
        GaBIEn.ensureQuit();
    }
}
