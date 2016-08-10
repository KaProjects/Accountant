package org.kaleta.accountant.frontend;

import org.kaleta.accountant.frontend.action.menu.*;
import org.kaleta.accountant.frontend.common.MenuItemWrapper;
import org.kaleta.accountant.frontend.component.BalanceTable;
import org.kaleta.accountant.frontend.component.JournalTable;
import org.kaleta.accountant.frontend.component.ProfitTable;

import javax.swing.*;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 */
public class AppFrame extends JFrame implements Configuration{

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
        update(Configuration.INIT_CONFIG);
        applySettings();
        this.pack();
    }

    private void initComponents( ){
        JTabbedPane tabbedPane = new JTabbedPane();

        JScrollPane journalPane = new JScrollPane(new JournalTable());
        tabbedPane.addTab("Journal", journalPane);

        JScrollPane balancePane = new JScrollPane(new BalanceTable());
        tabbedPane.addTab("Balance", balancePane);

        JScrollPane profitPane = new JScrollPane(new ProfitTable());
        tabbedPane.addTab("Profit", profitPane);





        this.getContentPane().setLayout(new GridLayout(1,1));
        this.add(tabbedPane);
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
        fileMenu.add(new MenuItemWrapper(new OpenSchemaDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK)));
        fileMenu.add(new MenuItemWrapper(new OpenSemanticDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_MASK)));
        fileMenu.add(new MenuItemWrapper(new OpenAddTransactionDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_MASK)));
        fileMenu.add(new MenuItemWrapper(new OpenCreateProcedureDialog(this), KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.SHIFT_MASK)));
        fileMenu.add(new MenuItemWrapper(new OpenProceduresDialog(this)));
        fileMenu.add(new MenuItemWrapper(new OpenAccountPreview(this)));
        fileMenu.add(new JSeparator());
        fileMenu.add(new MenuItemWrapper(new PerformExit(this), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)));
    }

    private void applySettings(){
        // TODO: 8/3/16 load settings and fire update(comand)


        update(Configuration.TRANSACTION_ACTION);


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
}
