package org.kaleta.accountant.frontend;

import org.kaleta.accountant.frontend.action.menu.PerformExit;
import org.kaleta.accountant.frontend.common.MenuItemWrapper;
import org.kaleta.accountant.frontend.component.YearMenu;
import org.kaleta.accountant.frontend.core.YearPane;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class AppFrame extends JFrame implements Configuration{
    private String selectedYear;

    public AppFrame(){
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle(Initializer.NAME +" - "+ Initializer.VERSION);

        initMenuBar();
        initComponents();
        applySettings();
        this.pack();
        this.setSize(1100,1000);

        int centerPosX = (Toolkit.getDefaultToolkit().getScreenSize().width - this.getSize().width) / 2;
        int centerPosY = (Toolkit.getDefaultToolkit().getScreenSize().height - this.getSize().height) / 2;
        this.setLocation(centerPosX, centerPosY);
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
        // TODO 1.0 : add more menus for action: add asset, resource, transaction...
        fileMenu.add(new JSeparator());
        fileMenu.add(new MenuItemWrapper(new PerformExit(this), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)));

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
