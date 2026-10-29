package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.component.schema.NewSchemaGroupCard;
import org.kaleta.accountant.frontend.component.schema.SchemaEditorRules;
import org.kaleta.accountant.frontend.component.card.CardCanvas;
import org.kaleta.accountant.frontend.component.card.CardStyle;
import org.kaleta.accountant.frontend.component.schema.SchemaGroupCard;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor of the chart of accounts: one tab per schema class, one card per group, one row per
 * account id.
 * <p>
 * Nothing here deletes. The schema is shared by every year and every account and transaction points
 * at one of its three digit ids, so the editor only adds groups and accounts into free slots and
 * renames what is already there. {@link SchemaEditorRules} decides which slots those are.
 */
public class SchemaEditor extends JTabbedPane implements Configurable {
    private Configuration configuration;
    private final Map<Integer, CardCanvas> classPanels = new LinkedHashMap<>();

    public SchemaEditor(Configuration configuration) {
        setConfiguration(configuration);
        initClassTabs();

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                SchemaEditor.this.update();
            }
        });

        update();
    }

    private void initClassTabs() {
        Map<Integer, SchemaModel.Class> classMap = Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear());
        for (Integer classId : SchemaEditorRules.CLASS_IDS) {
            SchemaModel.Class clazz = classMap.get(classId);
            if (clazz == null) {
                continue;
            }
            CardCanvas classPanel = new CardCanvas();

            JScrollPane scrollPane = new JScrollPane(classPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(CardStyle.ROW_HEIGHT);
            // the cards wrap into as many rows as the window is wide, so it never scrolls sideways
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            addTab(classId + "   " + clazz.getName(), scrollPane);
            classPanels.put(classId, classPanel);
        }
    }

    public void update() {
        Map<Integer, SchemaModel.Class> classMap = Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear());
        for (Map.Entry<Integer, CardCanvas> classEntry : classPanels.entrySet()) {
            classEntry.getValue().showCards(buildCards(classEntry.getKey(), classMap.get(classEntry.getKey())));
        }
    }

    /** A card per existing group, followed by the offer of the group slots still free. */
    private List<JComponent> buildCards(int classId, SchemaModel.Class clazz) {
        List<JComponent> cards = new ArrayList<>();
        List<Integer> freeGroupIds = new ArrayList<>();

        Map<Integer, SchemaModel.Class.Group> groupMap = Service.SCHEMA.getSchemaGroupMap(clazz);
        for (int groupId = 0; groupId <= SchemaEditorRules.LAST_ID; groupId++) {
            SchemaModel.Class.Group group = groupMap.get(groupId);
            if (group != null) {
                cards.add(new SchemaGroupCard(this, classId, group));
            } else if (SchemaEditorRules.canCreateGroup(classId, groupId)) {
                freeGroupIds.add(groupId);
            }
        }
        if (!freeGroupIds.isEmpty()) {
            cards.add(new NewSchemaGroupCard(this, classId, freeGroupIds));
        }
        return cards;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
