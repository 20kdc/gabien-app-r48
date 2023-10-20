/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UITextButton;
import r48.App;
import r48.dbs.CMDB;
import r48.dbs.RPGCommand;
import r48.io.IObjectBackend;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.map.UIMapView;
import r48.schema.AggregateSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.specialized.cmgb.EventCommandArraySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;

/**
 * Created on 14th September 2022 to house findTranslatables code.
 */
public class RMFindTranslatables extends App.Svc {
    public final @NonNull String objIdName;
    public final @NonNull IObjectBackend.ILoadedObject objRoot;
    public final LinkedList<CommandSite> sites = new LinkedList<CommandSite>();

    public RMFindTranslatables(App app, final IObjectBackend.ILoadedObject ilo) {
        super(app);
        objIdName = app.odb.getIdByObjectOrThrow(ilo);
        objRoot = ilo;
    }

    public CommandSite[] toArray() {
        return sites.toArray(new CommandSite[0]);
    }

    public void addSitesFromMap(final @Nullable UIMapView ctx, String eventPage, final ICommandClassifier cf) {
        final SchemaPath rootSchemaPath = new SchemaPath(app.sdb.getSDBEntry("RPG::Map"), objRoot);

        IRIO events = objRoot.getObject().getIVar("@events");
        for (DMKey eventKey : events.getHashKeys()) {
            IRIO event = events.getHashVal(eventKey);

            final SchemaPath eventSchemaPath = rootSchemaPath
                    .arrayHashIndex(eventKey, "E" + eventKey.toString())
                    .newWindow(app.sdb.getSDBEntry("RPG::Event"), event);

            IRIO pageList = event.getIVar("@pages");
            for (int page = 0; page < pageList.getALen(); page++) {
                IRIO pageObj = pageList.getAElem(page);
                if (pageObj.getType() == '0')
                    continue;

                // enter page
                final SchemaPath pageSchemaPath = eventSchemaPath
                        .arrayHashIndex(DMKey.of(page), "P" + page);

                IRIO eventList = pageObj.getIVar("@list");
                addSitesFromList(getEventCommandArraySchemaElement(app, "EventListEditor"), ctx, eventList, pageSchemaPath, cf);
            }
        }
    }

    public void addSitesFromCommonEvents(IRIO[] commonEvents, final ICommandClassifier cf) {
        SchemaElement sch = app.sdb.findSchemaFor(objIdName, objRoot.getObject());
        if (sch == null) {
            app.ui.launchDialog(T.u.cCommonEventsNoSchema);
        } else {
            SchemaPath rootSP = new SchemaPath(sch, objRoot);
            EventCommandArraySchemaElement ecase = RMFindTranslatables.getEventCommandArraySchemaElement(app, "EventListEditor");
            for (IRIO rio : commonEvents) {
                SchemaElement cevElm = app.sdb.findSchemaFor(null, rio);
                if (cevElm == null) {
                    app.ui.launchDialog(T.u.cCommonEventsNoSchema2);
                    return;
                }
                SchemaPath commonEventSP = rootSP.newWindow(cevElm, rio);
                addSitesFromList(ecase, null, rio.getIVar("@list"), commonEventSP, cf);
            }
        }
    }

    public void addSitesFromList(final EventCommandArraySchemaElement cmdbEditor, final @Nullable UIMapView ctx, final IRIO eventList, final SchemaPath basePath, final ICommandClassifier cf) {
        for (int i = 0; i < eventList.getALen(); i++) {
            IRIO cmd = eventList.getAElem(i);
            long cmdCode = cmd.getIVar("@code").getFX();
            RPGCommand cmdDetail = cmdbEditor.database.knownCommands.get((Integer) (int) cmdCode);
            if (cmdDetail.commandSiteAllowed && cf.matches(cmdDetail)) {
                CommandSite tu = siteFromContext(app, cmdbEditor, ctx, eventList, i, cmd, basePath);
                sites.add(tu);
            }
        }
    }

    public static EventCommandArraySchemaElement getEventCommandArraySchemaElement(App app, String cmdbEditor) {
        return (EventCommandArraySchemaElement) AggregateSchemaElement.extractField(app.sdb.getSDBEntry(cmdbEditor), null);
    }

    public static CommandSite siteFromContext(App app, final EventCommandArraySchemaElement cmdbEditor, final @Nullable UIMapView mapView, final IRIO listObj, final int codeIndex, final IRIO command, final SchemaPath basePath) {
        final CMDB cmdb = cmdbEditor.database;
        String text = cmdb.buildGroupCodename(listObj, codeIndex, true);
        final UITextButton button = new UITextButton(text, app.f.schemaFieldTH, () -> {
            ISchemaHost shi = new SchemaHostImpl(app, mapView);
            SchemaPath sp = basePath;
            // enter list
            sp = sp.tagSEMonitor(listObj, cmdbEditor, false);
            // enter command
            sp = sp.arrayHashIndex(DMKey.of(codeIndex), "C" + codeIndex);
            sp = sp.newWindow(cmdbEditor.getElementContextualWindowSchema(command), listObj);
            shi.pushPathTree(sp);
        });
        return new CommandSite(button) {
            @Override
            public void run() {
                int idx = EventCommandArraySchemaElement.findActualStart(listObj, command);
                if (idx != -1) {
                    button.text = cmdb.buildGroupCodename(listObj, codeIndex, true);
                } else {
                    button.text = cmdb.buildCodename(command, false, true);
                }
            }
        };
    }
}