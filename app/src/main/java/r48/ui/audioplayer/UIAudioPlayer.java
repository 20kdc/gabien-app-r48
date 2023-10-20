/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.audioplayer;

import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNull;

import gabien.GaBIEn;
import gabien.audio.IRawAudioDriver.IRawAudioSource;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import gabien.ui.UIScrollbar;
import gabien.ui.UISplitterLayout;
import gabien.wsi.IPeripherals;
import gabien.media.audio.*;
import gabien.media.audio.fileio.ReadAnySupportedAudioSource;
import r48.App;
import r48.app.AppMain;
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

    private final UISymbolButton playButton = new UISymbolButton(Art.Symbol.Play, app.f.schemaFieldTH, new Runnable() {
        @Override
        public void run() {
            if (position > source.length)
                position = 0;
            setPlaying(playButton.state);
        }
    }).togglable(false);

    private final UISymbolButton loopButton = new UISymbolButton(Art.Symbol.Loop, app.f.schemaFieldTH, null).togglable(false);

    private final UIScrollbar seeker = new UIScrollbar(false, app.f.generalS);
    private double lastSeekerScrollPoint;
    private double speed;

    public UIAudioPlayer(App app, AudioIOSource data, double spd) {
        super(app);
        speed = spd;
        source = new StreamingAudioDiscreteSample(data, (data.formatHint == null) ? AudioIOFormat.F_F32 : data.formatHint);
        audioThreadBuffer = new float[data.crSet.channels];
        UIScrollLayout svl = new UIScrollLayout(false, app.f.mapToolbarS);
        svl.panelsAdd(new UISymbolButton(Art.Symbol.Back, app.f.schemaFieldTH, new Runnable() {
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
                public void pullData(@NonNull short[] interleaved, int ofs, int frames) {
                    int lim = ofs + (frames * 2);
                    for (int i = ofs; i < lim; i += 2) {
                        source.getInterpolatedF32(position, audioThreadBuffer, loopButton.state);
                        int secondChannel = audioThreadBuffer.length > 1 ? 1 : 0;
                        interleaved[i] = (short) (AudioIOFormat.cF64toS32(audioThreadBuffer[0]) >> 16);
                        interleaved[i + 1] = (short) (AudioIOFormat.cF64toS32(audioThreadBuffer[secondChannel]) >> 16);
                        position += (source.sampleRate / 22050d) * speed;
                    }
                    if (source.length != 0)
                        if (loopButton.state && position >= source.length)
                            position %= source.length;
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
            InputStream tryWav = GaBIEn.getInFile(AppMain.autoDetectWindows(app.rootPath + filename + ".wav"));
            if (tryWav != null)
                return new UIAudioPlayer(app, ReadAnySupportedAudioSource.open(tryWav, true), speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UILabel(app.t.u.soundFail, app.f.schemaFieldTH);
    }

    public static UIElement createAbsoluteName(App app, String filename, double speed) {
        try {
            InputStream tryWav = GaBIEn.getInFile(filename);
            if (tryWav != null)
                return new UIAudioPlayer(app, ReadAnySupportedAudioSource.open(tryWav, true), speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UILabel(app.t.u.soundFail, app.f.schemaFieldTH);
    }

    @Override
    public void setAttachedToRoot(boolean attached) {
        super.setAttachedToRoot(attached);
        if (!attached)
            setPlaying(false);
    }
}
