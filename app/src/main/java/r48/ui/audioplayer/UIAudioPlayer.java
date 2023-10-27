/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui.audioplayer;

import java.io.InputStream;
import java.util.function.Supplier;

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
import r48.ui.UIDynAppPrx;
import r48.ui.UISymbolButton;

/**
 * Audio player!
 * Created on 2nd August 2022.
 */
public class UIAudioPlayer extends UIDynAppPrx {
    // These two are only interacted with from the audio thread.
    private StreamingAudioDiscreteSample source;
    private volatile double position;
    private boolean playing;
    private float[] audioThreadBuffer;

    // delay load stuff
    private Supplier<AudioIOSource> dataSupplier;

    private final UISymbolButton playButton = new UISymbolButton(Art.Symbol.Play, app.f.schemaFieldTH, new Runnable() {
        @Override
        public void run() {
            setPlaying(playButton.state);
        }
    }).togglable(false);

    private final UISymbolButton loopButton = new UISymbolButton(Art.Symbol.Loop, app.f.schemaFieldTH, null).togglable(false);

    private final UIScrollbar seeker = new UIScrollbar(false, app.f.generalS);
    private double lastSeekerScrollPoint = -1;
    private double speed;

    public UIAudioPlayer(App app, Supplier<AudioIOSource> dataSupplier, double spd) {
        super(app);
        this.dataSupplier = dataSupplier;
        speed = spd;
        UIScrollLayout svl = new UIScrollLayout(false, app.f.mapToolbarS);
        svl.panelsAdd(new UISymbolButton(Art.Symbol.Back, app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                position = 0;
            }
        }));
        svl.panelsAdd(playButton);
        svl.panelsAdd(loopButton);
        changeInner(new UISplitterLayout(svl, seeker, false, 0), true);
    }

    @Override
    public String toString() {
        return T.t.audioPlayer;
    }

    private boolean ensureSourceHasBeenInitialized() {
        if (source != null)
            return true;
        if (dataSupplier == null)
            return false;
        // Note (and beware!) the changeover here
        // dataSupplier becomes null so that it isn't attempted twice
        Supplier<AudioIOSource> dsrc = dataSupplier;
        dataSupplier = null;
        try {
            AudioIOSource data = dsrc.get();
            source = new StreamingAudioDiscreteSample(data, (data.formatHint == null) ? AudioIOFormat.F_F32 : data.formatHint);
            audioThreadBuffer = new float[data.crSet.channels];
        } catch (Exception ex) {
            ex.printStackTrace();
            changeInner(new UILabel(T.u.soundFail, app.f.schemaFieldTH), false);
            return false;
        }
        updateSeeker();
        return true;
    }

    public void setPlaying(boolean playing) {
        if (!ensureSourceHasBeenInitialized())
            return;
        if (this.playing != playing)
            GaBIEn.hintShutdownRawAudio();
        if (position > source.length)
            position = 0;
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

    private void updateSeeker() {
        if (source != null) {
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
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        updateSeeker();
    }

    private static final String[] extensionsWeWillTry = {
            ".wav",
            ".ogg",
            ".mp3",
            ".mid"
    };

    public static UIElement create(App app, String filename, double speed) {
        InputStream theInputStream = null;
        for (String mnt : extensionsWeWillTry) {
            theInputStream = GaBIEn.getInFile(AppMain.autoDetectWindows(app.rootPath + filename + mnt));
            if (theInputStream != null)
                break;
        }
        if (theInputStream == null && (app.secondaryImagePath.length() > 0)) {
            for (String mnt : extensionsWeWillTry) {
                theInputStream = GaBIEn.getInFile(AppMain.autoDetectWindows(app.secondaryImagePath + filename + mnt));
                if (theInputStream != null)
                    break;
            }
        }
        return create(app, theInputStream, speed);
    }

    public static UIElement createAbsoluteName(App app, String filename, double speed) {
        return create(app, GaBIEn.getInFile(filename), speed);
    }

    public static UIElement create(App app, final InputStream tryWav, double speed) {
        if (tryWav != null) {
            try {
                return new UIAudioPlayer(app, () -> {
                    try {
                        return ReadAnySupportedAudioSource.open(tryWav, true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, speed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new UILabel(app.t.u.soundFailFileNotFound, app.f.schemaFieldTH);
    }

    @Override
    public void setAttachedToRoot(boolean attached) {
        super.setAttachedToRoot(attached);
        if (!attached)
            setPlaying(false);
    }
}
