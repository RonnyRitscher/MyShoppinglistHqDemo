package de.proneucon.shoppinglisthq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/*
Diese Klasse ist unser Data Access Object und für das Verwalten der Daten verantwortlich.
Es unterhält die Datenbankverbindung und ist für das Hinzufügen, Auslesen und Löschen von Datensätzen zuständig.
Außerdem wandelt es Datensätze in Java-Objekte für uns um, so dass der Code unserer Benutzeroberfläche
nicht direkt mit den Datensätzen arbeiten muss.
 */
public class ShoppingMemoDataSource {

    //LOG-TAG
    private static final String TAG = ShoppingMemoDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private ShoppingMemoDbHelper dbHelper;

    //Für Abfragen des SQLite-Daten mit dem Cursor
    private String[] columns = {
            ShoppingMemoDbHelper.COLUMN_ID ,
            ShoppingMemoDbHelper.COLUMN_PRODUCT ,
            ShoppingMemoDbHelper.COLUMN_QUANTITY ,
            ShoppingMemoDbHelper.COLUMN_CHECKED
    };



    //CONSTRUKTOR
    public ShoppingMemoDataSource(Context context) {
        Log.d(TAG, "ShoppingMemoDataSource: DataSource erzeugt jetzt den dbHelper");
        dbHelper = new ShoppingMemoDbHelper(context);
    }


    //METHODE zum erstellen eines ShoppingMemoEintrags
    public ShoppingMemo createShoppingMemo(String product , int quantity){
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT , product);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY , quantity);

        //Beschaffen der ID des Eintrags
        long insertId = database.insert(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST , null , values);

            //Cursor
        Cursor cursor =
                database.query(
                        ShoppingMemoDbHelper.TABLE_SHOPPING_LIST ,                  // WELCHE LISTE
                        columns ,                                                   // STRING[]
                        ShoppingMemoDbHelper.COLUMN_ID + "=" + insertId ,   // WHERE
                        null, null , null , null);   // EINSTELLUNGEN

        cursor.moveToFirst(); //Setzt den cursor auf den Anfang
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        cursor.close();

        return shoppingMemo;
    }

    //-----------------------------------------------------------------
    // METHODE - Ändern eines Eintrags
    public ShoppingMemo updateShoppingMemo(long id , String newProduct , int newQuantity,  boolean newChecked){

        //CHECKED
        int intValueChecked = (newChecked)? 1 : 0;

        //setzen der neuen Werte
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT , newProduct);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY , newQuantity);
        values.put(ShoppingMemoDbHelper.COLUMN_CHECKED, intValueChecked);

        database.update(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST ,
                values,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id ,
                null);

        //Cursor (sql-statement) erzeugen
        //SQL-STATEMENT-> " SELECT _id , product , quantity WHERE _id=123 ;"//
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST, columns , ShoppingMemoDbHelper.COLUMN_ID + "="+id ,
                null, null , null , null);

        cursor.moveToFirst();       //curser auf anfang setzen
        ShoppingMemo memo = cursorToShoppingMemo(cursor);   //cursor auf den Eintrag setzen und als ShoppingMemo-Objekt zuweisen
        cursor.close();     //cursor schließen
        return memo;        //ShoppingMemo-Objekt zurückgeben
    }

    //-----------------------------------------------------------------
    // METHODE - löschen eines Beitrags
    public void deleteShoppingMemo(ShoppingMemo shoppingMemo){
        long id = shoppingMemo.getId();  //besorgt die ID
        database.delete(
                ShoppingMemoDbHelper.TABLE_SHOPPING_LIST ,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id ,
                null);

        Log.d(TAG, "deleteShoppingMemo: Eintrag gelöscht: " + id + " Inhalt: " +shoppingMemo.toString());
    }

    //-----------------------------------------------------------------
    // METHODE zum AUSLESEN der EINTRÄGE
    // gibt den Inhalt der Reihen aus, auf dem der cuser platziert wird
    private ShoppingMemo cursorToShoppingMemo(Cursor cursor) {
        // Besorge den Index
        int idIndex = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_ID);
        int idProduct = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_PRODUCT);
        int idQuantity = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_QUANTITY);
        int idChecked = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_CHECKED);

        //Die dahinter liegenden Werte beschaffen:
        String product = cursor.getString(idProduct);
        int quantity = cursor.getInt(idQuantity);
        long id = cursor.getLong(idIndex);
        int intValueChecked = cursor.getInt(idChecked);
        boolean isChecked = (intValueChecked != 0);

        // ShoppingMemo-Objekt mit den Werten erzeugen und übergeben
        ShoppingMemo shoppingMemo = new ShoppingMemo(product, quantity, id, isChecked);
        return  shoppingMemo;

    }

    //----------------------------------------------
    //METHODE um ALLE EINRÄGE darzustellen
    public List<ShoppingMemo> getAllShoppingMemos(){
        List<ShoppingMemo> shoppingMemoList = new ArrayList<>();

        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns, null, null, null, null, null);

        cursor.moveToFirst();

        ShoppingMemo shoppingMemo; //Hier kommt der Cuser anschließend rein

        while(!cursor.isAfterLast()){
            shoppingMemo = cursorToShoppingMemo(cursor);
            shoppingMemoList.add(shoppingMemo); //Hinzufügen der einzelnen  SM zur SML
            //Log.d(TAG, "getAllShoppingMemos:  ID: " + shoppingMemo.getId() + ", Inhalt: " + shoppingMemo.toString());
            cursor.moveToNext(); //cursior auf das nachste Element
        }

        cursor.close();  //cursor schließen
        return shoppingMemoList; // Liste zurückgeben
    }





    //---------------------------------------------
    //METHODE zum ÖFFNEN der DB
    public void open(){
        Log.d(TAG, "open: Eine Referenz auf die Datenbank wird angefragt.");
        database = dbHelper.getWritableDatabase(); // erstellt die Connection zur Datenbank
        Log.d(TAG, "open: Referenz erhalten. Pfad zur DB: " + database.getPath());
    }
    //METHODE zum SCHLIESSEN der DB
    public void close(){
        dbHelper.close();
        Log.d(TAG, "close: Datenbank mit Hilfe des DbHelpers geschlossen.");
    }
}
