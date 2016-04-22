package org.kaleta.accountant.frontend.action.menu;

import org.kaleta.accountant.backend.entity.Schema;
import org.kaleta.accountant.frontend.Configuration;
import org.kaleta.accountant.frontend.dialog.schema.SchemaDialog;
import org.kaleta.accountant.service.Service;

import java.awt.Component;

/**
 * Created by Stanislav Kaleta on 19.04.2016.
 */
public class OpenSchemaDialog extends MenuAction {
    public OpenSchemaDialog(Configuration config) {
        super(config, "Schema");
    }

    @Override
    protected void actionPerformed() {
        Schema schema = Service.SCHEMA.getSchema();
        SchemaDialog dialog = new SchemaDialog((Component) getConfiguration(), schema);
        dialog.setVisible(true);
        if (dialog.getResult()) {
            Service.SCHEMA.setSchema(schema);
        }
    }
}
