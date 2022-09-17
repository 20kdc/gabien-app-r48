/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.toolsets.utils;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
import gabien.uslx.append.IConsumer;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.CMDB;
import r48.dbs.FormatSyntax;
import r48.dbs.RPGCommand;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.map.UIMapView;
import r48.map.systems.IRMMapSystem;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;

/**
 * Created on 14th September 2022 to house findTranslatables code.
 */
public class RMFindTranslatables {
    public final @NonNull String objIdName;
    public final @NonNull IObjectBackend.ILoadedObject objRoot;
    public final LinkedList<CommandSite> sites = new LinkedList<CommandSite>();

    public RMFindTranslatables(final IObjectBackend.ILoadedObject ilo) {
        objIdName = AppMain.objectDB.getIdByObjectOrThrow(ilo);
        objRoot = ilo;
    }

    public CommandSite[] toArray() {
        return sites.toArray(new CommandSite[0]);
    }

    public void addSitesFromMap(final @Nullable UIMapView ctx) {
        final SchemaPath rootSchemaPath = new SchemaPath(AppMain.schemas.getSDBEntry("RPG::Map"), objRoot);

        IRIO events = objRoot.getObject().getIVar("@events");
        for (IRIO eventKey : events.getHashKeys()) {
            IRIO event = events.getHashVal(eventKey);

            final SchemaPath eventSchemaPath = rootSchemaPath
                    .arrayHashIndex(eventKey, "E" + eventKey.toString())
                    .newWindow(AppMain.schemas.getSDBEntry("RPG::Event"), event);

            IRIO pageList = event.getIVar("@pages");
            for (int page = 0; page < pageList.getALen(); page++) {
                IRIO pageObj = pageList.getAElem(page);
                if (pageObj.getType() == '0')
                    continue;

                // enter page
                final SchemaPath pageSchemaPath = eventSchemaPath
                        .arrayHashIndex(new IRIOFixnum(page), "P" + page)
                        .newWindow(AppMain.schemas.getSDBEntry("RPG::EventPage"), pageObj);

                IRIO eventList = pageObj.getIVar("@list");
                addSitesFromList(getEventCommandArraySchemaElement("EventListEditor"), ctx, eventList, new SchemaPath[] {
                        rootSchemaPath,
                        eventSchemaPath,
                        pageSchemaPath
                });
            }
        }
    }

    public void addSitesFromCommonEvents(IRIO[] commonEvents) {
        SchemaElement sch = AppMain.schemas.findSchemaFor(objIdName, objRoot.getObject());
        if (sch == null) {
            AppMain.launchDialog(TXDB.get("Somehow, the common events file does not have a schema."));
        } else {
            SchemaPath rootSP = new SchemaPath(sch, objRoot);
            EventCommandArraySchemaElement ecase = RMFindTranslatables.getEventCommandArraySchemaElement("EventListEditor");
            for (IRIO rio : commonEvents) {
                SchemaElement cevElm = AppMain.schemas.findSchemaFor(null, rio);
                if (cevElm == null) {
                    AppMain.launchDialog(TXDB.get("Unable to determine common event schema, looks like that refactor will have to happen now"));
                    return;
                }
                SchemaPath commonEventSP = rootSP.newWindow(cevElm, rio);
                addSitesFromList(ecase, null, rio.getIVar("@list"), new SchemaPath[] {
                        rootSP,
                        commonEventSP
                });
            }
        }
    }

    public void addSitesFromList(final EventCommandArraySchemaElement cmdbEditor, final @Nullable UIMapView ctx, final IRIO eventList, final SchemaPath[] basePaths) {
        for (int i = 0; i < eventList.getALen(); i++) {
            IRIO cmd = eventList.getAElem(i);
            long cmdCode = cmd.getIVar("@code").getFX();
            RPGCommand cmdDetail = cmdbEditor.database.knownCommands.get((Integer) (int) cmdCode);
            if (cmdDetail.isTranslatable) {
                CommandSite tu = siteFromContext(cmdbEditor, ctx, eventList, i, cmd, basePaths);
                sites.add(tu);
            }
        }
    }

    public static EventCommandArraySchemaElement getEventCommandArraySchemaElement(String cmdbEditor) {
        return (EventCommandArraySchemaElement) AggregateSchemaElement.extractField(AppMain.schemas.getSDBEntry(cmdbEditor), null);
    }

    public static CommandSite siteFromContext(final EventCommandArraySchemaElement cmdbEditor, final @Nullable UIMapView mapView, final IRIO listObj, final int codeIndex, final IRIO command, final SchemaPath[] basePaths) {
        final CMDB cmdb = cmdbEditor.database;
        String text = cmdb.buildGroupCodename(listObj, codeIndex);
        final UITextButton button = new UITextButton(text, FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                ISchemaHost shi = new SchemaHostImpl(mapView);
                for (SchemaPath sp : basePaths)
                    shi.pushObject(sp);
                SchemaPath sp = shi.getCurrentObject();
                // enter list
                sp = sp.newWindow(cmdbEditor, listObj);
                shi.pushObject(sp);
                // enter command
                sp = sp.arrayHashIndex(new IRIOFixnum(codeIndex), "C" + codeIndex);
                sp = sp.newWindow(cmdbEditor.getElementContextualWindowSchema(command), listObj);
                shi.pushObject(sp);
            }
        });
        return new CommandSite(button) {
            @Override
            public void run() {
                int idx = EventCommandArraySchemaElement.findActualStart(listObj, command);
                if (idx != -1) {
                    button.text = cmdb.buildGroupCodename(listObj, codeIndex);
                } else {
                    button.text = cmdb.buildCodename(command, false);
                }
            }
        };
    }
}
