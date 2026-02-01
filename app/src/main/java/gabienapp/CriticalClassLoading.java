/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package gabienapp;

import java.util.zip.DeflaterInputStream;

import gabien.audio.IRawAudioDriver.IRawAudioSource;
import gabien.pva.PVAFrameDrawable;
import gabien.render.DrawableRegion;
import gabien.text.ImageRenderedTextChunk;
import gabien.text.RenderedTextChunk;
import gabien.text.TextTools;
import gabien.ui.IPointerReceiver;
import gabien.ui.UIDynamicProxy;
import gabien.ui.WindowCreatingUIElementConsumer;
import gabien.ui.elements.UIAdjuster;
import gabien.ui.elements.UIBaseIconButton;
import gabien.ui.elements.UIBorderedElement;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UIIconButton;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UIPublicPanel;
import gabien.ui.elements.UITextBox;
import gabien.ui.elements.UITextButton;
import gabien.ui.elements.UIThemeIconButton;
import gabien.ui.elements.UIThumbnail;
import gabien.ui.layouts.UIListLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UIWindowView;
import gabien.uslx.append.EmptyLambdas;
import gabien.uslx.append.Entity;
import gabien.uslx.append.RefSyncSet;
import gabien.uslx.vfs.impl.DodgyInputWorkaroundFSBackend;
import gabien.uslx.vfs.impl.DodgyInputWorkaroundPathModel;
import gabien.uslx.vfs.impl.UnionFSBackend;
import gabien.wsi.IPointer;
import r48.UITest;
import r48.app.AppNewProject;
import r48.app.AppUI;
import r48.app.Coco;
import r48.app.InterlaunchGlobals;
import r48.app.TimeMachine;
import r48.app.TimeMachineChangeSource;
import r48.dbs.ATDB;
import r48.dbs.CMDB;
import r48.dbs.CMDBDB;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.IProxySchemaElement;
import r48.dbs.ObjectDB;
import r48.dbs.RPGCommand;
import r48.dbs.SDB;
import r48.dbs.SDBHelpers;
import r48.dbs.SDBOldParser;
import r48.dbs.TSDB;
import r48.imagefx.IImageEffect;
import r48.imagefx.ImageFXCache;
import r48.imageio.ImageIOFormat;
import r48.io.IObjectBackend;
import r48.io.IkaObjectBackend;
import r48.io.R2kObjectBackend;
import r48.io.data.DMContext;
import r48.io.data.DMKey;
import r48.ioplus.DatumLoader;
import r48.map.MapViewDrawContext;
import r48.map.StuffRenderer;
import r48.map.drawlayers.IMapViewDrawLayer;
import r48.map.events.NullEventGraphicRenderer;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.FixAndSecondaryImageLoader;
import r48.map.imaging.GabienImageLoader;
import r48.map.systems.MapSystem;
import r48.map.tiles.NullTileRenderer;
import r48.minivm.MVMEnv;
import r48.minivm.fn.MVMDMAppLibrary;
import r48.minivm.fn.MVMMathsLibrary;
import r48.minivm.fn.MVMR48AppLibraries;
import r48.minivm.fn.MVMSDBLibrary;
import r48.schema.EnumSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.TempDialogSchemaChoice;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.search.ByCodeCommandClassifier;
import r48.search.CompoundTextAnalyzer;
import r48.search.ICommandClassifier;
import r48.search.ITextAnalyzer;
import r48.search.ImmutableTextAnalyzerCommandClassifier;
import r48.search.TextAnalyzerCommandClassifier;
import r48.search.TextOperator;
import r48.toolsets.BasicToolset;
import r48.toolsets.IToolset;
import r48.toolsets.utils.UITestGraphicsStuff;
import r48.tr.TrNames;
import r48.ui.Art;
import r48.ui.UIDynAppPrx;
import r48.ui.UIMenuButton;
import r48.ui.dialog.UIChoicesMenu;
import r48.ui.dialog.UIFontSizeConfigurator;
import r48.ui.dialog.UIReadEvaluatePrintLoop;
import r48.ui.dialog.UITestFontSizes;
import r48.ui.dialog.UITextPrompt;
import r48.ui.help.HelpSystemController;
import r48.ui.help.UIHelpSystem;
import r48.ui.spacing.UIBorderedSubpanel;
import r48.wm.IDuplicatableWindow;
import r48.wm.TrackedUITicker;
import r48.wm.WindowManager;

/**
 * Loads critical classes.
 * Created 11th May, 2024.
 */
public class CriticalClassLoading {
    public static void actuallyLoad(InterlaunchGlobals ilg) {
        for (Class<?> c : getRoots()) {
            c.getFields();
        }
        // MiniVM warmup
        MVMEnv me = new MVMEnv();
        MVMMathsLibrary.add(me);
        me.evalString("(+ 1 2)");
        ImageIOFormat.initializeFormats(ilg.t);
    }

