package org.kaleta.accountant.frontend.core;

import org.kaleta.accountant.backend.model.SchemaModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.action.configuration.ConfigurationAction;
import org.kaleta.accountant.frontend.component.card.CardStyle;
import org.kaleta.accountant.frontend.component.schema.AccountTypeChip;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * The whole chart of accounts at a glance: the classes, the groups they hold, and every account in
 * them. Only the colours carry meaning here - a class, a group, and the side of the books an
 * account is on - because there is nothing to click.
 */
public class SchemaOverview extends JPanel implements Configurable {
    private Configuration configuration;

    public SchemaOverview(Configuration configuration){
        this.setLayout(new GridBagLayout());
        this.setBackground(CardStyle.CANVAS);

        this.getActionMap().put(Configuration.SCHEMA_UPDATED, new ConfigurationAction(this) {
            @Override
            protected void actionPerformed() {
                SchemaOverview.this.update();
            }
        });
        setConfiguration(configuration);
        update();
    }

    public void update(){
        this.removeAll();
        List<SchemaModel.Class> classList = Service.SCHEMA.getSchemaClassList(getConfiguration().getSelectedYear());
        for (SchemaModel.Class clazz : classList) {
            JPanel clazzPanel = new JPanel();
            clazzPanel.setLayout(new GridBagLayout());
            clazzPanel.setBackground(CardStyle.CANVAS);
            clazzPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(CardStyle.CARD_BORDER),
                    clazz.getName(),
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION,
                    CardStyle.titleFont(),
                    Constants.Color.OVERVIEW_CLASS));
            List<SchemaModel.Class.Group> groupList = clazz.getGroup();
            for (SchemaModel.Class.Group group : groupList) {
                JPanel groupPanel = new JPanel();
                groupPanel.setLayout(new GridBagLayout());
                groupPanel.setBackground(CardStyle.CARD_BG);
                groupPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(CardStyle.CARD_BORDER),
                        group.getName(),
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        CardStyle.baseFont(),
                        Constants.Color.OVERVIEW_GROUP));
                int width = new JLabel().getFontMetrics(new JLabel().getFont()).stringWidth(group.getName()) + 20;
                List<SchemaModel.Class.Group.Account> accountList = group.getAccount();
                for (SchemaModel.Class.Group.Account account : accountList) {
                    String text = "(" + account.getType() + ") " + account.getName();
                    JLabel label = new JLabel(text);
                    label.setFont(CardStyle.baseFont());
                    // the side of the books it is on, in the colours the schema editor uses for it
                    label.setForeground(AccountTypeChip.colourOf(account.getType()));
                    int labelWidth = label.getFontMetrics(label.getFont()).stringWidth(text) + 10;
                    width = (labelWidth > width) ? labelWidth : width;
                    groupPanel.add(label, new GridBagConstraints(0,accountList.indexOf(account),1,1,1,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                }
                groupPanel.add(new JLabel(), new GridBagConstraints(0,10,1,1,0,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
                groupPanel.setMinimumSize(new Dimension(width,0));
                groupPanel.setPreferredSize(new Dimension(width,groupPanel.getPreferredSize().height));
                clazzPanel.add(groupPanel, new GridBagConstraints(groupList.indexOf(group),0,1,1,0,0,GridBagConstraints.LINE_START,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0));
            }
            clazzPanel.add(new JLabel(), new GridBagConstraints(10,0,1,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.VERTICAL,new Insets(0,0,0,0),0,0));
            this.add(clazzPanel, new GridBagConstraints(0,classList.indexOf(clazz),1,1,0,0,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
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
