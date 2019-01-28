package org.kaleta.accountant.frontend;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.frontend.action.menu.*;
import org.kaleta.accountant.frontend.common.MenuItemWrapper;
import org.kaleta.accountant.frontend.component.YearMenu;
import org.kaleta.accountant.frontend.core.YearPane;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AppFrame extends JFrame implements Configuration {
    private String selectedYear;

    public AppFrame(){
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle(Initializer.NAME +" - "+ Initializer.VERSION);

        initMenuBar();
        initComponents();
        applySettings();
        this.pack();
        this.setSize(1800,1000);

        int centerPosX = (Toolkit.getDefaultToolkit().getScreenSize().width - this.getSize().width) / 2;
        int centerPosY = (Toolkit.getDefaultToolkit().getScreenSize().height - this.getSize().height) / 2;
        this.setLocation(centerPosX, centerPosY);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(new MenuItemWrapper(new OpenYearClosingDialog(this)));
        fileMenu.add(new JSeparator(SwingConstants.HORIZONTAL));
        fileMenu.add(new MenuItemWrapper(new PerformExit(this), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)));


        JMenu addMenu = new JMenu("Add");
        addMenu.setMnemonic(KeyEvent.VK_A);
        addMenu.add(new MenuItemWrapper(new OpenAddAssetDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK)));
        addMenu.add(new MenuItemWrapper(new OpenAddResourcesDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK)));
        addMenu.add(new MenuItemWrapper(new OpenAddTransactionDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK)));

        JMenu createMenu = new JMenu("Create");
        createMenu.setMnemonic(KeyEvent.VK_C);
        createMenu.add(new MenuItemWrapper(new OpenCreateProcedureDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK)));

        JMenu importMenu = new JMenu("Import");
        importMenu.setMnemonic(KeyEvent.VK_I);
        importMenu.add(new MenuItemWrapper(new OpenImportTransactionsDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK)));

        menuBar.add(fileMenu);
        menuBar.add(addMenu);
        menuBar.add(createMenu);
        menuBar.add(importMenu);
        menuBar.add(new JPanel());
        menuBar.add(new YearMenu());
    }

    private void initComponents( ){
        this.getContentPane().setLayout(new GridLayout(1,1));
        this.add(new YearPane());
    }

    private void applySettings(){
        update(Configuration.INIT_CONFIG);
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
}
