// Show rooms

function showRooms() {

    document.getElementById("rooms").scrollIntoView({
        behavior: "smooth"
    });

}


// Book room

async function bookRoom(roomId) {

    const customerId = prompt(
        "Enter your Customer ID:"
    );

    if (customerId === null || customerId.trim() === "") {
        return;
    }

    const checkIn = prompt(
        "Enter check-in date (YYYY-MM-DD):"
    );

    if (checkIn === null || checkIn.trim() === "") {
        return;
    }

    try {

        const response = await fetch(
            "http://localhost:8080/bookRoom",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body:
                    "customerId=" +
                    encodeURIComponent(customerId) +

                    "&roomId=" +
                    encodeURIComponent(roomId) +

                    "&checkIn=" +
                    encodeURIComponent(checkIn)
            }
        );

        const result = await response.text();

        alert(result);

        if (result === "Room booked successfully!") {

            loadRooms();
        }

    } catch (error) {

        console.error(error);

        alert(
            "Could not connect to Java backend."
        );
    }
}


// Add customer

document.getElementById("customerForm").addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();

        const name =
            document.getElementById("customerName").value;

        const phone =
            document.getElementById("customerPhone").value;

        try {

            const response = await fetch(
                "http://localhost:8080/addCustomer",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        "name=" + encodeURIComponent(name) +
                        "&phone=" + encodeURIComponent(phone)
                }
            );

            const result = await response.text();

            alert(result);

            if (result === "Customer added successfully!") {
                document.getElementById("customerForm").reset();
            }

        } catch (error) {

            alert(
                "Could not connect to Java backend."
            );

            console.error(error);
        }
    }
);

// Load customers from Java backend

async function loadCustomers() {

    try {

        const response = await fetch(
            "http://localhost:8080/customers"
        );

        const customers = await response.json();

        const customerList =
            document.getElementById("customerList");

        if (customers.length === 0) {

            customerList.innerHTML =
                "<p>No customers found.</p>";

            return;
        }

        customerList.innerHTML = "";

        customers.forEach(function(customer) {

            const customerDiv =
                document.createElement("div");

            customerDiv.className = "customer-card";

            customerDiv.innerHTML =
                "<h3>" + customer.name + "</h3>" +
                "<p>Customer ID: " + customer.id + "</p>" +
                "<p>Phone: " + customer.phone + "</p>";

            customerList.appendChild(customerDiv);

        });

    } catch (error) {

        console.error(error);

        document.getElementById("customerList").innerHTML =
            "<p>Could not load customers.</p>";
    }
}

// Load rooms from Java backend

async function loadRooms() {

    try {

        const response = await fetch(
            "http://localhost:8080/rooms"
        );

        const rooms = await response.json();

        const roomList =
            document.getElementById("roomList");

        roomList.innerHTML = "";

        if (rooms.length === 0) {

            roomList.innerHTML =
                "<p>No rooms found.</p>";

            return;
        }

        rooms.forEach(function(room) {

            const roomCard =
                document.createElement("div");

            roomCard.className = "room-card";

            let statusClass = "available";

            if (room.status !== "Available") {
                statusClass = "booked";
            }

            roomCard.innerHTML =
                "<h3>Room " + room.number + "</h3>" +

                "<p>Type: " + room.type + "</p>" +

                "<p>Price: ₹" + room.price +
                " / night</p>" +

                "<span class='" + statusClass + "'>" +
                room.status +
                "</span>" +

                (
                    room.status === "Available"
                    ?
                    "<br><br><button onclick='bookRoom(" +
                    room.id +
                    ")'>Book Now</button>"
                    :
                    ""
                );

            roomList.appendChild(roomCard);

        });

    } catch (error) {

        console.error(error);

        document.getElementById("roomList").innerHTML =
            "<p>Could not load rooms.</p>";
    }
}
loadRooms();

// Check out customer

document.getElementById("checkoutForm").addEventListener(
    "submit",
    async function(event) {

        event.preventDefault();

        const bookingId =
            document.getElementById("bookingId").value;

        try {

            const response = await fetch(
                "http://localhost:8080/checkout",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        "bookingId=" +
                        encodeURIComponent(bookingId)
                }
            );

            const result = await response.text();

            alert(result);

            if (
                result ===
                "Customer checked out successfully!"
            ) {

                document
                    .getElementById("checkoutForm")
                    .reset();

                // Refresh room status
                loadRooms();
            }

        } catch (error) {

            console.error(error);

            alert(
                "Could not connect to Java backend."
            );
        }
    }
);

// Load bookings from Java backend

async function loadBookings() {

    try {

        const response = await fetch(
            "http://localhost:8080/bookings"
        );

        const bookings = await response.json();

        const bookingMessage =
            document.getElementById("bookingMessage");

        if (bookings.length === 0) {

            bookingMessage.innerHTML =
                "<p>No bookings found.</p>";

            return;
        }

        bookingMessage.innerHTML = "";

        bookings.forEach(function(booking) {

            const bookingCard =
                document.createElement("div");

            bookingCard.className = "booking-card";

            let checkoutText =
                booking.checkOut === null
                ? "Not checked out"
                : booking.checkOut;

            bookingCard.innerHTML =
                "<h3>Booking ID: " +
                booking.id +
                "</h3>" +

                "<p>Customer: " +
                booking.customer +
                "</p>" +

                "<p>Room: " +
                booking.room +
                "</p>" +

                "<p>Room Type: " +
                booking.type +
                "</p>" +

                "<p>Check-in: " +
                booking.checkIn +
                "</p>" +

                "<p>Check-out: " +
                checkoutText +
                "</p>";

            bookingMessage.appendChild(bookingCard);

        });

    } catch (error) {

        console.error(error);

        document.getElementById("bookingMessage").innerHTML =
            "<p>Could not load bookings.</p>";
    }
}