    private static Class<?>[] getRoots() {
        return new Class<?>[] {
            SDBHelpers.class,
            SDB.class,
            SDBOldParser.class,
            TSDB.class,
            CMDBDB.class,
            CMDB.class,
            DBLoader.class,
            DatumLoader.class,
            ATDB.class,
            IDatabase.class,
            IProxySchemaElement.class,
            RPGCommand.class,
            TempDialogSchemaChoice.class,
            MapViewDrawContext.class,
            DMContext.class,
            TimeMachine.class,
            TimeMachineChangeSource.class,
            R2kObjectBackend.class,
            IkaObjectBackend.class,
            MapSystem.class,
            UILauncher.class,
            ErrorHandler.class,
            IPointerReceiver.class,
            UIFontSizeConfigurator.class,
            UIBorderedElement.class,
            UITextButton.class,
            UISplitterLayout.class,
            UIAdjuster.class,
            UILabel.class,
            UIEmpty.class,
            UIReadEvaluatePrintLoop.class,
            UIFancyInit.class,
            WindowCreatingUIElementConsumer.class,
            RenderedTextChunk.class,
            RenderedTextChunk.Compound.class,
            RenderedTextChunk.CRLF.class,
            UIListLayout.class,
            HelpSystemController.class,
            UIThumbnail.class,
            UITabBar.class,
            RefSyncSet.class,
            RefSyncSet.Holder.class,
            UITabBar.Tab.class,
            UITabBar.TabIcon.class,
            TextTools.PlainCached.class,
            UILabel.Contents.class,
            ImageRenderedTextChunk.class,
            ImageRenderedTextChunk.WSI.class,
            ImageRenderedTextChunk.GPU.class,
            TextTools.class,
            EmptyLambdas.class,
            UIHelpSystem.HelpElement.class,
            PVAFrameDrawable.class,
            DrawableRegion.class,
            Coco.class,
            IRawAudioSource.class,
            UITextBox.class,
            UIBaseIconButton.class,
            UIThemeIconButton.class,
            UIIconButton.class,
            UIMenuButton.class,
            UIBorderedSubpanel.class,
            LauncherEntry.class,
            StartupCause.class,
            TrNames.class,
            IPointer.class,
            IPointer.PointerType.class,
            IPointerReceiver.NopPointerReceiver.class,
            IPointerReceiver.TransformingElementPointerReceiver.class,
            IToolset.class,
            BasicToolset.class,
            TrackedUITicker.class,
            UIWindowView.ElementShell.class,
            UIWindowView.ScreenShell.class,
            UIWindowView.TabShell.class,
            WindowManager.class,
            UITextPrompt.class,
            ImageFXCache.class,
            AppUI.class,
            UIChoicesMenu.class,
            AppNewProject.class,
            MVMSDBLibrary.class,
            MVMDMAppLibrary.class,
            MVMR48AppLibraries.class,
            NullTileRenderer.class,
            FixAndSecondaryImageLoader.class,
            GabienImageLoader.class,
            CacheImageLoader.class,
            NullTileRenderer.class,
            ICommandClassifier.class,
            TextOperator.class,
            TextAnalyzerCommandClassifier.class,
            CompoundTextAnalyzer.class,
            ImmutableTextAnalyzerCommandClassifier.class,
            ByCodeCommandClassifier.class,
            IntegerSchemaElement.IIntegerContext.class,
            IImageEffect.class,
            UITest.class,
            UIDynamicProxy.class,
            UIDynAppPrx.class,
            UITestFontSizes.class,
            UITestGraphicsStuff.class,
            ISchemaHost.class,
            StuffRenderer.class,
            IMapViewDrawLayer.class,
            Art.Symbol.class,
            DMKey.Subtype.class,
            IDuplicatableWindow.class,
            EnumSchemaElement.Prefix.class,
            DodgyInputWorkaroundFSBackend.class,
            DodgyInputWorkaroundPathModel.class,
            UnionFSBackend.class,
            EmbedDataKey.class,
            IObjectBackend.Factory.class,
            ObjectDB.class,
            Entity.class,
            DMContext.Key.class,
            ITextAnalyzer.OrCharTest.class,
            ITextAnalyzer.CJK.class,
            ITextAnalyzer.NotLatin1.class,
            ITextAnalyzer.NotLatin1OrFullwidth.class,
            UIPublicPanel.class,
            DeflaterInputStream.class,
            NullEventGraphicRenderer.class,
            Art.Symbol.Instance.class,
        };
    }
}
