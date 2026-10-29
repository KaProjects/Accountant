package org.kaleta.accountant.frontend.dialog;

import org.kaleta.accountant.backend.model.AccountsModel;
import org.kaleta.accountant.common.Constants;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.component.card.CardStyle;
import org.kaleta.accountant.frontend.component.table.TableStyle;
import org.kaleta.accountant.service.ClosingService;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Closing a year, in three steps: what it would do, what the next year should start with, and what
 * is about to happen.
 * <p>
 * Nothing is written while this is open. The first step works the closing out and says whether the
 * books close; the second is the only decision there is - which accounts come over, since one that
 * ends the year empty is only worth carrying if it will be used again; the third says what will
 * happen, and is the only place the button that does it can be pressed.
 */
public class YearClosingDialog extends Dialog {
    private static final int CHECKS = 0;
    private static final int ACCOUNTS = 1;
    private static final int CONFIRMATION = 2;

    /** How deep in the chart a line of the account list sits. */
    private static final int CLASS = 0;
    private static final int GROUP = 1;
    private static final int SCHEMA_ACCOUNT = 2;
    private static final int ACCOUNT = 3;

    private final String year;
    private final CardLayout steps = new CardLayout();
    private final JPanel body = new JPanel(steps);

    private JPanel panelChecks;
    private JPanel panelAccounts;
    private JTextArea textConfirmation;
    private JButton buttonBack;
    private JButton buttonNext;

    private final Map<String, JCheckBox> choices = new LinkedHashMap<>();
    private final List<Entry> entries = new ArrayList<>();

    private ClosingService.Plan plan;
    private int step = CHECKS;

    public YearClosingDialog(Configuration configuration, String year) {
        super(configuration, "Closing " + year, "Close Year");
        this.year = year;
        buildDialogContent();
        // there is nothing to ask before checking: the year to close and the one that follows it are
        // both known, so the dialog opens with the answer already worked out
        check();
        showStep(CHECKS);
        pack();
        fitWithinTheWindow();
    }

    /**
     * The list of accounts is long, and the height it is given is the height there is: as tall as
     * the window the dialog belongs to, and no taller. What the packed dialog asks for is ignored -
     * a list shown through a scroll pane asks for all of itself.
     */
    private void fitWithinTheWindow() {
        Window owner = getOwner();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = owner == null ? screen.width : owner.getWidth();
        int height = owner == null ? Math.min(Math.max(getHeight(), 520), screen.height) : owner.getHeight();
        setSize(Math.min(Math.max(getWidth(), 640), maxWidth), height);
    }

    private void buildDialogContent() {
        body.add(checksStep(), String.valueOf(CHECKS));
        body.add(accountsStep(), String.valueOf(ACCOUNTS));
        body.add(confirmationStep(), String.valueOf(CONFIRMATION));

        buttonBack = new JButton("Back");
        buttonBack.addActionListener(e -> showStep(step - 1));
        buttonNext = new JButton("Next");
        buttonNext.addActionListener(e -> showStep(step + 1));

        setContent(layout -> {
            layout.setHorizontalGroup(layout.createParallelGroup().addComponent(body));
            layout.setVerticalGroup(layout.createSequentialGroup().addComponent(body));
        });
        setButtons(panel -> {
            panel.add(buttonBack);
            panel.add(buttonNext);
        });
    }

