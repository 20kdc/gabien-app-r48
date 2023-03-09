/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.audioplayer;

import java.io.InputStream;

import gabien.GaBIEn;
import gabien.IPeripherals;
import gabien.IRawAudioDriver.IRawAudioSource;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UIScrollbar;
import gabien.ui.UISplitterLayout;
import gabien.media.audio.*;
import r48.App;
import r48.io.PathUtils;
import r48.ui.Art;
import r48.ui.UISymbolButton;

/**
 * Audio player!
 * Created on 2nd August 2022.
 */
public class UIAudioPlayer extends App.Prx {
    // These two are only interacted with from the audio thread.
    public final StreamingAudioDiscreteSample source;
    public volatile double position;
    private boolean playing;
    private final float[] audioThreadBuffer;

    private final UISymbolButton playButton = new UISymbolButton(Art.Symbol.Play, app.f.schemaFieldTextHeight, new Runnable() {
        @Override
        public void run() {
            if (position > source.length)
                position = 0;
            setPlaying(playButton.state);
        }
    }).togglable(false);

    private final UISymbolButton loopButton = new UISymbolButton(Art.Symbol.Loop, app.f.schemaFieldTextHeight, null).togglable(false);

    private final UIScrollbar seeker = new UIScrollbar(false, app.f.generalScrollersize);
    private double lastSeekerScrollPoint;
    private double speed;

    public UIAudioPlayer(App app, AudioIOSource data, double spd) {
        super(app);
        speed = spd;
        source = new StreamingAudioDiscreteSample(data);
        audioThreadBuffer = new float[data.crSet.channels];
        UIScrollLayout svl = new UIScrollLayout(false, app.f.mapToolbarScrollersize);
        svl.panelsAdd(new UISymbolButton(Art.Symbol.Back, app.f.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                position = 0;
            }
        }));
        svl.panelsAdd(playButton);
        svl.panelsAdd(loopButton);
        proxySetElement(new UISplitterLayout(svl, seeker, false, 0), true);
    }

    public void setPlaying(boolean playing) {
        if (this.playing != playing)
            GaBIEn.hintShutdownRawAudio();
        this.playing = playing;
        this.playButton.state = playing;
        if (playing) {
            GaBIEn.getRawAudio().setRawAudioSource(new IRawAudioSource() {
                @Override
                public short[] pullData(int samples) {
                    short[] data = new short[samples * 2];
                    int ptr = 0;
                    for (int i = 0 ; i < samples; i++) {
                        source.getInterpolatedF32(position, audioThreadBuffer, loopButton.state);
                        int secondChannel = audioThreadBuffer.length > 1 ? 1 : 0;
                        data[ptr++] = (short) (AudioIOFormat.cF64toS32(audioThreadBuffer[0]) >> 16);
                        data[ptr++] = (short) (AudioIOFormat.cF64toS32(audioThreadBuffer[secondChannel]) >> 16);
                        position += (source.sampleRate / 22050d) * speed;
                    }
                    if (source.length != 0)
                        if (loopButton.state && position >= source.length)
                            position %= source.length;
                    return data;
                }
            });
        }
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        int len = source.length;
        if (len < 1)
            len = 1;
        if (seeker.scrollPoint != lastSeekerScrollPoint) {
            position = seeker.scrollPoint * len;
        } else {
            seeker.scrollPoint = position / len;
            if (seeker.scrollPoint < 0)
                seeker.scrollPoint = 0;
            if (seeker.scrollPoint > 1)
                seeker.scrollPoint = 1;
        }
        lastSeekerScrollPoint = seeker.scrollPoint;
    }

    public static UIElement create(App app, String filename, double speed) {
        try {
            InputStream tryWav = GaBIEn.getInFile(PathUtils.autoDetectWindows(app.rootPath + filename + ".wav"));
            return new UIAudioPlayer(app, WavIO.readWAV(tryWav, true), speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UILabel(app.t.u.soundFail, app.f.schemaFieldTextHeight);
    }

    @Override
    public void setAttachedToRoot(boolean attached) {
        super.setAttachedToRoot(attached);
        if (!attached)
            setPlaying(false);
    }
}
