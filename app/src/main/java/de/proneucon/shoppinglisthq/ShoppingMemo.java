package de.proneucon.shoppinglisthq;

/*
 *  Instanzen dieser Klasse können die Daten eines SQLite-Datensatzes aufnehmen.
 *  Sie repräsentieren die Datensätze im Code. Wir werden mit Objekten dieser Klasse den ListView füllen .
 * */
public class ShoppingMemo {

    private String product;
    private int quantity;
    private long id;

    //CONSTRUCTOR

    public ShoppingMemo(String product, int quantity, long id) {
        this.product = product;
        this.quantity = quantity;
        this.id = id;
    }


    //GETTER und SETTER


    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // TO-STRING()
    @Override
    public String toString() {
        return quantity + "x " +product;
    }
}
