package org.kaleta.accountant.frontend.component;

import org.kaleta.accountant.Initializer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * A field that holds a file the user picked: click it to browse, or drop the file onto it from the
 * Finder. Empty is a valid state - nothing here takes part in a dialog's validation - so a dialog
 * can offer a file without demanding one.
 * <p>
 * The field shows the file's name and keeps the file itself, because the name alone says nothing
 * about where it was dragged in from.
 */
public class SelectFileTextField extends JTextField {
    private static final String NOTHING_SELECTED = " - - Click to Select or Drop a File - - ";

    private File selectedFile;

    public SelectFileTextField() {
        this.setEditable(false);
        this.setSelectedFile(null);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    File chosen = chooseFile(SelectFileTextField.this);
                    if (chosen != null) {
                        setSelectedFile(chosen);
                    }
                }
            }
        });
        this.setTransferHandler(new FileDropHandler());
    }

    /**
     * Asks for a file. Returns null when the user cancels.
     * <p>
     * Must be called from the event thread: a file chooser built on a worker thread deadlocks
     * against its own directory scan, which is what {@link org.kaleta.accountant.frontend.common.Edt}
     * is for.
     */
    public static File chooseFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(Initializer.DEFAULT_FILES_DIR));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        int result = fileChooser.showOpenDialog(parent);
        return result == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile() : null;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File file) {
        if (file == null) {
            this.selectedFile = null;
            this.setText(NOTHING_SELECTED);
            this.setForeground(Color.GRAY);
            this.setToolTipText(null);
            return;
        }
        this.selectedFile = file;
        this.setText(file.getName());
        this.setForeground(new JTextField().getForeground());
        this.setToolTipText(file.getAbsolutePath());
    }

    /** Takes a single file dropped from outside the app; a folder or a selection of several is not one. */
    private class FileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport ts) {
            return ts.isDrop() && ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport ts) {
            if (!canImport(ts)) {
                return false;
            }
            try {
                @SuppressWarnings("unchecked")
                List<File> files = (List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (files.size() != 1 || !files.get(0).isFile()) {
                    return false;
                }
                setSelectedFile(files.get(0));
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
