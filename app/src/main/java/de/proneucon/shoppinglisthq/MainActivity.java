package de.proneucon.shoppinglisthq;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    //LOG-TAG
    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView mShoppingMemosListView;
    private boolean iskeyboardClicked = false;
    boolean returnValue = false;

    boolean sortData = true;




    ShoppingMemoDataSource dataSource;

    Spinner mySpinner;
    String[] sortierung = {"Ohne" , "Name (a-z)" , "Name (z-a)"};
    private Drawable drawable;
    private CheckedTextView checkedTextView;
    private boolean showCheckbox;


    //************************************************************
    //START CALLBACKS ********************************************
    //************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Das Datenquellen-Objekt wird angelegt.");
        dataSource = new ShoppingMemoDataSource(this);

        initializeShoppingMemosListView();

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

    //END CALLABCKS***********************************************
    //************************************************************
    //START METHODS***********************************************

    //--------------------------------------------------
    //METHODE zum anzeigen ALLER EINRÄGE in der LISTE/DB
    private void showAllListEntries() {

        //Liste mit allen EINTRÄGEN der ShoppingMemos
        List<ShoppingMemo> shoppingMemos = dataSource.getAllShoppingMemos();

//        //ARRAYADAPTER für die Listview erzeugen -> siehe AdapterView
//        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_list_item_multiple_choice,
//                shoppingMemos
//        );
//        //Listview auswählen
//        ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);
//        //Adapter anhängen
//        shoppingMemoListView.setAdapter(shoppingMemoArrayAdapter);

        // Anpassen des Array-Adapters
        ArrayAdapter<ShoppingMemo> adapter = (ArrayAdapter<ShoppingMemo>) mShoppingMemosListView.getAdapter();

        adapter.clear();
        adapter.addAll(shoppingMemos);
        adapter.notifyDataSetChanged();



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
            if (getCurrentFocus()!=null  && iskeyboardClicked) {
                // wenn flag=0 dann offnet sich die Tatatur nicht automatisch nach dem Hinzufügen in dem ET_Quantity
                // wenn flag=1 dann offnet sich die Tatatur automatisch nach dem Hinzufügen
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 1);
            }

            // anzeigen aller Einträge
            showAllListEntries();

            // Zeigt das Ende der Liste an:
            shoppingMemoListView.post(() -> shoppingMemoListView.smoothScrollToPosition(shoppingMemoListView.getCount() - 1));
        });

        //Aktivieren des Buttons mit ENTER
        editTextProduct.setOnEditorActionListener( (TextView, pos , keyEvent) -> {
            iskeyboardClicked=true;
            buttonAddProduct.performClick();
            editTextQuantity.requestFocus(); //setzt den Focus auf das TV-Quantity
            iskeyboardClicked=false;

            return true;
        } );
    }

    //------------------------------------------------------------
    private void initializeContextualActionBar() {

        final ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);

        //angabe wie darf ausgewählt werden
        shoppingMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); //Mehrere auswahlmöglichkeiten
        // den Multi-Listener verwenden
        //
        shoppingMemoListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            // Anzahl der ausgewählten Elemente
            int selectCount = 0;
            //************************************************************************//
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Hier können wir auf das Auswählen von Einträgen reagieren und den Text in
                // der CAB daran anpassen, bspw. Anzahl der ausgewählten Einträge aktualisieren.
                // Von hier kann ein mode.invalidate() angefordert werden.
                //Aktuelisieren des selectCount`s
                if(checked){
                    selectCount++;
                    //TODO Checkbox

                }else {
                    selectCount--;
                }

                //Aktualisieren der Beschriftung:
                // "0 ausgewählt"
                String cabTitle = selectCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitle);
                // invalidate,  damit es sofort ausgeführt und aktualisiert wird
                mode.invalidate();
            }
            //************************************************************************//
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //Hier das Menu aufbauen/erstellt werden
                // Das Menü der CAB mit Actions füllen.
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar , menu);
                return true;
            }
            //************************************************************************//
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Hier können wir Updates an der CAB vornehmen, indem wir auf
                // eine invalidate() Anfrage aus der onItemCheckedStateChanged()  reagieren.

                //MenuItem (EDIT-BUTTON) sichtbar oder unsichtbar machen:
                MenuItem item = menu.findItem(R.id.cab_change);
                if(selectCount==1){
                    item.setVisible(true);
                }else{
                    item.setVisible(false);
                }
                return false;
            }
            //************************************************************************//
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // returnValue explitit angegeben
                returnValue = true;

                // ein BooleanArray zu einem Array -> prüft welche Item aktiviert wurden
                SparseBooleanArray touchedShoppingMemosPositions = shoppingMemoListView.getCheckedItemPositions();
                ListView lv = shoppingMemoListView;

                // Hier können wir auf Klicks auf CAB-Actions reagieren.
                switch (item.getItemId()){
                    case R.id.cab_delete:
                        //Toast.makeText(MainActivity.this, "Einträge werden hier gelöscht", Toast.LENGTH_SHORT).show();

                        //iteration über alle Checkboxen
                        for(int i=0 ; i<touchedShoppingMemosPositions.size(); i++ ){
                            //prüft die einzelne Checkbox
                            boolean isChecked = touchedShoppingMemosPositions.valueAt(i);
                            //wenn ja, dann löschen
                            if(isChecked){
                                //Schlüssel-Position    Key:    Value:true/false
                                int positionInListView = touchedShoppingMemosPositions.keyAt(i);
                                // direktes ShoppingMemo-Objekt wird gesucht
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(positionInListView);

                                Log.d(TAG, "onActionItemClicked: Position im ListView: " + positionInListView +
                                        " , Inhalt: " + shoppingMemo.toString());

                                //hier wird der Eintrag gelöscht
                                dataSource.deleteShoppingMemo(shoppingMemo);
                            }
                        }
                        showAllListEntries();   //zeige alle aktuellen Einträge an
                        mode.finish();          //beendet den contextualActionBar-modus
                        return true;

                    case R.id.cab_change:

                        //iteration über alle Checkboxen
                        for(int i=0 ; i<touchedShoppingMemosPositions.size(); i++ ) {
                            //prüft die einzelne Checkbox
                            boolean isChecked = touchedShoppingMemosPositions.valueAt(i);
                            //wenn ja, dann löschen
                            if (isChecked) {
                                //Schlüssel-Position    Key:    Value:true/false
                                int positionInListView = touchedShoppingMemosPositions.keyAt(i);
                                // direktes ShoppingMemo-Objekt wird gesucht
                                ShoppingMemo shoppingMemo = (ShoppingMemo) shoppingMemoListView.getItemAtPosition(positionInListView);

                                Log.d(TAG, "onActionItemClicked: Position im ListView: " + positionInListView +
                                        " , Inhalt: " + shoppingMemo.toString());

                                //Hier kommt der Alert-Dialog zum einsatz
                                AlertDialog editShoppingMemoDialog = createEditShoppingDialog(shoppingMemo); //erstellen des Dialogs
                                editShoppingMemoDialog.show(); //Dialog anzeigen
                            }
                        }
                        mode.finish();
                        break;

//                    /*
//                    * SELECT-ALL ELEMENTS nachfügen
//                    */
                    case R.id.cab_selectAll:
                        Toast.makeText(MainActivity.this, "Alle Einträge werden ausgewählt", Toast.LENGTH_SHORT).show();
                        //TODO - hinzufügen der selectAll - Methode
                        showAllListEntries();   //zeige alle aktuellen Einträge an
                        mode.finish();          //beendet den contextualActionBar-modus
                        return true;

                    default:

                        returnValue = false;
                }

                return returnValue;
            }
            //************************************************************************//
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Hier können wir Aktualisierungen an der Activity vornehmen, wenn die CAB
                // entfernt wird. Standardmäßig werden die ausgewählten Einträge wieder freigegeben.
                // In dieser Callback-Methode reagieren wir auf das Schließen der CAB
                /// Wir setzen den Zähler auf 0 zurück
                selectCount = 0;
            }
        });
    }

    //------------------------------------------------------------
    // ALERT-DIALOG für Einträge ändern
    private AlertDialog createEditShoppingDialog(final ShoppingMemo shoppingMemo){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Anzeige/Ansicht
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_shopping_memo , null);

        //informationen zum Eintrag einholen und den neuen Eintrag übergeben
        final EditText editTextNewQuantity = dialogView.findViewById(R.id.editText_new_quantity);
        editTextNewQuantity.setText(String.valueOf(shoppingMemo.getQuantity()));        //Wichtig ist das Sting.valueOf()

        final EditText editTextNewProduct = dialogView.findViewById(R.id.editText_new_product);
        editTextNewProduct.setText(shoppingMemo.getProduct());

        //Dialog soll vollständig gebaut werden
        //jede methode des builders gibt das builder-objekt zurück
        builder.setView(dialogView)
                .setTitle(R.string.alertDialog_title)
                //Positiver Button(StringText , Listener )
                .setPositiveButton(R.string.alertDialog_button_positiv, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //was soll passieren wenn gedrückt wird:
                        //Objekte sollen in unsere Datenbak hereingeschrieben werden
                        String quantityString = editTextNewQuantity.getText().toString();
                        String product = editTextNewProduct.getText().toString();

                        //sicherstellen, dass keines der felder leer ist
                        if(TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(product) ){
                            //wenn leer, dann methode verlassen
                            Toast.makeText(MainActivity.this, "Felder dürfen nicht leer sein!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //umwandeln des Strings zum int
                        int quantity = Integer.parseInt(quantityString);

                        //Eintrag aktualisieren  // An dieser Stelle schreiben wir die geänderten Daten in die SQLite Datenbank
                        ShoppingMemo memo = dataSource.updateShoppingMemo(shoppingMemo.getId() , product , quantity , shoppingMemo.isChecked());
                        Log.d(TAG, "onClick: Alter Eintrag - ID: " + shoppingMemo.getId() + " inhalt: "+ shoppingMemo.toString() +" -> ");
                        Log.d(TAG, "onClick: Neuer Eintrag - ID: " + shoppingMemo.getId() + " inhalt: "+ memo.toString() +" -> ");

                        //Alle Einträge wieder anzeigen lassen
                        showAllListEntries();
                        // Dialog beenden
                        dialog.dismiss();
                    }
                })
                //Negativer Button(StringText , Listener )
                .setNegativeButton(R.string.alertDialog_button_negativ, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Dialog abbrechen
                        dialog.cancel();
                    }
                });
        //
        editTextNewQuantity.setSelection(0 ,  editTextNewQuantity.length());

        //Erstellt den finalen Builder und gibt ihn zurück
        return builder.create();
    }

    //-------------------------------------------------------------------
    private void initializeShoppingMemosListView() {
        List<ShoppingMemo> emptyListForInitialization = new ArrayList<>();

        mShoppingMemosListView = findViewById(R.id.listview_shopping_memos);

        // Erstellen des ArrayAdapters für unseren ListView
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo> (
                this,
                android.R.layout.simple_list_item_multiple_choice,
                emptyListForInitialization) {

            // Wird immer dann aufgerufen, wenn der übergeordnete ListView die Zeile neu zeichnen muss
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view =  super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                ShoppingMemo memo = (ShoppingMemo) mShoppingMemosListView.getItemAtPosition(position);

                // Hier prüfen, ob Eintrag abgehakt ist. Falls ja, Text durchstreichen
                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175,175,175));
                }
                else {
                    textView.setPaintFlags( textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }

                return view;
            }
        };

        mShoppingMemosListView.setAdapter(shoppingMemoArrayAdapter);

        mShoppingMemosListView.setOnItemClickListener( (AdapterView<?> adapterView, View view, int position, long id) -> {
            //} new AdapterView.OnItemClickListener() {
            //@Override
            //public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ShoppingMemo memo = (ShoppingMemo) adapterView.getItemAtPosition(position);

                // Hier den checked-Wert des Memo-Objekts umkehren, bspw. von true auf false
                // Dann ListView neu zeichnen mit showAllListEntries()
                ShoppingMemo updatedShoppingMemo = dataSource.updateShoppingMemo(memo.getId(), memo.getProduct(), memo.getQuantity(), (!memo.isChecked()));
                Log.d(TAG, "Checked-Status von Eintrag: " + updatedShoppingMemo.toString() + " ist: " + updatedShoppingMemo.isChecked());
                showAllListEntries();
            //}
        });

    }

    //Scan über den FloatingActionButton
    public void startScan(View view) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");

        intent.putExtra("SCAN_MODE" , "PRODUCT_MODE");

        try {
            //BESONDERHEIT: sie andere App soll uns ein ergebis zurück liefern
            startActivityForResult(intent , 1);
        }catch (ActivityNotFoundException e){
            //Was passiert wenn die App nicht installiert/auffindbar ist
            Toast.makeText(this, "Scanner nicht installiert!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1 && resultCode==RESULT_OK){
            TextView product = findViewById(R.id.editText_product); //steuert den TV editText_product an

            //hier soll der Text aus dem Intent in das et_product übergeben werden
            product.setText(getProductName(data.getStringExtra("SCAN_RESULT")));

            //Focus nach dem eintragen auf die quantity setzen
            TextView quantity = findViewById(R.id.editText_quantity);
            quantity.requestFocus();


        }else{
            Toast.makeText(this, "Scan nicht möglich. Prüfe RequestCode oder ResultCode!", Toast.LENGTH_SHORT).show();
        }

    }

    //---------------------------------------------------------------------
    private String getProductName(String scanResult){
        HoleDatenTask task = new HoleDatenTask();
        String result = null;
        try {
            result = task.execute(scanResult).get();
            Log.d(TAG, "getProductName: result: " + result);


            JSONObject rootObject = new JSONObject(result);
            Log.d(TAG, "getProductName: rootObject: " + rootObject.toString());

            //Informationen aus dem JSON filtern
            if(rootObject.has("product")){
                JSONObject productObject = rootObject.getJSONObject("product");

                //NAME des PRODUKTS
                if (productObject.has("product_name")){
                    return productObject.getString("product_name");
                }
            }

        } catch (ExecutionException e) {
            Log.e(TAG, " ERROR ", e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, " ERROR ", e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(TAG, " ", e);
            e.printStackTrace();
        }

        return result;
    }

    //---------------------------------------------------------------------
    //DER ASYNC_TASK kann verwendet werden um Daten (JSON/XML) von externen Seite zu verarbeiten
    public class HoleDatenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            //Welche WEBSEITE und elche API
            final String baseUrl = "https://world.openfoodfacts.org/api/v0/product/";   // WEBSEITE
            final String requestUrl = baseUrl + strings[0] + ".json";                   //

            Log.d(TAG, "doInBackground: REQUEST-URL " +requestUrl );

            StringBuilder result = new StringBuilder();

            URL url = null;
            try {
                //Verwende die gesamte zusammengesetzteURL
                url = new URL(requestUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground: ERROR - die erzeugte URL konnte nicht erreicht werden");
                e.printStackTrace();
            }

            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))){
                String line;
                while ( (line = reader.readLine()) != null ){
                    result.append(line);
                }
            }catch (IOException e){
                Log.e(TAG, "doInBackground: ERROR - lesen der Connection nicht möglich", e);
            }
            Log.d(TAG, "doInBackground: " + result.toString());
            return  result.toString();
        }
    }
}
