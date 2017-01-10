package org.kaleta.accountant.frontend.component.year;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.common.IconLoader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Stanislav Kaleta on 09.01.2017.
 */
public class SchemaEditor {
    private YearModel model;

    private JPanel overview;
    private List<JPanel> editorClassPanels;

    public SchemaEditor(YearModel model){
        this.model = model;
    }


    public JComponent getOverview(){
        overview = new JPanel();
        overview.setLayout(new GridBagLayout());
        return overview;
    }

    public JComponent getEditor(){
        JTabbedPane editor = new JTabbedPane();
        editorClassPanels = new ArrayList<>();
        for (int c=0;c<=7;c++) {
            JPanel classPanel = new JPanel();
            editor.addTab(model.getClasses().get(c).getName(), classPanel);
            classPanel.setLayout(new GridBagLayout());
            editorClassPanels.add(classPanel);
        }
        return editor;
    }

    public void updateOverview(){
        overview.removeAll();
        for (int c=0;c<10;c++){
            YearModel.Clazz clazz = model.getClasses().get(c);
            if (clazz == null){
                continue;
            }
            JPanel clazzPanel = new JPanel();
            clazzPanel.setLayout(new GridBagLayout());
            clazzPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.getHSBColor(160 / 360f, 1, 0.75f)),
                    clazz.getName(),
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    new JLabel().getFont(),
                    Color.getHSBColor(160 / 360f, 1f, 0.5f)));
            for (int g=0;g<10;g++){
                YearModel.Clazz.Group group = clazz.getGroups().get(g);
                if (group == null){
                    continue;
                }
                JPanel groupPanel = new JPanel();
                groupPanel.setLayout(new GridBagLayout());
                groupPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.getHSBColor(200 / 360f, 1, 0.75f)),
                        group.getName(),
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        new JLabel().getFont(),
                        Color.getHSBColor(200 / 360f, 1f, 0.5f)));
                int width = new JLabel().getFontMetrics(new JLabel().getFont()).stringWidth(group.getName()) + 20;
                for (int a=0;a<10;a++){
                    YearModel.Clazz.Group.Account account = group.getAccounts().get(a);
                    if (account == null){
                        continue;
                    }
                    String text = account.getType() + " " + account.getName();
                    JLabel label = new JLabel(text);
                    int labelWidth = label.getFontMetrics(label.getFont()).stringWidth(text) + 10;
                    width = (labelWidth > width) ? labelWidth : width;
                    groupPanel.add(label, new GridBagConstraints(0,a,1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                }
                groupPanel.add(new JLabel(), new GridBagConstraints(0,10,1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                groupPanel.setMinimumSize(new Dimension(width,0));
                groupPanel.setPreferredSize(new Dimension(width,groupPanel.getPreferredSize().height));
                clazzPanel.add(groupPanel, new GridBagConstraints(g,0,1,1,0,0,GridBagConstraints.LINE_START,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0));
            }
            clazzPanel.add(new JLabel(), new GridBagConstraints(10,0,1,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0));
            overview.add(clazzPanel, new GridBagConstraints(0,c,1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        }
        overview.add(new JPanel(), new GridBagConstraints(0,10,1,1,1,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
    }

    public void updateEditor(Configurable configurable){
        // TODO: 1/10/17 restirctions to ids  c0 do 8 c1 do 7 etc.

        for (JPanel classPanel : editorClassPanels) {
            classPanel.removeAll();
            for (int g = 0; g < 9; g++) {
                GridBagConstraints groupConstraints = new GridBagConstraints(g, 0, 1, 1, 1, 1, GridBagConstraints.LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
                YearModel.Clazz.Group group = model.getClasses().get(editorClassPanels.indexOf(classPanel)).getGroups().get(g);
                if (group == null) {
                    JButton buttonAddGroup = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(15, 15)));
                    buttonAddGroup.setBackground(Color.LIGHT_GRAY);
                    buttonAddGroup.addActionListener(l -> {
                        // TODO: 1/10/17 create group action and update DS, this
                        System.out.println("ASdasd");
                    });
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
                    buttonEditGroup.addActionListener(l -> {
                        // TODO: 1/10/17 edit group name -  update DS, this
                    });
                    groupPanel.add(buttonEditGroup, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

                    JButton buttonDelGroup = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
                    buttonDelGroup.setBackground(Color.GRAY);
                    buttonDelGroup.setMinimumSize(new Dimension(25, 25));
                    buttonDelGroup.setPreferredSize(new Dimension(25, 25));
                    buttonDelGroup.setMaximumSize(new Dimension(25, 25));
                    buttonDelGroup.addActionListener(l -> {
                        // TODO: 1/10/17 check if deletable -> update DS, this | message
                    });
                    groupPanel.add(buttonDelGroup, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

                    int width = new JLabel().getFontMetrics(new JLabel().getFont()).stringWidth(group.getName()) + 20;
                    for (int a = 0; a < 10; a++) {
                        GridBagConstraints accConstraints = new GridBagConstraints(0, a + 1, 3, 1, 1, 1, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
                        YearModel.Clazz.Group.Account account = group.getAccounts().get(a);
                        if (account == null) {
                            JButton buttonAddAccount = new JButton(IconLoader.getIcon(IconLoader.ADD, new Dimension(10, 10)));
                            buttonAddAccount.setBackground(Color.LIGHT_GRAY);
                            buttonAddAccount.addActionListener(l -> {
                                // TODO: 1/10/17 create acc action and update DS, this
                            });
                            groupPanel.add(buttonAddAccount, accConstraints);
                        } else {
                            String text = " " + account.getType() + " " + account.getName() + " ";
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
                            buttonEdit.addActionListener(l -> {
                                // TODO: 1/10/17 edit acc update DS, this
                            });
                            accPanel.add(buttonEdit);

                            JButton buttonDel = new JButton(IconLoader.getIcon(IconLoader.DELETE, new Dimension(10, 10)));
                            buttonDel.setBackground(Color.GRAY);
                            buttonDel.setMinimumSize(new Dimension(25, 25));
                            buttonDel.setPreferredSize(new Dimension(25, 25));
                            buttonDel.setMaximumSize(new Dimension(25, 25));
                            buttonDel.addActionListener(l -> {
                                // TODO: 1/10/17 check if deletable -> update DS, this | message
                            });
                            accPanel.add(buttonDel);

                            groupPanel.add(accPanel, accConstraints);
                        }
                    }
                    groupPanel.setMinimumSize(new Dimension(width, 0));
                    groupPanel.setPreferredSize(new Dimension(width, groupPanel.getPreferredSize().height));
                    classPanel.add(groupPanel, groupConstraints);
                }
            }
        }
    }
}
