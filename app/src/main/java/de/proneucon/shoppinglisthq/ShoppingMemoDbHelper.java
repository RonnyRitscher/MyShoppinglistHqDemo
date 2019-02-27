package de.proneucon.shoppinglisthq;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase; //noch leer
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

/*
Sie ist eine Hilfsklasse mit deren Hilfe wir die SQLite-Datenbank erstellen lassen.
Sie enthält weiterhin wichtige Konstanten, die wir für die Arbeit mit der Datenbank benötigen,
wie den Tabellennamen, die Datenbankversion oder die Namen der Spalten.
 */
public class ShoppingMemoDbHelper extends SQLiteOpenHelper {

    //DATENBANK-name und -version festlegen
    public static final String DB_NAME = "shoppinglist_bd";
    public static final int DB_VERSION = 2;

    //TABELLEN
    public static final String TABLE_SHOPPING_LIST = "shopping_list";

    //ID - Unterstrich bei id`s für cursor etc...
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRODUCT = "product";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_CHECKED = "checked";

    //LOG-TAG
    private static final String TAG = ShoppingMemoDbHelper.class.getSimpleName();

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_SHOPPING_LIST;


    //-----------------------------------------------------------------------
    // SQL-BEFEHL zur ERZEUGUNG der DATENBANK
    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_SHOPPING_LIST +
            "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT +" TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                    COLUMN_CHECKED + " BOOLEAN NOT NULL DEFAULT 0" +
                    ");";


    //CONSTRUKTOR------------------------------------------------------------
    public ShoppingMemoDbHelper(@Nullable Context context) {
        //super(context, "Platzhalter für die Datenbank", null, 1);  //-> andern wenn infos zur db vorliegen
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG, "ShoppingMemoDbHelper:  DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    //METHODEN von SQLiteOpenHelper-------------------------------------------
    // erstellen der Datenbank (neue DB)
    // Die onCreate-Methode wird nur aufgerufen, falls die Datenbank noch nicht existiert
    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            Log.d(TAG, "onCreate: Die Tabelle wird mit dem SQL-Befehl: \n\t" + SQL_CREATE + "\n erstellt.");
            db.execSQL(SQL_CREATE);
        } catch (Exception e){
            Log.e(TAG, "onCreate: ERROR -> Fehler beim Anlegen der Tabelle: " + e.getMessage() );
        }
    }

    //Aktualisierungen der Datenbank (zB: neue/erweitern von Tabellen)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Die Tabelle mit Versionsnummer " + oldVersion + " wird entfernt.");
        db.execSQL(SQL_DROP);

        Log.d(TAG, "Die Tabelle mit Versionsnummer " + newVersion + " wird angelegt.");

        onCreate(db);
    }
}
