package org.kaleta.accountant.frontend;

import org.kaleta.accountant.frontend.action.menu.*;
import org.kaleta.accountant.frontend.common.MenuItemWrapper;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 */
public class AppFrame extends JFrame implements Configuration{

    public AppFrame(){
        initMenuBar();
        initComponents();
        this.pack();
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension frameSize = this.getSize();
//        int centerPosX = (screenSize.width - frameSize.width) / 2;
//        int centerPosY = (screenSize.height - frameSize.height) / 2;
//        this.setLocation(centerPosX, centerPosY);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle(Initializer.NAME +" - "+ Initializer.VERSION);
    }

    private void initComponents( ){



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
        fileMenu.add(new JSeparator());
        fileMenu.add(new MenuItemWrapper(new PerformExit(this), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)));
    }
}
