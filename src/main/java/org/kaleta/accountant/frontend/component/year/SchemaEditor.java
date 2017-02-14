package org.kaleta.accountant.frontend.component.year;

import org.kaleta.accountant.frontend.component.year.model.SchemaModel;

import javax.swing.*;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Stanislav Kaleta on 09.01.2017.
 */
@Deprecated
public class SchemaEditor {
    private SchemaModel model;

    private Map<Integer, JPanel> editorClassPanels;

    public SchemaEditor(SchemaModel model){
        this.model = model;
    }


    public JComponent getEditor(){
        JTabbedPane editor = new JTabbedPane();
        editorClassPanels = new HashMap<>();
        for (int cid : new int[]{0,1}) {
            JPanel classPanel = new JPanel();
            editor.addTab(model.getClasses().get(cid).getName(), classPanel);
            classPanel.setLayout(new GridBagLayout());
            editorClassPanels.put(cid, classPanel);
        }
        return editor;
    }

    public void updateEditor(){

    }

    private void createGroupPanels(int cId){

    }
}
