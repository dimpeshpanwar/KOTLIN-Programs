fun main() {
    val todos = mutableListOf<String>()
    while (true) {
        println("\n1. Add Task\n2. View Tasks\n3. Remove Task\n4. Exit")
        print("Choose option: ")
        when (readLine()?.toIntOrNull()) {
            1 -> {
                print("Enter task: ")
                val task = readLine()
                if (!task.isNullOrBlank()) todos.add(task)
                println("✅ Task added!")
            }
            2 -> {
                if (todos.isEmpty()) println("No tasks yet!") 
                else todos.forEachIndexed { i, t -> println("${i + 1}. $t") }
            }
            3 -> {
                print("Enter task number to remove: ")
                val num = readLine()?.toIntOrNull()
                if (num != null && num in 1..todos.size) {
                    todos.removeAt(num - 1)
                    println("🗑 Task removed.")
                } else println("Invalid number!")
            }
            4 -> return
            else -> println("Invalid option!")
        }
    }
}
