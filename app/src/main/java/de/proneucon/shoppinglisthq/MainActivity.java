package de.proneucon.shoppinglisthq;

import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //LOG-TAG
    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean iskeyboardClicked = false;
    boolean returnValue = false;

    Spinner mySpinner;

    ShoppingMemoDataSource dataSource;

    String[] sortierung = {"Ohne" , "Name (a-z)" , "Name (z-a)"};


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

        /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //Spinner auswählen:
        mySpinner = findViewById(R.id.spinner_sortierung);
        //ARRAYADAPTER für den Spinner auswählen
       mySpinner.setAdapter(new ArrayAdapter<>(
               this ,
               android.R.layout.simple_list_item_multiple_choice,
               sortierung));

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  */

        //Liste mit allen EINTRÄGEN der ShoppingMemos
        List<ShoppingMemo> shoppingMemos = dataSource.getAllShoppingMemos();

        //ARRAYADAPTER für die Listview erzeugen -> siehe AdapterView
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

                // ein Boolean zu einem Array -> prüft welche Item aktiviert sind
                SparseBooleanArray touchedShoppingMemosPositions = shoppingMemoListView.getCheckedItemPositions();

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
                                AlertDialog editShoppingMemoDialog = createShoppingDialog(shoppingMemo); //erstellen des Dialogs
                                editShoppingMemoDialog.show(); //Dialog anzeigen
                            }
                        }
                        mode.finish();
                        break;

//                    /*
//                    * SELECT-ALL ELEMENTS nachfügen
//                    */
//                    case R.id.cab_selectAll:
//                        for(int i=0 ;i< touchedShoppingMemosPositions.size(); i++){
//                            //Schlüssel-Position    Key:    Value:true/false
//                            int positionInListView = touchedShoppingMemosPositions.keyAt(i);
//                            touchedShoppingMemosPositions.valueAt(i).
//                        }
//                        return true;
//                        /**/

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

                //Wenn der nEintrag gelöscht wurde, explizit auf 0 setzen
                selectCount = 0;
            }
        });
    }

    //------------------------------------------------------------
    // ALERT-DIALOG für Einträge ändern
    private AlertDialog createShoppingDialog(final ShoppingMemo shoppingMemo){
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
                        ShoppingMemo memo = dataSource.updateShoppingMemo(shoppingMemo.getId() , product , quantity);
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
                    public void onClick(DialogInterface dialog, int which) {
                        //Dialog abbrechen
                        dialog.cancel();
                    }
                });
        //
        editTextNewQuantity.setSelection(0 ,  editTextNewQuantity.length());

        //Erstellt den finalen Builder und gibt ihn zurück
        return builder.create();
    }
}
