package org.kaleta.accountant.frontend.action.listener;

import org.kaleta.accountant.frontend.Configurable;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.service.Service;

import javax.swing.*;
import java.awt.*;

public class SchemaEditorGroupAction extends ActionListener {
    public static final int CREATE = 0;
    public static final int EDIT = 1;
    public static final int DELETE = 2;

    private int action;
    private String classId;
    private String groupId;

    public SchemaEditorGroupAction(Configurable configurable, int action, int classId, int groupId) {
        super(configurable);
        this.action = action;
        this.classId = String.valueOf(classId);
        this.groupId = String.valueOf(groupId);
    }

    @Override
    protected void actionPerformed() {
        switch (action) {
            case 0: {
                String name = JOptionPane.showInputDialog((Component) getConfiguration(), "Set Group Name");
                if (name != null && !name.trim().isEmpty()) {
                    Service.SCHEMA.createGroup(getConfiguration().getSelectedYear(), classId, groupId, name);
                    getConfiguration().update(Configuration.SCHEMA_UPDATED);
                }
                break;
            }
            case 1: {
                String newName = JOptionPane.showInputDialog((Component) getConfiguration(), "Set Group Name",
                        Service.SCHEMA.getGroupName(getConfiguration().getSelectedYear(), classId, groupId));
                if (newName != null && !newName.trim().isEmpty()) {
                    Service.SCHEMA.renameGroup(getConfiguration().getSelectedYear(), classId, groupId, newName);
                    getConfiguration().update(Configuration.SCHEMA_UPDATED);
                }
                break;
            }
            case 2: {
                if (Service.SCHEMA.isGroupDeletable(getConfiguration().getSelectedYear(), classId, groupId)) {
                    if (JOptionPane.showConfirmDialog((Component) getConfiguration(),
                            "Are you sure you want to delete '"
                                    + Service.SCHEMA.getGroupName(getConfiguration().getSelectedYear(), classId, groupId)
                                    + "'?", "Deleting Group", JOptionPane.YES_NO_OPTION) == 0) {
                        Service.SCHEMA.deleteGroup(getConfiguration().getSelectedYear(), classId, groupId);
                        getConfiguration().update(Configuration.SCHEMA_UPDATED);
                    }
                } else {
                    JOptionPane.showMessageDialog((Component) getConfiguration(),
                            "Group cannot be deleted, because at least one associated account is opened!",
                            "Forbidden Operation!",
                            JOptionPane.WARNING_MESSAGE);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Illegal action identifier! value=" + action);
        }
    }
}
