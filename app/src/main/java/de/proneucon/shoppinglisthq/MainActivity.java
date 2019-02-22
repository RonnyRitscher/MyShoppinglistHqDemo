package de.proneucon.shoppinglisthq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //-----------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //INFLATER  (PArameter -> Was, Wohin
        getMenuInflater().inflate(R.menu.menu_main , menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Toast.makeText(this, "Einstellungen wurden gedrückt!", Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //--------------------------------------------------
}
