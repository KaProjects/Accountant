package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.common.Validable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Dialog extends JDialog implements Configurable, DocumentListener, ActionListener, ListSelectionListener {
    private Configuration configuration;
    boolean result;

    private JPanel contentPanel;
    private JButton buttonOk;
    private JPanel panelButtons;

    private final List<Validable> validableList;

    Dialog(Configuration configuration, String title, String confirmationLabel){
        super((Frame) configuration, title, true);
        validableList = new ArrayList<>();
        result = false;
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        String actionKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionKey);
        this.getRootPane().getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Dialog.this.dispatchEvent(new WindowEvent(Dialog.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        setConfiguration(configuration);
        initComponents(confirmationLabel);
    }

    private void initComponents(String confirmationLabel){
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
        GroupLayout contentLayout = new GroupLayout(this.contentPanel);
        this.contentPanel.setLayout(contentLayout);

        panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                        .addComponent(contentPanel)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panelButtons)
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
                        .addComponent(panelButtons)
                        .addComponent(buttonCancel)
                        .addComponent(buttonOk))
                .addGap(10));
    }

    private void validateSource(Object source){
        if (source instanceof Validable) {
            Validable validable = (Validable) source;
            if (!validableList.contains(validable)) validableList.add(validable);
        }
        validateDialog();
    }

    void setContent(Consumer<GroupLayout> layout) {
        layout.accept((GroupLayout) contentPanel.getLayout());
    }

    void setButtons(Consumer<JPanel> panelButtons) {
        panelButtons.accept(this.panelButtons);
    }

    void validateDialog(){
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

    void setDialogValid(String invalidMessage) {
        buttonOk.setEnabled(invalidMessage == null);
        buttonOk.setToolTipText(invalidMessage);
    }

    public boolean getResult(){
        return result;
    }

    @Override
    public void setVisible(boolean b) {
        this.setLocation(getParent().getLocationOnScreen().x + getParent().getSize().width/2 - getSize().width/2,
                getParent().getLocationOnScreen().y + getParent().getSize().height/2 - getSize().height/2);
        buttonOk.grabFocus();
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

    @Override
    public void valueChanged(ListSelectionEvent e) {
        validateSource(e.getSource());
    }
}
