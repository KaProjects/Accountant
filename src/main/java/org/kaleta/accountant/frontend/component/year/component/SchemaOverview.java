package org.kaleta.accountant.frontend.component.year.component;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.component.year.model.SchemaModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Created by Stanislav Kaleta on 14.02.2017.
 */
public class SchemaOverview extends JPanel implements Configurable {
    private Configuration configuration;


    public SchemaOverview(){
        this.setLayout(new GridBagLayout());

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                SchemaOverview.this.update();
            }
        });
    }

    public void update(){
        this.removeAll();
        for (int c=0;c<10;c++){
            SchemaModel.Clazz clazz = getConfiguration().getModel().getSchemaModel().getClasses().get(c);
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
                SchemaModel.Clazz.Group group = clazz.getGroups().get(g);
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
                    SchemaModel.Clazz.Group.Account account = group.getAccounts().get(a);
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
            this.add(clazzPanel, new GridBagConstraints(0,c,1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        }
        this.add(new JPanel(), new GridBagConstraints(0,10,1,1,1,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
        this.revalidate();
        this.repaint();
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
