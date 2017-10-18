import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.Math;

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

        String temp = String.format("%.2f", price);
        price = Double.parseDouble(temp);

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
        frame.setSize(500, 400);

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

    private static String labelList, printList;
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

        labelList = "";
        printList = "";
        x = 0;
        total = 0;

        System.out.println("Checkout");
        JFrame checkoutWindow = new JFrame("Checkout");
        checkoutWindow.getContentPane().setBackground(new Color(160, 250, 255));

        JTextField itemField = new JTextField(20);

        itemField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                int temp;
                String text = itemField.getText();
                try {
                    temp = changeItem(text, "Quantity", "-1", rs, con);
                    if(temp == 1)
                    {
                        printList += text + "\n";
                        labelList += text + "<br>";
                        x++;
                        total += getPrice(text);
                    }
                    System.out.println("Item: " + text + "\nPrice: " + getPrice(text) + "\nTotal: " + total);
                    totalLabel.setText(String.format("$%.2f", total));
                    itemList.setText("<html>Items:<br>" + labelList + "<html>");
                    System.out.println(printList);
                } catch (SQLException e1) { e1.printStackTrace(); }
            }
        });

        itemList = new JLabel("Items:");
        totalLabel = new JLabel("$0.00");

        JButton payButton = new JButton("Pay");
        payButton.setPreferredSize(new Dimension(75, 40));

        payButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                checkoutWindow.dispose();
                try {
                    pay();
                } catch (SQLException e1) { e1.printStackTrace(); }
            }
        });

        JLabel totalTitle = new JLabel("Total", SwingConstants.CENTER);
        JPanel titlePanel = new JPanel();
        JPanel totalPanel = new JPanel();
        JPanel itemPanel = new JPanel();
        JPanel itemTotalPanel = new JPanel();
        JPanel checkoutPanel = new JPanel();
        JPanel everything = new JPanel();

        titlePanel.add(totalTitle);
        //titlePanel.setBackground(Color.RED);
        titlePanel.setBounds(200,0,100,100);
        totalTitle.setFont(new Font("Calibri", Font.BOLD, 30));
        titlePanel.setOpaque(false);

        totalPanel.add(totalLabel);
        //totalPanel.setBackground(Color.GREEN);
        totalPanel.setBounds(375,50,75,50);
        totalLabel.setFont(new Font("Calibri", Font.BOLD,20));
        totalPanel.setOpaque(false);

        itemPanel.add(itemList);
        //itemPanel.setBackground(Color.GRAY);
        itemPanel.setBounds(50,110,300,250);
        totalLabel.setFont(new Font("Calibri", Font.PLAIN, 15));
        itemPanel.setOpaque(false);

        itemTotalPanel.add(itemField);
        //itemTotalPanel.setBackground(Color.WHITE);
        itemTotalPanel.setBounds(50,50,300,50);
        itemTotalPanel.setOpaque(false);

        checkoutPanel.add(payButton);
        //checkoutPanel.setBackground(Color.BLUE);
        checkoutPanel.setBounds(375,175,85,50);
        checkoutPanel.setOpaque(false);

        //everything.setBackground(Color.YELLOW);
        everything.setOpaque(false);

        checkoutWindow.getContentPane().add(titlePanel);
        checkoutWindow.getContentPane().add(totalPanel);
        checkoutWindow.getContentPane().add(itemPanel);
        checkoutWindow.getContentPane().add(itemTotalPanel);
        checkoutWindow.getContentPane().add(checkoutPanel);
        checkoutWindow.getContentPane().add(everything);

        checkoutWindow.setSize(500, 400);
        checkoutWindow.setVisible(true);
    }

    public static void pay() throws SQLException
    {
        JFrame payMenu = new JFrame();
        payMenu.getContentPane().setBackground(new Color(160, 250, 255));

        JPanel titlePanel = new JPanel();
        JPanel totalPanel = new JPanel();
        JPanel radioButtonsPanel = new JPanel();
        JPanel payButtonPanel = new JPanel();
        JLabel payTitle = new JLabel("Payment");
        JLabel totalLabel = new JLabel(String.format("$%.2f", total));
        JPanel everything = new JPanel();
        JButton payButton = new JButton("Finish and Pay");

        JRadioButton cash = new JRadioButton("Cash");
        JRadioButton debit = new JRadioButton("Debit Card");
        JRadioButton credit = new JRadioButton("Credit Card");
        JRadioButton check = new JRadioButton("Check");
        JRadioButton EBT = new JRadioButton("EBT");
        ButtonGroup bG = new ButtonGroup();
        bG.add(cash);
        bG.add(debit);
        bG.add(credit);
        bG.add(check);
        bG.add(EBT);
        radioButtonsPanel.add(cash);
        radioButtonsPanel.add(debit);
        radioButtonsPanel.add(credit);
        radioButtonsPanel.add(check);
        radioButtonsPanel.add(EBT);
        cash.setSelected(true);

        payMenu.getContentPane().add(titlePanel);
        payMenu.getContentPane().add(totalPanel);
        payMenu.getContentPane().add(radioButtonsPanel);
        payMenu.getContentPane().add(payButtonPanel);
        payMenu.getContentPane().add(everything);

        titlePanel.add(payTitle);
        totalPanel.add(totalLabel);
        payButtonPanel.add(payButton);

        payButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                payMenu.dispose();
            }
        });

        titlePanel.setBackground(Color.WHITE);
        payTitle.setFont(new Font("Calibri", Font.BOLD, 30));
        titlePanel.setBounds(150,0,200,50);
        titlePanel.setOpaque(false);

        totalPanel.setBackground(Color.BLUE);
        totalLabel.setFont(new Font("Calibri", Font.PLAIN, 20));
        totalPanel.setBounds(200,125,100,50);
        totalPanel.setOpaque(false);

        radioButtonsPanel.setBackground(Color.GREEN);
        radioButtonsPanel.setBounds(25,190,450,50);
        radioButtonsPanel.setOpaque(false);

        payButtonPanel.setBackground(Color.RED);
        payButtonPanel.setBounds(175,260,150,40);
        payButtonPanel.setOpaque(false);

        everything.setBackground(Color.YELLOW);
        everything.setOpaque(false);

        payMenu.setSize(500, 400);
        payMenu.setVisible(true);
    }
}