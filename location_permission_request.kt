import java.util.Scanner
// program to grant location permission
class LocationPermissionHandler {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private var isLocationPermissionGranted = false

    fun requestLocationPermission() {
        when {
            isLocationPermissionGranted -> {
                println("Location permission is already granted")
            }
            shouldShowRequestPermissionRationale() -> {
                println("Location permission is required for this application")
                println("Do you want to grant location permission? (yes/no)")

                val scanner = Scanner(System.`in`)
                val response = scanner.nextLine().toLowerCase()

                if (response == "yes") {
                    requestPermission()
                } else {
                    println("Permission denied by user")
                }
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        // In a real application, this would check if we need to show a rationale
        // For this example, we'll always return true
        return true
    }

    private fun requestPermission() {
        println("Requesting location permission...")
        // Simulating a permission request
        println("Do you grant location permission? (yes/no)")

        val scanner = Scanner(System.`in`)
        val response = scanner.nextLine().toLowerCase()

        onRequestPermissionsResult(LOCATION_PERMISSION_REQUEST_CODE, response == "yes")
    }

    private fun onRequestPermissionsResult(requestCode: Int, isGranted: Boolean) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (isGranted) {
                    isLocationPermissionGranted = true
                    println("Location permission granted")
                } else {
                    println("Location permission denied")
                }
            }
            else -> {
                println("Unhandled permission request")
            }
        }
    }
}

fun main() {
    val permissionHandler = LocationPermissionHandler()
    permissionHandler.requestLocationPermission()
}