package org.kaleta.accountant.frontend.dep.dialog.schema;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.frontend.dialog.Dialog;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 */
@Deprecated
public class SchemaDialog extends Dialog {
    private final Schema schema;

    public SchemaDialog(Frame parent, Schema schema) {
        super(parent, "Schema Editor");
        this.schema = schema;
        buildDialog();
        this.pack();
    }

    @Override
    protected void buildDialog() {
        JPanel panelClasses = new JPanel();
        panelClasses.setLayout(new BoxLayout(panelClasses, BoxLayout.Y_AXIS));
        panelClasses.add(new ClassPanel(schema.getClazz().get(0), this));
        panelClasses.add(new ClassPanel(schema.getClazz().get(1), this));
        panelClasses.add(new ClassPanel(schema.getClazz().get(2), this));
        panelClasses.add(new ClassPanel(schema.getClazz().get(3), this));
        panelClasses.add(new ClassPanel(schema.getClazz().get(4), this));
        panelClasses.add(new ClassPanel(schema.getClazz().get(5), this));
        panelClasses.add(new ClassPanel(schema.getClazz().get(6), this));
        panelClasses.add(new ClassPanel(schema.getClazz().get(7), this));

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> {
            this.dispose();
        });
        JButton buttonOk = new JButton("Save");
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(panelClasses)
                .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, Short.MAX_VALUE).addComponent(buttonCancel).addGap(5).addComponent(buttonOk).addGap(5)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelClasses)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonCancel).addComponent(buttonOk))
                .addGap(5));
    }
}
