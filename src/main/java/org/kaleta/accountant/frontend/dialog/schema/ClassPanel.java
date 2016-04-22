package org.kaleta.accountant.frontend.dialog.schema;

import org.kaleta.accountant.backend.entity.Schema;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 */
class ClassPanel extends JPanel {
    private final Schema.Class clazz;
    private JDialog parent;

    public ClassPanel(Schema.Class clazz, JDialog parent) {
        this.clazz = clazz;
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
                        ClassDialog dialog = new ClassDialog(clazz, ClassPanel.this);
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
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.getHSBColor(160 / 360f, 1, 0.75f)),
                clazz.getId() + " - " + clazz.getName(),
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new JLabel().getFont(),
                Color.getHSBColor(160 / 360f, 1f, 0.5f)));

        this.removeAll();
        for (int i = 0; i < clazz.getGroup().size(); i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i;
            c.gridy = 0;
            c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.LINE_START;
            if (i == clazz.getGroup().size() - 1) {
                c.weightx = 1;
                c.weighty = 1;
            }
            this.add(new GroupPanel(clazz.getGroup().get(i), parent), c);
        }
        revalidate();
        repaint();
        parent.pack();
    }
}
