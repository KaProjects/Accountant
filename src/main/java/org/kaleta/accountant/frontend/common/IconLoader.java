package org.kaleta.accountant.frontend.common;

import org.kaleta.accountant.frontend.Initializer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

/**
 * Created by Stanislav Kaleta on 16.04.2016.
 */
public class IconLoader {
    public static final String NO_ICON = "/icon/no_icon.png";
    public static final String ERROR_ICON = "/icon/error_icon.png";
    public static final String ADD = "/icon/add.png";
    public static final String DELETE = "/icon/delete.png";
    public static final String EDIT = "/icon/edit.png";
    public static final String TOGGLE_EXPAND = "/icon/toggleExpand.png";
    public static final String TOGGLE_HIDE = "/icon/toggleHide.png";


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
            Initializer.LOG.warning("Cant fined icon with path " + iconPath + " :\n" + ErrorDialog.getExceptionStackTrace(e));
            return new ImageIcon(IconLoader.class.getResource(NO_ICON),"no_icon");
        }
    }
}
