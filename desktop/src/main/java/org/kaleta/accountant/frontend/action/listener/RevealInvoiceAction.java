package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.common.ErrorHandler;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.common.Edt;
import org.kaleta.accountant.service.Service;
import org.kaleta.accountant.service.ServiceFailureException;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

/**
 * Shows the invoice of an asset where it is kept, rather than opening it.
 * <p>
 * The document is read, printed and sent on in the file manager the machine already has, so the
 * app hands it over instead of trying to display it: the Finder - or whatever stands in for it -
 * opens with the file in view.
 */
public class RevealInvoiceAction extends ActionListener {
    private final AccountsModel.Account account;

    public RevealInvoiceAction(Configurable configurable, AccountsModel.Account account) {
        super(configurable);
        this.account = account;
    }

    @Override
    protected void actionPerformed() {
        File invoice = Service.INVOICE.getInvoice(account);
        if (invoice == null) {
            // the file was moved or deleted since the panel was drawn; the account still names it
            String name = Service.INVOICE.getInvoiceName(account);
            Edt.run(() -> JOptionPane.showMessageDialog((Frame) getConfiguration(),
                    "The invoice '" + name + "' is no longer in " + Service.INVOICE.getDirectory() + ".",
                    "Invoice", JOptionPane.WARNING_MESSAGE));
            return;
        }
        reveal(invoice);
    }

    /**
     * Puts the file in view in the file manager. Only macOS is asked for the file itself without
     * reservation; elsewhere the platform is asked whether it can do it, and the folder the file
     * sits in is the fallback, because {@code browseFileDirectory} does nothing at all on the
     * platforms that do not support it.
     */
    private void reveal(File file) {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
                new ProcessBuilder("open", "-R", file.getAbsolutePath()).start();
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                desktop.browseFileDirectory(file);
            } else {
                desktop.open(file.getParentFile());
            }
        } catch (IOException | UnsupportedOperationException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }
}