    private JPanel checksStep() {
        JLabel title = title("1. What closing " + year + " would do");

        JLabel labelNewYear = new JLabel("The year that follows it:");
        JLabel valueNewYear = new JLabel(nextYear());
        valueNewYear.setFont(CardStyle.titleFont());
        valueNewYear.setForeground(CardStyle.NAME_FG);

        panelChecks = new JPanel();
        panelChecks.setLayout(new BoxLayout(panelChecks, BoxLayout.Y_AXIS));
        panelChecks.setOpaque(false);

        JPanel step = new JPanel(new BorderLayout(0, 10));
        step.setBorder(new EmptyBorder(4, 4, 4, 4));
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(title, BorderLayout.NORTH);
        JPanel name = new JPanel();
        name.setLayout(new BoxLayout(name, BoxLayout.X_AXIS));
        name.add(labelNewYear);
        name.add(Box.createHorizontalStrut(8));
        name.add(valueNewYear);
        name.add(Box.createHorizontalGlue());
        top.add(name, BorderLayout.SOUTH);
        step.add(top, BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(panelChecks);
        pane.setPreferredSize(new Dimension(600, 340));
        step.add(pane, BorderLayout.CENTER);
        return step;
    }

    private JPanel accountsStep() {
        panelAccounts = new JPanel();
        panelAccounts.setLayout(new BoxLayout(panelAccounts, BoxLayout.Y_AXIS));
        panelAccounts.setBackground(Color.WHITE);

        JPanel step = new JPanel(new BorderLayout(0, 8));
        step.setBorder(new EmptyBorder(4, 4, 4, 4));
        step.add(title("2. What " + year + " leaves to the new year"), BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(panelAccounts);
        // what the list asks for is the whole chart of accounts; what it gets is a window onto it
        pane.setPreferredSize(new Dimension(600, 340));
        step.add(pane, BorderLayout.CENTER);
        return step;
    }

    private JPanel confirmationStep() {
        textConfirmation = new JTextArea();
        textConfirmation.setEditable(false);
        textConfirmation.setFont(CardStyle.baseFont());
        textConfirmation.setBorder(new EmptyBorder(6, 8, 6, 8));

        JPanel step = new JPanel(new BorderLayout(0, 8));
        step.setBorder(new EmptyBorder(4, 4, 4, 4));
        step.add(title("3. What will happen"), BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(textConfirmation);
        pane.setPreferredSize(new Dimension(600, 340));
        step.add(pane, BorderLayout.CENTER);
        return step;
    }

    /** Works the closing out and says whether it can happen; nothing is written by this. */
    private void check() {
        plan = Service.CLOSING.prepare(year, getNewYear());
        panelChecks.removeAll();

        panelChecks.add(line(plan.isBalanced()
                ? "The books close with a " + (plan.getProfit() < 0 ? "loss" : "profit") + " of " + Math.abs(plan.getProfit())
                : "The books do not close", plan.isBalanced()));
        panelChecks.add(line(plan.getClosingTransactions().size()
                + " closing transactions would be booked into " + year, true));
        panelChecks.add(line(plan.getCandidates().size() + " accounts to choose from for " + getNewYear(), true));
        for (String problem : plan.getProblems()) {
            panelChecks.add(line(problem, false));
        }
        panelChecks.add(Box.createVerticalGlue());
        panelChecks.revalidate();
        panelChecks.repaint();

        showAccounts();
        allowClosing();
    }

    /**
     * The accounts to decide about, under the class, the group and the schema account they belong
     * to, the way the chart reads. Any of those three can be folded away once it has been decided
     * about, to leave the rest of the list in view.
     * <p>
     * Everything starts ticked, because the next year is this year's chart; clearing a box is what
     * leaves an account behind. What holds a balance cannot be cleared - the balance has to go
     * somewhere - and what is not listed at all is not a decision: the fixed assets, everything
     * derived from another account, and the capital the books stand on.
     */
    private void showAccounts() {
        panelAccounts.removeAll();
        choices.clear();
        entries.clear();

        List<ClosingService.Candidate> candidates = new ArrayList<>(plan.getCandidates());
        candidates.sort(Comparator.comparing(candidate -> candidate.getAccount().getFullId()));

        String shownClass = null;
        String shownGroup = null;
        String shownSchemaAccount = null;
        for (ClosingService.Candidate candidate : candidates) {
            AccountsModel.Account account = candidate.getAccount();
            if (!account.getClassId().equals(shownClass)) {
                add(header(CLASS, Service.SCHEMA.getSchemaClassMap(year)
                        .get(Integer.parseInt(account.getClassId())).getName(), null), CLASS, true);
                shownClass = account.getClassId();
                shownGroup = null;
                shownSchemaAccount = null;
            }
            if (!account.getGroupId().equals(shownGroup)) {
                add(header(GROUP, Service.SCHEMA.getGroupName(year, account.getClassId(), account.getGroupId()),
                        account.getClassId() + account.getGroupId()), GROUP, true);
                shownGroup = account.getGroupId();
                shownSchemaAccount = null;
            }
            if (!account.getSchemaId().equals(shownSchemaAccount)) {
                add(header(SCHEMA_ACCOUNT, Service.SCHEMA.getAccountName(year, account.getClassId(),
                        account.getGroupId(), account.getSchemaAccountId()), account.getSchemaId()), SCHEMA_ACCOUNT, true);
                shownSchemaAccount = account.getSchemaId();
            }
            add(accountRow(candidate), ACCOUNT, false);
        }
        panelAccounts.add(Box.createVerticalGlue());

        refreshFolding();
        panelAccounts.revalidate();
        panelAccounts.repaint();
    }

    private void add(JComponent component, int depth, boolean isHeader) {
        entries.add(new Entry(component, depth, isHeader));
        panelAccounts.add(component);
    }

    /**
     * A class, a group or a schema account, as the chart names it. Clicking it folds everything
     * below it away, and the chevron says which way it is.
     */
    private JPanel header(int depth, String name, String id) {
        JLabel chevron = new JLabel("\u25be");
        chevron.setFont(CardStyle.hintFont());
        chevron.setForeground(CardStyle.ID_FG);

        JLabel label = new JLabel(name);
        label.setFont(depth == SCHEMA_ACCOUNT ? CardStyle.nameFont() : CardStyle.titleFont());
        label.setForeground(headerColour(depth));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(depth == CLASS ? TableStyle.TOTAL_BG
                : depth == GROUP ? TableStyle.CLASS_BG : TableStyle.GROUP_BG);
        header.setBorder(BorderFactory.createCompoundBorder(TableStyle.rowBorder(),
                new EmptyBorder(3, 6 + depth * 14, 3, 6)));
        header.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
        header.add(chevron);
        header.add(Box.createHorizontalStrut(6));
        if (id != null) {
            header.add(CardStyle.id(id, CardStyle.ID_FG));
            header.add(Box.createHorizontalStrut(8));
        }
        header.add(label);
        header.add(Box.createHorizontalGlue());

        CardStyle.makeClickable(header, e -> fold(header, chevron), () -> { }, () -> { });
        return header;
    }

    private Color headerColour(int depth) {
        switch (depth) {
            case CLASS: return Constants.Color.OVERVIEW_CLASS;
            case GROUP: return Constants.Color.OVERVIEW_GROUP;
            default: return CardStyle.NAME_FG;
        }
    }

    private void fold(JPanel header, JLabel chevron) {
        for (Entry entry : entries) {
            if (entry.component == header) {
                entry.folded = !entry.folded;
                chevron.setText(entry.folded ? "\u25b8" : "\u25be");
            }
        }
        refreshFolding();
        panelAccounts.revalidate();
        panelAccounts.repaint();
    }

    /** A row is shown while nothing above it is folded; a fold at any level hides all of it. */
    private void refreshFolding() {
        boolean[] foldedAt = new boolean[ACCOUNT];
        for (Entry entry : entries) {
            boolean hidden = false;
            for (int depth = 0; depth < entry.depth; depth++) {
                hidden = hidden || foldedAt[depth];
            }
            entry.component.setVisible(!hidden);
            if (entry.header) {
                foldedAt[entry.depth] = entry.folded;
                for (int deeper = entry.depth + 1; deeper < ACCOUNT; deeper++) {
                    foldedAt[deeper] = false;
                }
            }
        }
    }

    /** One line of the list, and how deep in the chart it sits. */
    private static class Entry {
        private final JComponent component;
        private final int depth;
        private final boolean header;
        private boolean folded;

        Entry(JComponent component, int depth, boolean header) {
            this.component = component;
            this.depth = depth;
            this.header = header;
        }
    }

    /**
     * One account to decide about. Everything starts checked, because the next year is this one's
     * chart of accounts; unchecking is what leaves an account behind. The ones holding a balance
     * cannot be dropped at all - the balance has to go somewhere.
     */
    private JPanel accountRow(ClosingService.Candidate candidate) {
        String fullId = candidate.getAccount().getFullId();
        JCheckBox box = new JCheckBox();
        box.setOpaque(false);
        box.setSelected(true);
        box.setEnabled(!candidate.isRequired());
        box.addActionListener(e -> {
            if (!box.isSelected()) {
                warnAboutProcedures(fullId);
            }
        });
        choices.put(fullId, box);

        JLabel id = CardStyle.id(fullId, CardStyle.ID_FG);
        JLabel name = new JLabel(candidate.getAccount().getName());
        name.setFont(CardStyle.baseFont());
        JLabel value = new JLabel(describe(candidate));
        value.setFont(CardStyle.hintFont());
        value.setForeground(candidate.isRequired() ? TableStyle.TEXT : CardStyle.MUTED_FG);

        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(TableStyle.rowBorder(),
                new EmptyBorder(2, 4 + ACCOUNT * 14, 2, 6)));
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));
        row.add(box);
        row.add(id);
        row.add(Box.createHorizontalStrut(8));
        row.add(name);
        row.add(Box.createHorizontalStrut(8));
        row.add(Box.createHorizontalGlue());
        row.add(value);
        return row;
    }

