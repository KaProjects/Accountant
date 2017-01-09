package org.kaleta.accountant.frontend.component.year;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Created by Stanislav Kaleta on 09.01.2017.
 */
public class SchemaEditor {
    private YearModel.Schema schema;

    public SchemaEditor(YearModel.Schema schema){
        this.schema = schema;
    }


    public JPanel getOverview(){
        JPanel background = new JPanel();
        background.setLayout(new GridBagLayout());
        for (int c=0;c<10;c++){
            YearModel.Schema.Clazz clazz = schema.getClasses().get(c);
            if (clazz == null){
                continue;
            }
            JPanel clazzPanel = new JPanel();
            clazzPanel.setLayout(new GridBagLayout());
            clazzPanel.setBorder(BorderFactory.createTitledBorder(clazz.getName()));
            for (int g=0;g<10;g++){
                YearModel.Schema.Clazz.Group group = clazz.getGroups().get(g);
                if (group == null){
                    continue;
                }
                JPanel groupPanel = new JPanel();
                groupPanel.setLayout(new GridBagLayout());
                groupPanel.setBorder(BorderFactory.createTitledBorder(group.getName()));
                int width = new JLabel().getFontMetrics(new JLabel().getFont()).stringWidth(group.getName()) + 20;
                for (int a=0;a<10;a++){
                    YearModel.Schema.Clazz.Group.Account account = group.getAccounts().get(a);
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
            background.add(clazzPanel, new GridBagConstraints(0,c,1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        }
        background.add(new JPanel(), new GridBagConstraints(0,10,1,1,1,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        return background;
    }

    public JPanel getEditor(){
        return new JPanel();
    }
}
