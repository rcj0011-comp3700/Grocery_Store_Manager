import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            String host = "jdbc:mysql://localhost/grocery_store";

            Connection con = DriverManager.getConnection(host, "root", "cameron1");

            String query = "SELECT * FROM inventory";

            // create the java statement
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            // execute the query, and get a java resultSet
            ResultSet rs = st.executeQuery(query);

            createMenu();

            con.close();
        }
        catch ( SQLException err )
        {
            System.out.println("Error: " + err.getMessage());
        }
    }

    public static int changeItem(String name, String category, String value, ResultSet rs, Connection con) throws SQLException
    {
        String statement = "";

        while(rs.next())
        {
            if (rs.getString("Name").equals(name))
            {
                if (category == "Quantity") {
                    int newValue = Integer.parseInt(value) + rs.getInt(4);
                    statement = "UPDATE inventory SET Quantity = " + newValue + " WHERE Name = '" + name + "'";
                } else if (category == "Producer") {
                    String newValue = value;
                    statement = "UPDATE inventory SET Producer = '" + newValue + "' WHERE Name = '" + name + "'";
                } else if (category == "ID") {
                    int newValue = Integer.parseInt(value);
                    statement = "UPDATE inventory SET ID = " + newValue + " WHERE Name = '" + name + "'";
                } else if (category == "Name") {
                    String newValue = value;
                    statement = "UPDATE inventory SET Name = '" + newValue + "' WHERE Name = '" + name + "'";
                } else if (category == "Price") {
                    double newValue = Double.parseDouble(value);
                    statement = "UPDATE inventory SET Price = " + newValue + " WHERE Name = '" + name + "'";
                }
            }
        }

        rs.beforeFirst();

        if(statement != "")
        {
            PreparedStatement st = con.prepareStatement(statement);
            st.execute();
            System.out.println("Changed item: " + name);
            return 1;
        }
        else
        {
            System.out.println("No item with name " + name + " found.");
            return 0;
        }
    }

/*    public static product lookupItem(String item, Connection con) throws SQLException
    {
        String query = "SELECT * FROM inventory";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query);

        while(rs.next())
        {
            if(rs.getString("Name").equals(item))
            {
                product temp = new product(getLastID(rs), item, rs.getDouble(3), rs.getInt(4), rs.getString(5));
                return temp;
            }
        }
        return null;
    }

    public static int getLastID(ResultSet rs) throws SQLException
    {
        int count = 0;

        while(rs.next())
            count++;

        return count+1;
    }*/

    public static double getPrice(String item) throws SQLException
    {
        double price = 0;

        String host = "jdbc:mysql://localhost/grocery_store";

        Connection con = DriverManager.getConnection(host, "root", "cameron1");

        String query = "SELECT * FROM inventory";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next())
            if (rs.getString("Name").equals(item))
                price = rs.getDouble(3);

        return price;
    }

    public static void newItem(product thing, Connection con) throws SQLException
    {
        int ID = thing.getID();
        String name = thing.getName();
        double price = thing.getPrice();
        int quantity = thing.getQuantity();
        String producer = thing.getProducer();

        String statement = "INSERT INTO inventory VALUES (" + ID + ", '" + name + "', " + price + ", " + quantity + ", '" + producer + "')";

        PreparedStatement st = con.prepareStatement(statement);

        st.execute();
    }

    public static void createMenu()
    {
        JFrame frame = new JFrame("Grocery Store");
        frame.getContentPane().setBackground(new Color(160, 250, 255));

        JButton checkoutButton = new JButton("Checkout");
        JButton manageButton = new JButton("Manage Products");
        checkoutButton.setPreferredSize(new Dimension(150, 40));
        manageButton.setPreferredSize(new Dimension(150, 40));

        checkoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    checkout();
                } catch (SQLException e1) { e1.printStackTrace(); }
            }
        });
        manageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("Manage items");
            }
        });

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setSize(500, 450);

        JLabel title = new JLabel("Store Management System");
        title.setFont(new Font("Calibri", Font.BOLD, 30));

        JPanel titlePanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        titlePanel.add(title);
        frame.getContentPane().add(titlePanel);

        buttonPanel.add(checkoutButton);
        buttonPanel.add(manageButton);
        frame.getContentPane().add(buttonPanel);

        //Display the window.
        frame.setVisible(true);
        buttonPanel.setOpaque(false);
        titlePanel.setOpaque(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static String list;
    private static int x;
    private static double total;
    private static JLabel totalLabel, itemList;

    public static void checkout() throws SQLException
    {
        String host = "jdbc:mysql://localhost/grocery_store";

        Connection con = DriverManager.getConnection(host, "root", "cameron1");

        String query = "SELECT * FROM inventory";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query);

        list = "";
        x = 0;
        total = 0;

        System.out.println("Checkout");
        JFrame checkoutWindow = new JFrame("Checkout");
        checkoutWindow.getContentPane().setBackground(new Color(160, 250, 255));

        JTextField item = new JTextField(20);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                int temp;
                String text = item.getText();
                try {
                    temp = changeItem(text, "Quantity", "-1", rs, con);
                    if(temp == 1)
                    {
                        list += text + "\n";
                        x++;
                        total += getPrice(text);
                    }
                    System.out.println("Item: " + text + "\nPrice: " + getPrice(text) + "\nTotal: " + total);
                    totalLabel.setText(String.valueOf(total));
                    itemList.setText("Items:\n" + list);
                } catch (SQLException e1) { e1.printStackTrace(); }
            }
        });

        JLabel totalTitle = new JLabel("Total", SwingConstants.CENTER);
        totalTitle.setFont(new Font("Calibri", Font.BOLD, 30));

        itemList = new JLabel("Items:");
        totalLabel = new JLabel("Total");

        JButton payButton = new JButton("Finish and Pay");
        payButton.setPreferredSize(new Dimension(150, 40));

        JPanel checkoutPanel = new JPanel();
        checkoutPanel.add(totalTitle);
        checkoutPanel.add(totalLabel);
        checkoutPanel.add(itemList);
        checkoutPanel.add(item);
        checkoutPanel.add(payButton);

        checkoutWindow.setSize(500, 450);
        checkoutPanel.setOpaque(false);
        checkoutWindow.setVisible(true);
        checkoutWindow.getContentPane().add(checkoutPanel);
    }
}