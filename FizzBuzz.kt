// filename: FizzBuzz.kt
// Compile: kotlinc FizzBuzz.kt -include-runtime -d FizzBuzz.jar
// Run: java -jar FizzBuzz.jar

fun main() {
    (1..50).forEach {
        val output = when {
            it % 15 == 0 -> "FizzBuzz"
            it % 3 == 0 -> "Fizz"
            it % 5 == 0 -> "Buzz"
            else -> it.toString()
        }
        println(output)
    }
}
