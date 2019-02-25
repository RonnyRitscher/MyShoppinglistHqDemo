package de.proneucon.shoppinglisthq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //LOG-TAG
    private static final String TAG = MainActivity.class.getSimpleName();

    ShoppingMemoDataSource dataSource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Das Datenquellen-Objekt wird angelegt.");
        dataSource = new ShoppingMemoDataSource(this);

        //Methode um Produkte in die Liste hinzuzufügen
        activateAddButton();

        initializeContextualActionBar();

    }


    //----------------------------------------------------
    // PAUSE und RESUME  -> hier öffnen und schlißen wir die DB

    @Override
    protected void onPause() {
        super.onPause();

        //schließen der DB
        Log.d(TAG, "onCreate: Datenquelle wird geschlossen...");
        dataSource.close();

    }

    @Override
    protected void onResume() {
        super.onResume();

        //öffnen der DB
        Log.d(TAG, "onCreate: Datenquelle wird geöffnet...");
        dataSource.open();

        //anzeigen ALLER EINTRÄGE der LISTE
        showAllListEntries();


    }

    //-----------------------------------------------------

    // DAS MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //INFLATER  (Parameter -> Was, Wohin
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "Einstellungen wurden gedrückt!", Toast.LENGTH_SHORT).show();
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    //--------------------------------------------------
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
    //METHODE wenn der AddButton geklickt wird
    private void activateAddButton() {

        //Listview auswählen
        ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);

        final EditText editTextQuantity = findViewById(R.id.editText_quantity);
        final EditText editTextProduct = findViewById(R.id.editText_product);
        Button buttonAddProduct = findViewById(R.id.button_add_product);

        buttonAddProduct.setOnClickListener(v -> {

            String quantityString = editTextQuantity.getText().toString();
            String product = editTextProduct.getText().toString();

            //prüfen ob diese leer sind:
            // -> wenn ja, dann ERROR und NICHT HINZUFÜGEN!
            if (TextUtils.isEmpty(quantityString)) {
                editTextQuantity.setError(getString(R.string.editText_errorMessage));
                //return ist wichtig, damit keine falsche eingabe umgesetzt wird
                return;
            }
            //prüfen ob diese leer sind:
            if (TextUtils.isEmpty(product)) {
                editTextProduct.setError(getString(R.string.editText_errorMessage));
                return;
            }

            //quantity-String in einen Integer parsen
            int quantity = Integer.parseInt(quantityString);

            //Textfelder wieder leeren
            editTextProduct.setText("");
            editTextQuantity.setText("");

            //erstellen eines Eintrags
            dataSource.createShoppingMemo(product, quantity);

            //Ausblenden der Tastatur nach adden
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            // anzeigen aller Einträge
            showAllListEntries();

            // Zeigt das Ende der Liste an:
            shoppingMemoListView.post(() -> shoppingMemoListView.smoothScrollToPosition(shoppingMemoListView.getCount() - 1));
        });
    }

    //------------------------------------------------------------
    private void initializeContextualActionBar() {
        final ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);

        //angabe wie darf ausgewählt werden
        shoppingMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); //Mehrere auswahlmöglichkeiten
        // den Multi-Listener verwenden
        //
        shoppingMemoListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Hier können wir auf das Auswählen von Einträgen reagieren und den Text in
                // der CAB daran anpassen, bspw. Anzahl der ausgewählten Einträge aktualisieren.
                // Von hier kann ein mode.invalidate() angefordert werden.


            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //Hier das Menu aufbauen/erstellt werden
                // Das Menü der CAB mit Actions füllen.
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar , menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Hier können wir Updates an der CAB vornehmen, indem wir auf
                // eine invalidate() Anfrage reagieren.
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Hier können wir auf Klicks auf CAB-Actions reagieren.
                switch (item.getItemId()){
                    case R.id.cab_delete:
                        Toast.makeText(MainActivity.this, "Einträge werden hier gelöscht", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }

                //return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Hier können wir Aktualisierungen an der Activity vornehmen, wenn die CAB
                // entfernt wird. Standardmäßig werden die ausgewählten Einträge wieder freigegeben.
            }
        });

    }
}
