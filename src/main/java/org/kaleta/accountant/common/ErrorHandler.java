package org.kaleta.accountant.common;

import org.kaleta.accountant.frontend.common.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ErrorHandler {

    /**
     * Returns stack trace for specified exception as String.
     */
    public static String getThrowableStackTrace(Throwable throwable){
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        return writer.toString();
    }

    /**
     * Returns dialog window which shows error message (and details) for specified exception.
     */
    public static JDialog getThrowableDialog (Throwable throwable){
        return new ErrorDialog(getThrowableStackTrace(throwable));
    }

    private static class ErrorDialog extends JDialog {

        public ErrorDialog(String stackTrace) {
            this.setTitle("Application Failure!");
            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.setModal(true);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension dialogSize = this.getSize();
            int centerPosX = (screenSize.width - dialogSize.width) / 2;
            int centerPosY = (screenSize.height - dialogSize.height) / 2;
            this.setLocation(centerPosX, centerPosY);

            JLabel errorIcon = new JLabel(IconLoader.getIcon(IconLoader.ERROR_ICON,"error", new Dimension(50, 50)));

            JLabel labelError = new JLabel();
            labelError.setText(" A runtime error has occurred! Please send a bug report to the developer. ");
            labelError.setFont(new Font(labelError.getFont().getName(), Font.BOLD, 15));
            labelError.setOpaque(true);
            labelError.setBackground(Color.WHITE);

            JTextArea exceptionTextArea = new JTextArea();
            exceptionTextArea.setEditable(false);

            exceptionTextArea.setText(stackTrace);

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
            this.pack();
        }
    }
}
