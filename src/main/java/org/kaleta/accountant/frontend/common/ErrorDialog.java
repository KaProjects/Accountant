package org.kaleta.accountant.frontend.common;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 *
 * Dialog window which shows error message (and details) for specified exception.
 */
public class ErrorDialog extends JDialog {
    private final Throwable throwable;

    public ErrorDialog(Throwable throwable) {
        this.throwable = throwable;
        initComponents();
        this.setTitle("Application Failure!");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = this.getSize();
        int centerPosX = (screenSize.width - dialogSize.width) / 2;
        int centerPosY = (screenSize.height - dialogSize.height) / 2;
        this.setLocation(centerPosX, centerPosY);
    }

    private void initComponents() {
        JLabel errorIcon = new JLabel(IconLoader.getIcon(IconLoader.ERROR_ICON,"error", new Dimension(50, 50)));

        JLabel labelError = new JLabel();
        labelError.setText(" A runtime error has occurred! Please send a bug report to the developer. ");
        labelError.setFont(new Font(labelError.getFont().getName(), Font.BOLD, 15));
        labelError.setOpaque(true);
        labelError.setBackground(Color.WHITE);

        JTextArea exceptionTextArea = new JTextArea();
        exceptionTextArea.setEditable(false);

        exceptionTextArea.setText(getStackTrace(throwable));

        JScrollPane exceptionScrollPane = new JScrollPane(exceptionTextArea);
        exceptionScrollPane.setVisible(false);

        JToggleButton toggleButton = new JToggleButton("▿ More Details");
        toggleButton.addActionListener(e -> {
            JToggleButton source = (JToggleButton) e.getSource();
            if (source.isSelected()) {
                exceptionScrollPane.setVisible(true);
                //U+25B5 (White up-pointing small triangle ▵)
                source.setText("▵ Hide Details");
                ErrorDialog.this.pack();
            } else {
                exceptionScrollPane.setVisible(false);
                //U+25BF (White down-pointing small triangle ▿)
                source.setText("▿ More Details");
                ErrorDialog.this.pack();
            }
        });

        JButton buttonClose = new JButton("Close");
        buttonClose.addActionListener(e -> ErrorDialog.this.dispose());

        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addGap(10)
                        .addComponent(errorIcon, 50, 50, 50)
                        .addComponent(labelError)
                        .addGap(10))
                .addGroup(layout.createSequentialGroup()
                        .addGap(10)
                        .addComponent(toggleButton))
                .addComponent(exceptionScrollPane, 0, 590, Short.MAX_VALUE)
                .addComponent(buttonClose, GroupLayout.Alignment.CENTER));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(errorIcon, 50, 50, 50)
                        .addComponent(labelError, GroupLayout.Alignment.CENTER, 50, 50, 50))
                .addComponent(toggleButton, 20, 20, 20)
                .addComponent(exceptionScrollPane, 0, 300, Short.MAX_VALUE)
                .addGap(20)
                .addComponent(buttonClose)
                .addGap(10));
    }

    private static String getStackTrace(Throwable throwable){
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
    }

    /**
     * Returns stack trace for specified exception as String.
     */
    public static String getExceptionStackTrace(Exception exception){
        return getStackTrace(exception);
    }
}
