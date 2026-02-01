/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import java.util.LinkedList;

import gabien.ui.UIElement;
import gabien.ui.layouts.UIScrollLayout;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.layouts.UITabBar;
import gabien.ui.layouts.UITabPane;
import r48.App;
import r48.RubyTable;
import r48.dbs.PathSyntax;
import r48.io.data.IRIO;
import r48.map.StuffRenderer;
import r48.map2d.tiles.AutoTileTypeField;
import r48.map2d.tiles.TileEditingTab;
import r48.schema.specialized.tbleditors.ITableCellEditor;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.EmbedDataSlot;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.UITileGrid;

/**
 * Reuses tile editing metadata for a fancy way to edit tile flags
 *  (solves problems like how to set everything in an AT field)
 * Created on 03/03/2020.
 */
public class FancyCategorizedTilesetRubyTableSchemaElement extends BaseRubyTableSchemaElement {
    public final ITableCellEditor editor;
    public final EmbedDataKey<Integer> tabKey = new EmbedDataKey<>();
    public final EmbedDataKey<UITileGrid> tabCopyKey = new EmbedDataKey<>();

    public FancyCategorizedTilesetRubyTableSchemaElement(App app, int dw, int dh, int p, int d, PathSyntax iV, int[] defaults, ITableCellEditor editor) {
        super(app, dw, dh, p, d, iV, defaults);
        this.editor = editor;
    }

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        final RubyTable targ = new RubyTable(extractTarget(target).editUser());
        final StuffRenderer renderer = launcher.getApp().system.rendererFromTso(target);
        final TileEditingTab[] tileTabs = renderer.tileRenderer.getEditConfig(0);
        final AutoTileTypeField[] atFields = renderer.tileRenderer.indicateATs();
        final UIElement[] tileTabElements = new UIElement[tileTabs.length];
        final UITileGrid[] tileTabGrids = new UITileGrid[tileTabs.length];
        int spriteScale = app.f.getSpriteScale();
        // -- keys/slots --
        EmbedDataSlot<Integer> tabSlot = launcher.embedSlot(target, tabKey, 0);
        EmbedDataSlot<UITileGrid> tabCopySlot = launcher.embedSlot(target, tabCopyKey, null);
        // -- Assemble tab pane --
        final UITabPane tabPane = new UITabPane(app.f.tilesTabTH, true, false, app.f.tilesTabS);
        for (int i = 0; i < tileTabs.length; i++) {
            final TileEditingTab tab = tileTabs[i];
            // Multi-select is allowed on everything.
            final UITileGrid tileGrid = new UITileGrid(app, renderer, 0, 0, true, tab.visTilesNormal, tab.visTilesHover, " " + tab.localizedText + " ", spriteScale);
            
            UIScrollLayout fields = new UIScrollLayout(true, app.f.generalS);
            LinkedList<UIElement> fieldsList = new LinkedList<>();
            final int[] values = new int[targ.planeCount];

            final Runnable onChange = editor.createEditor(fieldsList, values, new Runnable() {
                @Override
                public void run() {
                    // -- Backup scroll values --
                    tabSlot.value = tabPane.getTabIndex();
                    tabCopySlot.value = tileGrid;
                    // -- Actually write data --
                    int base = tileGrid.getSelected();
                    int stride = tileGrid.getSelectStride();
                    for (int selY = 0; selY < tileGrid.selHeight; selY++) {
                        for (int selX = 0; selX < tileGrid.selWidth; selX++) {
                            int originalTileGridIndex = base + (stride * selY) + selX;
                            if ((originalTileGridIndex < 0) || (originalTileGridIndex >= tab.actTiles.length))
                                continue;
                            int originalTile = tab.actTiles[originalTileGridIndex];
                            writeData(originalTile);
                            // If this is an AT field tab, making a change here nukes everything
                            //  in the AT field.
                            if (tab.atProcessing)
                                for (AutoTileTypeField attf : atFields)
                                    if (attf.contains(originalTile))
                                        for (int st = 0; st < attf.length; st++)
                                            writeData(attf.start + st);
                        }
                    }
                    path.changeOccurred(false);
                }
                
                public void writeData(int tile) {
                    if ((tile < 0) || (tile >= targ.width))
                        return;
                    for (int p = 0; p < targ.planeCount; p++)
                        targ.setTiletype(tile, 0, p, (short) values[p]);
                }
            });
            fields.panelsSet(fieldsList);

            tileGrid.onSelectionChange = () -> {
                int tile = tab.actTiles[tileGrid.getSelected()];
                if ((tile < 0) || (tile >= targ.width))
                    return;
                for (int p = 0; p < targ.planeCount; p++)
                    values[p] = targ.getTiletype(tile, 0, p);
                onChange.run();
            };
            // Done to initialize the values
            tileGrid.onSelectionChange.run();

            UIElement uie = new UISplitterLayout(tileGrid, fields, false, 1) {
                @Override
                public String toString() {
                    return tileGrid.toString();
                }
            };
            
            tileTabElements[i] = uie;
            tileTabGrids[i] = tileGrid;
            
            tabPane.addTab(new UITabBar.Tab(uie, new UITabBar.TabIcon[] {}));
        }
        // -- Restore scroll values --
        int oldTabIndex = tabSlot.value;
        if (oldTabIndex < tileTabElements.length) {
            tabPane.selectTab(tileTabElements[oldTabIndex]);
            UITileGrid oldTabCopy = tabCopySlot.value;
            if (oldTabCopy != null) {
                tileTabGrids[oldTabIndex].setSelected(oldTabCopy.getSelected());
                tileTabGrids[oldTabIndex].selWidth = oldTabCopy.selWidth;
                tileTabGrids[oldTabIndex].selHeight = oldTabCopy.selHeight;
            }
        }
        return tabPane;
    }

}
