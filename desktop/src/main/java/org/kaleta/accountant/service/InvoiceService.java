package org.kaleta.accountant.service;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.common.ErrorHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The invoices of the fixed assets: the document that says what an asset cost and when.
 * <p>
 * An invoice is a file in the data directory's {@code invoices} folder, and the asset account
 * refers to it by file name alone - never by a path - so that moving or restoring the data
 * directory moves the invoices with it. The reference is kept in the account's metadata under the
 * {@code invoice} key rather than in an attribute of its own, because only the assets of class 0
 * ever have an invoice and an attribute would sit empty on every other account in the books.
 * <p>
 * Only the year whose books are open is written. A year already closed keeps the asset without its
 * invoice, which costs nothing: closing a year copies an account's metadata into the account it
 * opens in the next one, so every year opened from here on carries the invoice by itself.
 */
public class InvoiceService {

    InvoiceService(){
        // package-private
    }

    /** The folder the invoices live in. It is created by the resource check at startup. */
    public File getDirectory(){
        return new File(Initializer.getDataSource() + Constants.Data.INVOICES_DIR);
    }

    /** The file name recorded for this account, or null when it has no invoice. */
    public String getInvoiceName(AccountsModel.Account account){
        for (String entry : metadataEntries(account.getMetadata())) {
            if (entry.startsWith(Constants.Account.INVOICE_METADATA_KEY + "=")) {
                String name = entry.substring(Constants.Account.INVOICE_METADATA_KEY.length() + 1).trim();
                return name.isEmpty() ? null : name;
            }
        }
        return null;
    }

    /**
     * The invoice of this account as a file, or null when it has none or the file it names is gone.
     * <p>
     * A reference without a file behaves as no invoice at all, so that an invoice attached by
     * mistake can be replaced: deleting the file leaves the asset asking for one again.
     */
    public File getInvoice(AccountsModel.Account account){
        String name = getInvoiceName(account);
        if (name == null) {
            return null;
        }
        File file = new File(getDirectory(), name);
        return file.isFile() ? file : null;
    }

    /**
     * Copies this file into the invoices folder and records it as the account's invoice. Returns
     * the name it was stored under.
     * <p>
     * The stored name carries the account's id, so that the folder can be read on its own and so
     * that two assets cannot claim the same file. Attaching another invoice to the same asset
     * overwrites its own file when the name is the same one, and leaves the previous file behind
     * when it is not.
     */
    public String attachInvoice(String year, AccountsModel.Account account, File source){
        if (source == null || !source.isFile()) {
            throw new ServiceFailureException("'" + source + "' is not a file");
        }
        try {
            File directory = getDirectory();
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("the invoices directory could not be created: " + directory);
            }
            String name = storedName(account, source.getName());
            Files.copy(source.toPath(), new File(directory, name).toPath(), StandardCopyOption.REPLACE_EXISTING);

            Service.ACCOUNT.setMetadata(year, account.getFullId(), withInvoice(account.getMetadata(), name));
            account.setMetadata(withInvoice(account.getMetadata(), name));
            Initializer.LOG.info("Invoice '" + name + "' attached to account id=" + account.getFullId());
            return name;
        } catch (IOException e) {
            Initializer.LOG.severe(ErrorHandler.getThrowableStackTrace(e));
            throw new ServiceFailureException(e);
        }
    }

    /**
     * What the invoice of this asset is called once it is in the folder: the account's id, so the
     * folder says which asset each file belongs to, and the name it was dropped in under, so it
     * still says what the document is. Anything a file name cannot hold is replaced.
     */
    private String storedName(AccountsModel.Account account, String originalName){
        return account.getFullId() + "-" + originalName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /**
     * The metadata with this invoice in it. Whatever else the metadata held is kept exactly as it
     * was: this service understands one key out of a bag that other parts of the app write too.
     */
    String withInvoice(String metadata, String fileName){
        List<String> entries = new ArrayList<>();
        for (String entry : metadataEntries(metadata)) {
            if (!entry.startsWith(Constants.Account.INVOICE_METADATA_KEY + "=")) {
                entries.add(entry);
            }
        }
        entries.add(Constants.Account.INVOICE_METADATA_KEY + "=" + fileName);
        return String.join(",", entries);
    }

    private List<String> metadataEntries(String metadata){
        List<String> entries = new ArrayList<>();
        if (metadata == null) {
            return entries;
        }
        for (String entry : metadata.split(",")) {
            if (!entry.trim().isEmpty()) {
                entries.add(entry.trim());
            }
        }
        return entries;
    }
}
