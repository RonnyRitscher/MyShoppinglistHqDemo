package de.proneucon.shoppinglisthq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import javax.sql.DataSource;

public class MainActivity extends AppCompatActivity {

    //LOG-TAG
    private static final String TAG = MainActivity.class.getSimpleName();

    ShoppingMemoDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TEST-OBJEKT
        ShoppingMemo testMemo = new ShoppingMemo("Birnen" , 5 , 100);
        Log.d(TAG, "onCreate: Inhalt der TestMemo (" + testMemo.toString() + ") erzeugt");

        dataSource = new ShoppingMemoDataSource(this);

        //öffnen der DB
        Log.d(TAG, "onCreate: Datenquelle wird geöffnet...");
        dataSource.open();

        // aktive zugriffe auf die DB
        //TESTPRODUKT
        ShoppingMemo shoppingMemo = dataSource.createShoppingMemo("Testprodukt" , 3);
        Log.d(TAG, "onCreate: Es wurde folgender Eintrag in die DB geschrieben: \n"+
                "ID: " + shoppingMemo.getId() + ", Inhalt: " + shoppingMemo.toString());

        //anzeigen ALLER EINTRÄGE der LISTE
        showAllListEntries();


        //schließen der DB
        Log.d(TAG, "onCreate: Datenquelle wird geschlossen...");
        dataSource.close();
    }

    //-----------------------------------------------------
    //METHODE zum anzeigen ALLER EINRÄGE in der LISTE/DB
    private void showAllListEntries() {
        List<ShoppingMemo> shoppingMemos = dataSource.getAllShoppingMemos();

        //ARRAYADAPTER erzeugen
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                shoppingMemos
                );

        //Listview auswählen
        ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);

        //Adapter anhängen
        shoppingMemoListView.setAdapter(shoppingMemoArrayAdapter);

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
