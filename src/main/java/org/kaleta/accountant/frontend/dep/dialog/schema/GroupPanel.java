package org.kaleta.accountant.frontend.dep.dialog.schema;

import org.kaleta.accountant.backend.entity.Schema;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 */
class GroupPanel extends JPanel {
    private final Schema.Class.Group group;
    private int width = 0;
    private JDialog parent;

    public GroupPanel(Schema.Class.Group group, JDialog parent) {
        this.group = group;
        this.parent = parent;
        initComponents();
        reset();
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.LIGHT_GRAY);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 1) {
                        setBackground(Color.LIGHT_GRAY);
                        GroupDialog dialog = new GroupDialog(group, GroupPanel.this);
                        dialog.setVisible(true);
                        if (dialog.getResult()) {
                            reset();
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((JPanel) e.getSource()).requestFocus();
                setBackground(Color.LIGHT_GRAY.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.LIGHT_GRAY);
            }
        });
    }

    private void reset() {
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.getHSBColor(200 / 360f, 1, 0.75f)),
                group.getId() + " - " + group.getName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new JLabel().getFont(),
                Color.getHSBColor(200 / 360f, 1f, 0.5f)));

        this.removeAll();
        revalidate();
        repaint();
        width = 0;
        for (int i = 0; i < group.getAccount().size(); i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = i;
            c.anchor = GridBagConstraints.PAGE_START;
            c.fill = GridBagConstraints.HORIZONTAL;
            if (i == group.getAccount().size() - 1) {
                c.weightx = 1;
                c.weighty = 1;
            }
            String text = group.getAccount().get(i).getId() + " " + group.getAccount().get(i).getType() + " - " + group.getAccount().get(i).getName();
            JLabel label = new JLabel(text);
            label.setForeground(Color.getHSBColor(290 / 360f, 1f, 0.5f));
            this.add(label, c);
            int tempWidth = label.getFontMetrics(label.getFont()).stringWidth(text) + 10;
            width = (tempWidth > width) ? tempWidth : width;
        }
        int titleWidth = new JLabel().getFontMetrics(new JLabel().getFont()).stringWidth(group.getId() + " - " + group.getName()) + 20;
        width = (titleWidth > width) ? titleWidth : width;

        revalidate();
        repaint();
        parent.pack();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(width, super.getMinimumSize().height);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(width, super.getMaximumSize().height);
    }
}
