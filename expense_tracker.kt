import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class Expense(
    val id: Int,
    val date: LocalDate,
    val amount: Double,
    val category: String,
    val description: String
)

class ExpenseTracker {
    private val expenses = mutableListOf<Expense>()
    private var nextId = 1

    fun addExpense(date: LocalDate, amount: Double, category: String, description: String) {
        expenses.add(Expense(nextId++, date, amount, category, description))
        println("Expense added successfully.")
    }

    fun listExpenses() {
        if (expenses.isEmpty()) {
            println("No expenses recorded.")
        } else {
            expenses.forEach { expense ->
                println("[${expense.id}] ${expense.date} - $${expense.amount} - ${expense.category}: ${expense.description}")
            }
        }
    }

    fun getTotalExpenses(): Double {
        return expenses.sumOf { it.amount }
    }

    fun getExpensesByCategory(): Map<String, Double> {
        return expenses.groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    fun getMonthlyExpenses(): Map<String, Double> {
        return expenses.groupBy { it.date.format(DateTimeFormatter.ofPattern("yyyy-MM")) }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }

    fun getAverageExpensePerDay(): Double {
        if (expenses.isEmpty()) return 0.0
        val dateRange = expenses.maxOf { it.date }.toEpochDay() - expenses.minOf { it.date }.toEpochDay() + 1
        return getTotalExpenses() / dateRange
    }
}

class ExpenseAnalytics(private val expenseTracker: ExpenseTracker) {
    fun showSummary() {
        println("\nExpense Summary:")
        println("Total Expenses: $${String.format("%.2f", expenseTracker.getTotalExpenses())}")
        println("Average Daily Expense: $${String.format("%.2f", expenseTracker.getAverageExpensePerDay())}")

        println("\nExpenses by Category:")
        expenseTracker.getExpensesByCategory().forEach { (category, amount) ->
            println("$category: $${String.format("%.2f", amount)}")
        }

        println("\nMonthly Expenses:")
        expenseTracker.getMonthlyExpenses().forEach { (month, amount) ->
            println("$month: $${String.format("%.2f", amount)}")
        }
    }
}

class CLI(private val expenseTracker: ExpenseTracker, private val expenseAnalytics: ExpenseAnalytics) {
    private val scanner = Scanner(System.`in`)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun start() {
        println("Welcome to the Expense Tracker!")
        var running = true
        while (running) {
            println("\nPlease choose an option:")
            println("1. Add an expense")
            println("2. List all expenses")
            println("3. Show expense summary and analytics")
            println("4. Exit")
            print("Enter your choice (1-4): ")

            when (scanner.nextLine()) {
                "1" -> addExpense()
                "2" -> expenseTracker.listExpenses()
                "3" -> expenseAnalytics.showSummary()
                "4" -> {
                    running = false
                    println("Thank you for using the Expense Tracker. Goodbye!")
                }
                else -> println("Invalid option. Please try again.")
            }
        }
    }

    private fun addExpense() {
        print("Enter date (YYYY-MM-DD): ")
        val dateString = scanner.nextLine()
        val date = LocalDate.parse(dateString, dateFormatter)

        print("Enter amount: ")
        val amount = scanner.nextDouble()
        scanner.nextLine() // Consume newline

        print("Enter category: ")
        val category = scanner.nextLine()

        print("Enter description: ")
        val description = scanner.nextLine()

        expenseTracker.addExpense(date, amount, category, description)
    }
}

fun main() {
    val expenseTracker = ExpenseTracker()
    val expenseAnalytics = ExpenseAnalytics(expenseTracker)
    val cli = CLI(expenseTracker, expenseAnalytics)
    cli.start()
}