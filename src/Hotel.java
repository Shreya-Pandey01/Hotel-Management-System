import java.sql.*;
import java.util.Scanner;

public class Hotel {

    static Scanner sc = new Scanner(System.in);

    // Add Customer
    public static void addCustomer() {
        try {
            Connection con = DBConnection.getConnection();

            System.out.print("Enter customer name: ");
            String name = sc.nextLine();

            System.out.print("Enter phone number: ");
            String phone = sc.nextLine();

            String sql = "INSERT INTO customer (name, phone) VALUES (?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, phone);

            ps.executeUpdate();

            System.out.println("Customer added successfully!");

            con.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // View Customers
    public static void viewCustomers() {
        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM customer";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n--- Customers ---");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("customer_id") + " | " +
                    rs.getString("name") + " | " +
                    rs.getString("phone")
                );
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // View Rooms
    public static void viewRooms() {
        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM room";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n--- Rooms ---");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("room_id") + " | Room " +
                    rs.getInt("room_number") + " | " +
                    rs.getString("room_type") + " | ₹" +
                    rs.getDouble("price") + " | " +
                    rs.getString("status")
                );
            }

            con.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Book a Room
public static void bookRoom() {
    try {
        Connection con = DBConnection.getConnection();

        System.out.print("Enter customer ID: ");
        int customerId = sc.nextInt();

        System.out.print("Enter room ID: ");
        int roomId = sc.nextInt();

        sc.nextLine();

        // Check whether room is available
        String checkSql = "SELECT status FROM room WHERE room_id = ?";
        PreparedStatement checkPs = con.prepareStatement(checkSql);
        checkPs.setInt(1, roomId);

        ResultSet rs = checkPs.executeQuery();

        if (!rs.next()) {
            System.out.println("Room not found!");
            con.close();
            return;
        }

        if (!rs.getString("status").equals("Available")) {
            System.out.println("Room is already booked!");
            con.close();
            return;
        }

        System.out.print("Enter check-in date (YYYY-MM-DD): ");
        String checkIn = sc.nextLine();

        String sql = "INSERT INTO booking (customer_id, room_id, check_in) VALUES (?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, customerId);
        ps.setInt(2, roomId);
        ps.setDate(3, Date.valueOf(checkIn));

        ps.executeUpdate();

        // Change room status
        String updateSql = "UPDATE room SET status = 'Booked' WHERE room_id = ?";
        PreparedStatement updatePs = con.prepareStatement(updateSql);
        updatePs.setInt(1, roomId);
        updatePs.executeUpdate();

        System.out.println("Room booked successfully!");

        con.close();

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

// Check Out
public static void checkOut() {
    try {
        Connection con = DBConnection.getConnection();

        System.out.print("Enter room ID: ");
        int roomId = sc.nextInt();

        // Find active booking for this room
        String checkSql =
                "SELECT booking_id FROM booking " +
                "WHERE room_id = ? AND check_out IS NULL";

        PreparedStatement checkPs = con.prepareStatement(checkSql);
        checkPs.setInt(1, roomId);

        ResultSet rs = checkPs.executeQuery();

        if (!rs.next()) {
            System.out.println("No active booking found for this room!");
            con.close();
            return;
        }

        int bookingId = rs.getInt("booking_id");

        // Set checkout date
        String updateBooking =
                "UPDATE booking SET check_out = CURDATE() " +
                "WHERE booking_id = ?";

        PreparedStatement bookingPs = con.prepareStatement(updateBooking);
        bookingPs.setInt(1, bookingId);
        bookingPs.executeUpdate();

        // Make room available again
        String updateRoom =
                "UPDATE room SET status = 'Available' " +
                "WHERE room_id = ?";

        PreparedStatement roomPs = con.prepareStatement(updateRoom);
        roomPs.setInt(1, roomId);
        roomPs.executeUpdate();

        System.out.println("Check-out successful!");
        System.out.println("Room is now available.");

        con.close();

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

// View Bookings
public static void viewBookings() {
    try {
        Connection con = DBConnection.getConnection();

        String sql = "SELECT b.booking_id, c.name, r.room_number, " +
                     "r.room_type, b.check_in, b.check_out " +
                     "FROM booking b " +
                     "JOIN customer c ON b.customer_id = c.customer_id " +
                     "JOIN room r ON b.room_id = r.room_id";

        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        System.out.println("\n========== BOOKINGS ==========");

        while (rs.next()) {
            System.out.println(
                "Booking ID: " + rs.getInt("booking_id") +
                " | Customer: " + rs.getString("name") +
                " | Room: " + rs.getInt("room_number") +
                " | Type: " + rs.getString("room_type") +
                " | Check-in: " + rs.getDate("check_in") +
                " | Check-out: " + rs.getDate("check_out")
            );
        }

        con.close();

    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}
}