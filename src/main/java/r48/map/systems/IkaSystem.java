/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.systems;

import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.map.events.IEventGraphicRenderer;
import r48.map.events.IkaEventGraphicRenderer;
import r48.map.imaging.CacheImageLoader;
import r48.map.imaging.FixAndSecondaryImageLoader;
import r48.map.imaging.GabienImageLoader;
import r48.map.tiles.ITileRenderer;
import r48.map.tiles.IkaTileRenderer;

/**
 * Created on 03/06/17.
 */
public class IkaSystem extends MapSystem {
    public IkaSystem() {
        super(new CacheImageLoader(new FixAndSecondaryImageLoader("Pbm/", "", new GabienImageLoader(".pbm", 0, 0, 0))));
    }

    @Override
    public StuffRenderer rendererFromMap(RubyIO map) {
        ITileRenderer tileRenderer = new IkaTileRenderer(imageLoader);
        IEventGraphicRenderer eventRenderer = new IkaEventGraphicRenderer(imageLoader);
        return new StuffRenderer(imageLoader, tileRenderer, eventRenderer, StuffRenderer.prepareTraditional(tileRenderer, new int[] {0}, eventRenderer, imageLoader, map, "Back", true, true, 0, 0, -1, -1, 1));
    }
}
