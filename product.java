public class product
{
    private int ID;
    private String Name;
    private double Price;
    private int Quantity;
    private String Producer;

    public product (int id, String name, double price, int quantity, String producer)
    {
        ID = id;
        Name = name;
        Price = price;
        Quantity = quantity;
        Producer = producer;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getID() {
        return ID;

    }
    public void setID(int id) {
        ID = id;
    }

    public String getProducer() {
        return Producer;
    }

    public void setProducer(String producer) {
        Producer = producer;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }
}
