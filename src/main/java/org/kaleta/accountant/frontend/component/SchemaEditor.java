package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.action.listener.SchemaEditorAccountAction;
import org.kaleta.accountant.frontend.action.listener.SchemaEditorGroupAction;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SchemaEditor extends JTabbedPane implements Configurable {
    private Configuration configuration;
    // [classId, firstEditableGroupId, lastEditableGroupId] = couple of first/last groups can be forbidden to edit
    private int[][] classesDef = new int[][]{{0,0,8},{1,0,9},{2,0,9},{3,2,9},{4,1,9},{5,2,9},{6,0,9}};
    private Map<Integer, JPanel> editorClassPanels;

    public SchemaEditor(Configuration configuration){
        setConfiguration(configuration);
        editorClassPanels = new HashMap<>();
        Map<Integer, SchemaModel.Class> classMap = Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear());
        for (int[] cidArray : classesDef) {
            JPanel classPanel = new JPanel();
            this.addTab(classMap.get(cidArray[0]).getName(), classPanel);
            classPanel.setLayout(new GridBagLayout());
            editorClassPanels.put(cidArray[0], classPanel);
        }

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                SchemaEditor.this.update();
            }
        });
    }

    public void update() {
        Map<Integer, SchemaModel.Class> classMap = Service.SCHEMA.getSchemaClassMap(getConfiguration().getSelectedYear());
        for (int[] cIdArray : classesDef) {
            JPanel classPanel = editorClassPanels.get(cIdArray[0]);
            classPanel.removeAll();
            for (int gId = cIdArray[1]; gId <= cIdArray[2]; gId++) {
                GridBagConstraints groupConstraints = new GridBagConstraints(gId, 0, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
                SchemaModel.Class.Group group = Service.SCHEMA.getSchemaGroupMap(classMap.get(cIdArray[0])).get(gId);
                if (group == null) {
                    JButton buttonAddGroup = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(15, 15)));
                    buttonAddGroup.setBackground(Color.LIGHT_GRAY);
                    buttonAddGroup.addActionListener(new SchemaEditorGroupAction(this, SchemaEditorGroupAction.CREATE, cIdArray[0], gId));
                    classPanel.add(buttonAddGroup, groupConstraints);
                } else {
                    JPanel groupPanel = new JPanel();
                    groupPanel.setLayout(new GridBagLayout());
                    groupPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), group.getName()));
                    groupPanel.setBackground(Color.GRAY);

                    JButton buttonEditGroup = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(10, 10)));
                    buttonEditGroup.setBackground(Color.GRAY);
                    buttonEditGroup.setMinimumSize(new Dimension(25, 25));
                    buttonEditGroup.setPreferredSize(new Dimension(25, 25));
                    buttonEditGroup.setMaximumSize(new Dimension(25, 25));
                    buttonEditGroup.addActionListener(new SchemaEditorGroupAction(this, SchemaEditorGroupAction.EDIT, cIdArray[0], gId));
                    groupPanel.add(buttonEditGroup, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

                    JButton buttonDelGroup = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
                    buttonDelGroup.setBackground(Color.GRAY);
                    buttonDelGroup.setMinimumSize(new Dimension(25, 25));
                    buttonDelGroup.setPreferredSize(new Dimension(25, 25));
                    buttonDelGroup.setMaximumSize(new Dimension(25, 25));
                    buttonDelGroup.addActionListener(new SchemaEditorGroupAction(this, SchemaEditorGroupAction.DELETE, cIdArray[0], gId));
                    groupPanel.add(buttonDelGroup, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

                    int width = new JLabel().getFontMetrics(new JLabel().getFont()).stringWidth(group.getName()) + 20;
                    for (int aId = 0; aId < 10; aId++) {
                        GridBagConstraints accConstraints = new GridBagConstraints(0, aId + 1, 3, 1, 1, 0, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
                        SchemaModel.Class.Group.Account account = Service.SCHEMA.getSchemaAccountMap(group).get(aId);
                        if (account == null) {
                            JButton buttonAddAccount = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(10, 10)));
                            buttonAddAccount.setMinimumSize(new Dimension(25, 25));
                            buttonAddAccount.setPreferredSize(new Dimension(25, 25));
                            buttonAddAccount.setMaximumSize(new Dimension(25, 25));
                            buttonAddAccount.setBackground(Color.LIGHT_GRAY);
                            buttonAddAccount.addActionListener(new SchemaEditorAccountAction(this, SchemaEditorAccountAction.CREATE, cIdArray[0], gId, aId));
                            groupPanel.add(buttonAddAccount, accConstraints);
                        } else {
                            String text = "   " + account.getName() + "  ";
                            JLabel label = new JLabel(text);
                            int labelWidth = label.getFontMetrics(label.getFont()).stringWidth(text) + 10 + 50;
                            width = (labelWidth > width) ? labelWidth : width;

                            JPanel accPanel = new JPanel();
                            accPanel.setLayout(new BoxLayout(accPanel, BoxLayout.X_AXIS));
                            accPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                            accPanel.setBackground(Color.GRAY);
                            accPanel.add(label);
                            accPanel.add(Box.createHorizontalGlue());

                            JButton buttonEdit = new JButton(IconLoader.getIcon(IconLoader.EDIT, new Dimension(10, 10)));
                            buttonEdit.setBackground(Color.GRAY);
                            buttonEdit.setMinimumSize(new Dimension(25, 25));
                            buttonEdit.setPreferredSize(new Dimension(25, 25));
                            buttonEdit.setMaximumSize(new Dimension(25, 25));
                            buttonEdit.addActionListener(new SchemaEditorAccountAction(this, SchemaEditorAccountAction.EDIT, cIdArray[0], gId, aId));
                            accPanel.add(buttonEdit);

                            JButton buttonDel = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
                            buttonDel.setBackground(Color.GRAY);
                            buttonDel.setMinimumSize(new Dimension(25, 25));
                            buttonDel.setPreferredSize(new Dimension(25, 25));
                            buttonDel.setMaximumSize(new Dimension(25, 25));
                            buttonDel.addActionListener(new SchemaEditorAccountAction(this, SchemaEditorAccountAction.DELETE, cIdArray[0], gId, aId));
                            accPanel.add(buttonDel);

                            accPanel.setMinimumSize(new Dimension(accPanel.getPreferredSize().width, 25));
                            accPanel.setPreferredSize(new Dimension(accPanel.getPreferredSize().width, 25));
                            accPanel.setMaximumSize(new Dimension(accPanel.getPreferredSize().width, 25));

                            groupPanel.add(accPanel, accConstraints);
                        }
                    }
                    JPanel paddingPanel = new JPanel();
                    paddingPanel.setOpaque(false);
                    groupPanel.add(paddingPanel, new GridBagConstraints(0, 11, 3, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                    groupPanel.setMinimumSize(new Dimension(width, 0));
                    groupPanel.setPreferredSize(new Dimension(width, groupPanel.getPreferredSize().height));
                    classPanel.add(groupPanel, groupConstraints);
                }
            }
            classPanel.revalidate();
            classPanel.repaint();
        }
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
