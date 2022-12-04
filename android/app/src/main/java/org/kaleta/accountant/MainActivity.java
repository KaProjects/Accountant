package org.kaleta.accountant;

import android.app.AlertDialog;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.kaleta.accountant.dialog.AddAccountDialog;
import org.kaleta.accountant.dialog.AddTemplateDialog;
import org.kaleta.accountant.dialog.AddTransactionDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.add);
        fab.setOnClickListener(view -> {
            AddTransactionDialog builder = new AddTransactionDialog(this);
            AlertDialog dialog = builder.create();
            dialog.show();
            builder.createValidator(dialog.getButton(AlertDialog.BUTTON_POSITIVE));

        });

        RecyclerView recyclerView = findViewById(R.id.transaction);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TransactionAdapter());


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_account) {
            AddAccountDialog builder = new AddAccountDialog(this);
            AlertDialog dialog = builder.create();
            dialog.show();
            builder.createValidator(dialog.getButton(AlertDialog.BUTTON_POSITIVE));

            return true;
        }
        if (id == R.id.action_create_template) {
            AddTemplateDialog builder = new AddTemplateDialog(this);
            AlertDialog dialog = builder.create();
            dialog.show();
            builder.createValidator(dialog.getButton(AlertDialog.BUTTON_POSITIVE));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
