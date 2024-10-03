fun main() {
    println("Welcome to Vehicle Service Request Chatbot!")
    println("Let's get started with your vehicle service request.")
    
    // Ask for the user's name
    print("What's your name? ")
    val name = readLine() ?: "Customer"

    // Ask for the vehicle type
    print("What type of vehicle do you have (e.g., Car, Bike, Truck)? ")
    val vehicleType = readLine() ?: "Unknown"

    // Ask for the vehicle model
    print("What's your vehicle model? ")
    val vehicleModel = readLine() ?: "Unknown"

    // Ask for the vehicle registration number
    print("Please provide your vehicle registration number: ")
    val registrationNumber = readLine() ?: "Not Provided"

    // Ask for the type of service
    println("What type of service do you need?")
    println("1. General Maintenance")
    println("2. Oil Change")
    println("3. Tire Replacement")
    println("4. Brake Inspection")
    print("Enter the number of the service type: ")
    val serviceType = when (readLine()?.toIntOrNull()) {
        1 -> "General Maintenance"
        2 -> "Oil Change"
        3 -> "Tire Replacement"
        4 -> "Brake Inspection"
        else -> "Other Service"
    }

    // Ask for preferred service date
    print("When would you like to schedule your service (e.g., 2024-10-05)? ")
    val serviceDate = readLine() ?: "Not Specified"

    // Display the service request summary
    println("\n--- Service Request Summary ---")
    println("Customer Name: $name")
    println("Vehicle Type: $vehicleType")
    println("Vehicle Model: $vehicleModel")
    println("Registration Number: $registrationNumber")
    println("Service Type: $serviceType")
    println("Preferred Service Date: $serviceDate")
    println("\nThank you! Your vehicle service request has been submitted.")
}
