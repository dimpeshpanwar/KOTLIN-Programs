// filename: Calculator.kt
// Compile: kotlinc Calculator.kt -include-runtime -d Calculator.jar
// Run: java -jar Calculator.jar 15 + 5

fun main(args: Array<String>) {
    if (args.size != 3) {
        println("Usage: java -jar Calculator.jar <num1> <op> <num2>")
        return
    }

    val a = args[0].toDouble()
    val op = args[1]
    val b = args[2].toDouble()

    val result = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b != 0.0) a / b else "Error: Divide by zero"
        else -> "Invalid operator"
    }

    println("Result: $result")
}