    private String describe(ClosingService.Candidate candidate) {
        if (candidate.isRequired()) {
            return "carries " + candidate.getClosingValue();
        }
        return candidate.isUsed() ? candidate.getClosingValue() + " this year" : "unused";
    }

    /**
     * Leaving an account behind that one of the user's own procedures books is worth stopping for:
     * the procedure was composed by hand and will have to be looked at. The ones the app wrote for
     * that account are not - they go with it, and step three says so.
     */
    private void warnAboutProcedures(String fullId) {
        List<String> procedures = Service.PROCEDURES.getProceduresBooking(year, fullId);
        if (procedures.isEmpty()) {
            return;
        }
        JOptionPane.showMessageDialog(this,
                "'" + fullId + "' is booked by " + String.join(", ", procedures)
                        + ".\nLeaving it behind takes it out of " + (procedures.size() == 1 ? "that procedure" : "those procedures")
                        + " when the year is closed.",
                "The account is used by a procedure", JOptionPane.WARNING_MESSAGE);
    }

    private void showConfirmation() {
        int carried = Service.CLOSING.carriedAccounts(plan, getCarried()).size();
        int dropped = Service.CLOSING.droppedAccounts(plan, getCarried()).size();
        // what the closing would do to the procedures, worked out the way it will do it
        List<String> procedures = Service.PROCEDURES.previewRemovalOf(
                Service.CLOSING.droppedAccounts(plan, getCarried()));

        StringBuilder text = new StringBuilder();
        text.append("Closing ").append(year).append("\n");
        text.append("    ").append(plan.getClosingTransactions().size())
                .append(" transactions dated 31.12., one for each account and one for the profit\n");
        text.append("    the ").append(plan.getProfit() < 0 ? "loss" : "profit").append(" of ")
                .append(Math.abs(plan.getProfit())).append(" onto the accumulated earnings\n");

        text.append("\nOpening ").append(getNewYear()).append("\n");
        text.append("    ").append(carried).append(" accounts carried over\n");
        text.append("    ").append(dropped).append(" accounts left behind\n");

        if (!plan.getWrittenOffAssets().isEmpty()) {
            text.append("\nFixed assets worth nothing at the end of ").append(year)
                    .append(", written off with their depreciation accounts\n");
            for (AccountsModel.Account asset : plan.getWrittenOffAssets()) {
                text.append("    ").append(asset.getFullId()).append("   ").append(asset.getName()).append("\n");
            }
        }

        if (!procedures.isEmpty()) {
            text.append("\nProcedures that book an account being left behind\n");
            for (String procedure : procedures) {
                text.append("    ").append(procedure).append("\n");
            }
        }

        text.append("\nNothing has been written yet. Closing the year writes all of it.");
        textConfirmation.setText(text.toString());
        textConfirmation.setCaretPosition(0);
    }

