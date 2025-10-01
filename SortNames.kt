// filename: SortNames.kt
// Compile: kotlinc SortNames.kt -include-runtime -d SortNames.jar
// Run: java -jar SortNames.jar
fun main() {
 val names = listOf("Ravi", "Anjali", "Karan", "Maya", "Sita")
 println("Original: $names")
 println("Sorted: ${names.sorted()}")
}
