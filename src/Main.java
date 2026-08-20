import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\n==============================");
            System.out.println("     HOTEL MANAGEMENT SYSTEM");
            System.out.println("==============================");

            System.out.println("1. Add Customer");
            System.out.println("2. View Customers");
            System.out.println("3. View Rooms");
            System.out.println("4. Book Room");
            System.out.println("5. Check Out");
            System.out.println("6. View Bookings");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

    case 1:
        Hotel.addCustomer();
        break;

    case 2:
        Hotel.viewCustomers();
        break;

    case 3:
        Hotel.viewRooms();
        break;

    case 4:
        Hotel.bookRoom();
        break;

    case 5:
        Hotel.checkOut();
        break;

    case 6:
        Hotel.viewBookings();
        break;

    case 7:
        System.out.println("Thank you!");
        sc.close();
        return;

    default:
        System.out.println("Invalid choice!");
}
        }
    }
}