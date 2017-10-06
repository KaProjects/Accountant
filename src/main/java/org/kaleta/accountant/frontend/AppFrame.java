package org.kaleta.accountant.frontend;

import org.kaleta.accountant.frontend.action.menu.*;
import org.kaleta.accountant.frontend.common.MenuItemWrapper;
import org.kaleta.accountant.frontend.component.YearMenu;
import org.kaleta.accountant.frontend.component.YearPane;
import org.kaleta.accountant.frontend.year.model.YearModel;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AppFrame extends JFrame implements Configuration{
    private String selectedYear;
    private YearModel model; // TODO: 3.10.2017 to del

    public AppFrame(){
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension frameSize = this.getSize();
//        int centerPosX = (screenSize.width - frameSize.width) / 2;
//        int centerPosY = (screenSize.height - frameSize.height) / 2;
//        this.setLocation(centerPosX, centerPosY);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle(Initializer.NAME +" - "+ Initializer.VERSION);

        initMenuBar();
        initComponents();
        applySettings();
        this.pack();
        this.setSize(1100,1000);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);
        JMenu newMenu = new JMenu("Add");
        fileMenu.add(newMenu);
        fileMenu.add(new JSeparator());
        // TODO: 3.10.2017 add menus
        fileMenu.add(new JSeparator());
        fileMenu.add(new MenuItemWrapper(new PerformExit(this), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)));

        // TODO: 3.10.2017 to del
        JMenu depMenu = new JMenu("dep");
        menuBar.add(depMenu);
        depMenu.add(new MenuItemWrapper(new OpenSchemaDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK)));
        depMenu.add(new MenuItemWrapper(new OpenSemanticDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_MASK)));
        depMenu.add(new MenuItemWrapper(new OpenAddTransactionDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_MASK)));
        depMenu.add(new MenuItemWrapper(new OpenCreateProcedureDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK)));
        depMenu.add(new MenuItemWrapper(new OpenProceduresDialog(this)));
        depMenu.add(new MenuItemWrapper(new OpenAccountPreview(this)));

        menuBar.add(new JPanel());
        menuBar.add(new YearMenu());
    }

    private void initComponents( ){
        this.getContentPane().setLayout(new GridLayout(1,1));

//        JTabbedPane tabbedPane = new JTabbedPane();
//
//        JScrollPane journalPane = new JScrollPane(new JournalTable());
//        tabbedPane.addTab("Journal", journalPane);
//
//        JScrollPane balancePane = new JScrollPane(new BalanceTable());
//        tabbedPane.addTab("Balance", balancePane);
//
//        JScrollPane profitPane = new JScrollPane(new ProfitTable());
//        tabbedPane.addTab("Profit", profitPane);
//        this.add(tabbedPane);
        // TODO: 3.10.2017 to del - old layout

        this.add(new YearPane());
    }

    private void applySettings(){
        update(Configuration.INIT_CONFIG);


        // TODO: 3.10.2017 to del
        model = Service.YEAR.getYearModel(Integer.parseInt(Service.CONFIG.getActiveYear()));


        update(Configuration.YEAR_ADDED); // to init Year Menu
        selectYear(Service.CONFIG.getActiveYear());



    }

    private void updateComponent(JComponent component,int command) {
        Action action = component.getActionMap().get(command);
        if (action != null){
            ActionEvent actionEvent = new ActionEvent(this,0,null);
            action.actionPerformed(actionEvent);
        }

        for (Component child : component.getComponents()) {
            try {
                updateComponent((JComponent) child, command);
            } catch (ClassCastException ex){
                // continue
            }
        }
    }

    @Override
    public void update(int command) {
        for (Component component : this.getComponents()) {
            updateComponent((JComponent) component, command);
        }
    }

    @Override
    public void selectYear(String year){
        selectedYear = year;
        update(Configuration.YEAR_SELECTED);
    }

    @Override
    public String getSelectedYear() {
        return selectedYear;
    }

    @Override
    public YearModel getModel() {
        return model;
    }
}
