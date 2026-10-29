package org.kaleta.accountant.frontend.component.accounts;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.action.listener.CreateAnalyticalAccountAction;
import org.kaleta.accountant.frontend.action.listener.RenameAccountAction;
import org.kaleta.accountant.frontend.common.IconLoader;
import org.kaleta.accountant.frontend.component.card.CardRow;
import org.kaleta.accountant.frontend.component.card.CardStyle;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * One schema account, drawn as a card: its three digit id and name in the header, the accounts
 * opened under it stacked beneath, and a row at the end for opening another one.
 */
class SchemaAccountCard extends CardStyle.Card {

    SchemaAccountCard(Configurable owner, String schemaId, String schemaAccountName) {
        super(CardStyle.HEADER_BG);
        add(header(schemaId, schemaAccountName), BorderLayout.NORTH);
        add(accounts(owner, schemaId), BorderLayout.CENTER);
    }

    private JPanel header(String schemaId, String schemaAccountName) {
        JPanel header = CardStyle.header();
        header.add(CardStyle.id(schemaId, CardStyle.ID_FG));
        header.add(Box.createHorizontalStrut(8));

        JLabel name = new JLabel(schemaAccountName);
        name.setFont(CardStyle.titleFont());
        name.setForeground(CardStyle.NAME_FG);
        header.add(name);
        header.add(Box.createHorizontalGlue());
        return header;
    }

    private JPanel accounts(Configurable owner, String schemaId) {
        JPanel accounts = new JPanel();
        accounts.setLayout(new BoxLayout(accounts, BoxLayout.Y_AXIS));
        accounts.setOpaque(false);
        accounts.setBorder(BorderFactory.createEmptyBorder(5, 4, 7, 4));

        List<AccountsModel.Account> opened = new ArrayList<>(
                Service.ACCOUNT.getAccountsBySchemaId(owner.getConfiguration().getSelectedYear(), schemaId));
        // by id, so a card reads in the order the accounts were opened rather than however the
        // service happened to return them
        opened.sort(Comparator.comparingInt(SchemaAccountCard::semanticOrder));
        for (AccountsModel.Account account : opened) {
            accounts.add(accountRow(owner, account));
        }
        if (AccountsEditorRules.canCreateAccount(schemaId)) {
            accounts.add(newAccountRow(owner, schemaId, opened.isEmpty()));
        }
        accounts.add(Box.createVerticalGlue());
        return accounts;
    }

    /** A semantic id is a number, sometimes with a suffix such as "5-2020" on a retired account. */
    private static int semanticOrder(AccountsModel.Account account) {
        String digits = account.getSemanticId().split("-")[0];
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private CardRow accountRow(Configurable owner, AccountsModel.Account account) {
        CardRow row = new CardRow() { };
        row.add(CardStyle.id("." + account.getSemanticId(), CardStyle.ID_FG));
        row.add(Box.createHorizontalStrut(8));

        JLabel name = new JLabel(account.getName());
        name.setFont(CardStyle.nameFont());
        name.setForeground(CardStyle.NAME_FG);
        row.add(name);

        row.add(Box.createHorizontalStrut(8));
        row.add(Box.createHorizontalGlue());

        // clicking a row renames it, as a schema row or a procedure row does
        CardStyle.makeRowClickable(row, new RenameAccountAction(owner, account));
        return row;
    }

    private CardRow newAccountRow(Configurable owner, String schemaId, boolean isFirst) {
        CardRow row = new CardRow() { };
        row.add(CardStyle.id("  ", CardStyle.SLOT_FG));
        row.add(Box.createHorizontalStrut(8));

        JLabel hint = new JLabel(isFirst ? "new account (general)" : "new account");
        hint.setFont(CardStyle.hintFont());
        hint.setForeground(CardStyle.SLOT_FG);
        row.add(hint);

        row.add(Box.createHorizontalGlue());
        row.add(CardStyle.icon(IconLoader.ADD, 10));
        row.add(Box.createHorizontalStrut(2));

        CardStyle.makeRowClickable(row, new CreateAnalyticalAccountAction(owner, schemaId, isFirst));
        return row;
    }
}