    private void showStep(int which) {
        step = Math.max(CHECKS, Math.min(CONFIRMATION, which));
        if (step == CONFIRMATION) {
            showConfirmation();
        }
        steps.show(body, String.valueOf(step));
        // there is nowhere to go back from the first step, and nowhere further from the last
        buttonBack.setEnabled(step > CHECKS);
        buttonNext.setEnabled(step < CONFIRMATION);
        allowClosing();
    }

    /**
     * Walking through the steps is free - looking at what would happen changes nothing. What the
     * checks decide is whether the year can be closed at all, and that is the only thing the
     * closing button follows.
     */
    private void allowClosing() {
        boolean closable = plan != null && plan.isClosable();
        setDialogValid(closable ? null : "The checks have to pass first");
    }

    private JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(CardStyle.titleFont());
        label.setForeground(CardStyle.NAME_FG);
        return label;
    }

    private JPanel line(String text, boolean good) {
        JLabel label = new JLabel((good ? "✓  " : "✗  ") + text);
        label.setFont(CardStyle.baseFont());
        label.setForeground(good ? TableStyle.REVENUE_FG : TableStyle.EXPENSE_FG);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 4, 3, 4));
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
        row.add(label, BorderLayout.WEST);
        return row;
    }

    private String nextYear() {
        try {
            return String.valueOf(Integer.parseInt(year) + 1);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    /** Always the year that follows the one being closed; there is nothing else it could be. */
    public String getNewYear() {
        return nextYear();
    }

    /** The accounts the new year is to start with. */
    public List<String> getCarried() {
        List<String> carried = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> choice : choices.entrySet()) {
            if (choice.getValue().isSelected()) {
                carried.add(choice.getKey());
            }
        }
        return carried;
    }

    public ClosingService.Plan getPlan() {
        return plan;
    }
}
