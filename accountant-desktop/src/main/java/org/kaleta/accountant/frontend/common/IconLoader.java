package org.kaleta.accountant.frontend.common;

import org.kaleta.accountant.Initializer;
import org.kaleta.accountant.common.ErrorHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class IconLoader {
    private static final String NO_ICON = "/icon/no_icon.png";
    public static final String ERROR_ICON = "/icon/error_icon.png";
    public static final String ADD = "/icon/add.png";
    public static final String DELETE = "/icon/delete.png";
    public static final String EDIT = "/icon/edit.png";
    public static final String TOGGLE_EXPAND = "/icon/toggleExpand.png";
    public static final String TOGGLE_HIDE = "/icon/toggleHide.png";
    public static final String CHART = "/icon/chart.png";
    public static final String WARNING = "/icon/warning.png";
    public static final String SAVE_CHANGES = "/icon/savechanges.png";
    public static final String DISCARD_CHANGES = "/icon/discardchanges.png";


    public static Icon getIcon(String iconPath){
        return getIcon(iconPath, "", null);
    }

    public static Icon getIcon(String iconPath, String description){
        return getIcon(iconPath, description, null);
    }

    public static Icon getIcon(String iconPath, Dimension size){
        return getIcon(iconPath, "", size);
    }

    public static Icon getIcon(String iconPath, String description, Dimension size){
        try {
            Image img = ImageIO.read(IconLoader.class.getResource(iconPath));
            if (size != null){
                img = img.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
            }
            return new ImageIcon(img, description);
        } catch (IOException e) {
            Initializer.LOG.warning("Cant fined icon with path " + iconPath + " :\n" + ErrorHandler.getThrowableStackTrace(e));
            return new ImageIcon(IconLoader.class.getResource(NO_ICON),"no_icon");
        }
    }
}
