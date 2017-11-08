package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Dialog extends JDialog implements Configurable, DocumentListener, ActionListener {
    private Configuration configuration;
    protected boolean result;

    private JPanel contentPanel;
    private JButton buttonOk;

    private List<Validable> validableList;

    Dialog(Configuration configuration, String title, String confirmationLabel){
        super((Frame) configuration);
        setConfiguration(configuration);
        initComponents(title, confirmationLabel);
    }

    Dialog(Frame frame, String title, String confirmationLabel){
        super(frame);
        initComponents(title, confirmationLabel);
    }

    private void initComponents(String title, String confirmationLabel){
        validableList = new ArrayList<>();
        result = false;
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(a -> {
            result = false;
            dispose();
        });

        buttonOk = new JButton(confirmationLabel);
        buttonOk.setEnabled(false);
        buttonOk.addActionListener(a -> {
            result = true;
            dispose();
        });

        contentPanel = new JPanel();

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addComponent(contentPanel)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, Short.MAX_VALUE)
                                .addComponent(buttonCancel)
                                .addGap(5)
                                .addComponent(buttonOk)))
                .addGap(10));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(contentPanel)
                .addGap(5)
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonCancel)
                        .addComponent(buttonOk))
                .addGap(10));

        GroupLayout contentLayout = new GroupLayout(this.contentPanel);
        this.contentPanel.setLayout(contentLayout);
    }

    protected void setContentLayout(Consumer<GroupLayout> layout) {
        layout.accept((GroupLayout) contentPanel.getLayout());
    }

    private void validateSource(Object source){
        if (source instanceof Validable) {
            Validable validable = (Validable) source;
            if (!validableList.contains(validable)) validableList.add(validable);
        }


        for (Validable validable : validableList) {
            String errorMsg = validable.validator();
            if (errorMsg != null){
                buttonOk.setEnabled(false);
                buttonOk.setToolTipText(errorMsg);
                return;
            }
        }



        buttonOk.setToolTipText("");
        buttonOk.setEnabled(true);
    }

    public boolean getResult(){
        return result;
    }

    @Override
    public void setVisible(boolean b) {
        this.setLocation(getParent().getLocationOnScreen().x + getParent().getSize().width/2 - getSize().width/2,
                getParent().getLocationOnScreen().y + getParent().getSize().height/2 - getSize().height/2);
        buttonOk.grabFocus();
        buttonOk.requestFocus();
        buttonOk.transferFocus();

        super.setVisible(b);
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        validateSource(actionEvent.getSource());
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        validateSource(documentEvent.getDocument().getProperty("owner"));
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        validateSource(documentEvent.getDocument().getProperty("owner"));
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        validateSource(documentEvent.getDocument().getProperty("owner"));
    }
}
