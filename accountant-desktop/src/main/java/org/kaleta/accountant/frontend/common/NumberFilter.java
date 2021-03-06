package org.kaleta.accountant.frontend.common;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class NumberFilter extends DocumentFilter {
    private boolean suppressed = false;

    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string,
                             AttributeSet attr) throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if (test(sb.toString()) || suppressed) {
            super.insertString(fb, offset, string, attr);
        } else {
            warnUser();
        }
    }


    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if (test(sb.toString()) || suppressed) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            warnUser();
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length)
            throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if (test(sb.toString()) || suppressed) {
            super.remove(fb, offset, length);
        } else {
            warnUser();
        }
    }

    private boolean test(String text) {
        if(text.equals("")){
            return true;
        } else {
            try {
                int a = Integer.parseInt(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private void warnUser(){
        JOptionPane.showMessageDialog(null, "Only numbers are allowed to enter!","Wrong Number Format", JOptionPane.WARNING_MESSAGE);
    }
}
