import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

// ========== Domain Models ==========

sealed class TaskResult<out T> {
    data class Success<T>(val value: T, val duration: Duration) : TaskResult<T>()
    data class Failure(val error: Throwable, val retries: Int) : TaskResult<Nothing>()
    object Cancelled : TaskResult<Nothing>()
}

sealed class TaskPriority(val weight: Int) {
    object Critical : TaskPriority(100)
    object High : TaskPriority(75)
    object Normal : TaskPriority(50)
    object Low : TaskPriority(25)
}

data class Task<T>(
    val id: String,
    val priority: TaskPriority,
    val maxRetries: Int = 3,
    val timeout: Duration = Duration.ofSeconds(30),
    val dependencies: List<String> = emptyList(),
    val execute: suspend () -> T
)

data class TaskMetrics(
    val totalExecuted: AtomicInteger = AtomicInteger(0),
    val totalSucceeded: AtomicInteger = AtomicInteger(0),
    val totalFailed: AtomicInteger = AtomicInteger(0),
    val averageExecutionTime: MutableList<Long> = mutableListOf()
)

// ========== Event System ==========

sealed class SystemEvent {
    data class TaskSubmitted(val taskId: String, val priority: TaskPriority) : SystemEvent()
    data class TaskStarted(val taskId: String, val workerId: Int) : SystemEvent()
    data class TaskCompleted<T>(val taskId: String, val result: TaskResult<T>) : SystemEvent()
    data class WorkerStatusChanged(val workerId: Int, val isActive: Boolean) : SystemEvent()
    data class SystemMetricsUpdate(val metrics: TaskMetrics) : SystemEvent()
}

interface EventListener {
    suspend fun onEvent(event: SystemEvent)
}

class EventBus {
    private val listeners = mutableListOf<EventListener>()
    private val eventFlow = MutableSharedFlow<SystemEvent>(replay = 10)

    fun subscribe(listener: EventListener) {
        listeners.add(listener)
    }

    suspend fun publish(event: SystemEvent) {
        eventFlow.emit(event)
        listeners.forEach { it.onEvent(event) }
    }

    fun events(): SharedFlow<SystemEvent> = eventFlow.asSharedFlow()
}

// ========== Worker Pool ==========

class Worker<T>(
    val id: Int,
    private val eventBus: EventBus,
    private val scope: CoroutineScope
) {
    private val _status = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _status.asStateFlow()

    suspend fun execute(task: Task<T>): TaskResult<T> {
        _status.value = true
        eventBus.publish(SystemEvent.WorkerStatusChanged(id, true))
        eventBus.publish(SystemEvent.TaskStarted(task.id, id))

        val start = Instant.now()
        var attempts = 0
        var lastError: Throwable? = null

        while (attempts <= task.maxRetries) {
            try {
                val result = withTimeout(task.timeout.toMillis()) {
                    task.execute()
                }
                val duration = Duration.between(start, Instant.now())
                _status.value = false
                eventBus.publish(SystemEvent.WorkerStatusChanged(id, false))
                return TaskResult.Success(result, duration)
            } catch (e: TimeoutCancellationException) {
                lastError = e
                attempts++
                delay(100L * attempts)
            } catch (e: CancellationException) {
                _status.value = false
                eventBus.publish(SystemEvent.WorkerStatusChanged(id, false))
                return TaskResult.Cancelled
            } catch (e: Exception) {
                lastError = e
                attempts++
                delay(100L * attempts)
            }
        }

        _status.value = false
        eventBus.publish(SystemEvent.WorkerStatusChanged(id, false))
        return TaskResult.Failure(lastError ?: Exception("Unknown error"), attempts)
    }
}

// ========== Task Scheduler with Dependency Resolution ==========

class TaskScheduler<T>(
    private val workerCount: Int = 4,
    private val eventBus: EventBus = EventBus()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val workers = List(workerCount) { Worker<T>(it, eventBus, scope) }
    private val taskQueue = Channel<Task<T>>(Channel.UNLIMITED)
    private val completedTasks = ConcurrentHashMap<String, TaskResult<T>>()
    private val pendingTasks = ConcurrentHashMap<String, Task<T>>()
    private val metrics = TaskMetrics()

    init {
        startWorkers()
        startMetricsCollector()
    }

    private fun startWorkers() {
        workers.forEach { worker ->
            scope.launch {
                for (task in taskQueue) {
                    if (areDependenciesMet(task)) {
                        val result = worker.execute(task)
                        handleTaskCompletion(task, result)
                    } else {
                        taskQueue.send(task) // Re-queue if dependencies not met
                        delay(100)
                    }
                }
            }
        }
    }

    private fun areDependenciesMet(task: Task<T>): Boolean {
        return task.dependencies.all { depId ->
            completedTasks[depId] is TaskResult.Success
        }
    }

    private suspend fun handleTaskCompletion(task: Task<T>, result: TaskResult<T>) {
        completedTasks[task.id] = result
        pendingTasks.remove(task.id)
        
        metrics.totalExecuted.incrementAndGet()
        when (result) {
            is TaskResult.Success -> {
                metrics.totalSucceeded.incrementAndGet()
                metrics.averageExecutionTime.add(result.duration.toMillis())
            }
            is TaskResult.Failure -> metrics.totalFailed.incrementAndGet()
            TaskResult.Cancelled -> {}
        }

        eventBus.publish(SystemEvent.TaskCompleted(task.id, result))
    }

    suspend fun submit(task: Task<T>) {
        pendingTasks[task.id] = task
        eventBus.publish(SystemEvent.TaskSubmitted(task.id, task.priority))
        taskQueue.send(task)
    }

    private fun startMetricsCollector() {
        scope.launch {
            while (isActive) {
                delay(5000)
                eventBus.publish(SystemEvent.SystemMetricsUpdate(metrics))
            }
        }
    }

    fun subscribeToEvents(listener: EventListener) {
        eventBus.subscribe(listener)
    }

    suspend fun getResult(taskId: String): TaskResult<T>? {
        while (pendingTasks.containsKey(taskId)) {
            delay(100)
        }
        return completedTasks[taskId]
    }

    fun shutdown() {
        scope.cancel()
        taskQueue.close()
    }
}

// ========== DSL Builder for Task Creation ==========

@DslMarker
annotation class TaskDsl

@TaskDsl
class TaskBuilder<T> {
    var id: String = ""
    var priority: TaskPriority = TaskPriority.Normal
    var maxRetries: Int = 3
    var timeout: Duration = Duration.ofSeconds(30)
    var dependencies: List<String> = emptyList()
    private var execution: (suspend () -> T)? = null

    fun execute(block: suspend () -> T) {
        execution = block
    }

    fun build(): Task<T> {
        require(id.isNotEmpty()) { "Task ID must not be empty" }
        require(execution != null) { "Task execution block must be defined" }
        return Task(id, priority, maxRetries, timeout, dependencies, execution!!)
    }
}

fun <T> task(init: TaskBuilder<T>.() -> Unit): Task<T> {
    return TaskBuilder<T>().apply(init).build()
}

// ========== Advanced Features: Flow Processing Pipeline ==========

class DataProcessor<IN, OUT>(
    private val bufferSize: Int = 100
) {
    fun process(
        input: Flow<IN>,
        transformations: List<suspend (IN) -> OUT>
    ): Flow<OUT> = flow {
        input
            .buffer(bufferSize)
            .map { item ->
                transformations.fold(item as Any) { acc, transform ->
                    transform(acc as IN)
                }
            }
            .collect { emit(it as OUT) }
    }
}

// ========== Complex Generic Type with Variance ==========

interface Repository<out T> {
    suspend fun getAll(): List<T>
    suspend fun getById(id: String): T?
}

class InMemoryRepository<T>(
    private val storage: MutableMap<String, T> = ConcurrentHashMap()
) : Repository<T> {
    suspend fun save(id: String, item: T) {
        storage[id] = item
    }

    override suspend fun getAll(): List<T> = storage.values.toList()

    override suspend fun getById(id: String): T? = storage[id]
}

// ========== Demo Application ==========

suspend fun main() = coroutineScope {
    val scheduler = TaskScheduler<Any>(workerCount = 4)
    
    // Subscribe to events
    scheduler.subscribeToEvents(object : EventListener {
        override suspend fun onEvent(event: SystemEvent) {
            when (event) {
                is SystemEvent.TaskCompleted<*> -> {
                    println("Task ${event.taskId} completed: ${event.result}")
                }
                is SystemEvent.SystemMetricsUpdate -> {
                    val avg = event.metrics.averageExecutionTime.average()
                    println("Metrics - Success: ${event.metrics.totalSucceeded.get()}, " +
                            "Failed: ${event.metrics.totalFailed.get()}, " +
                            "Avg Time: ${avg}ms")
                }
                else -> {}
            }
        }
    })

    // Create tasks using DSL
    val task1 = task<Int> {
        id = "task-1"
        priority = TaskPriority.Critical
        maxRetries = 2
        execute {
            delay(Random.nextLong(100, 500))
            println("Executing task-1")
            42
        }
    }

    val task2 = task<String> {
        id = "task-2"
        priority = TaskPriority.High
        dependencies = listOf("task-1")
        execute {
            delay(Random.nextLong(100, 500))
            println("Executing task-2 (depends on task-1)")
            "Result from task 2"
        }
    }

    val task3 = task<Double> {
        id = "task-3"
        priority = TaskPriority.Normal
        dependencies = listOf("task-1", "task-2")
        execute {
            delay(Random.nextLong(100, 500))
            println("Executing task-3 (depends on task-1 and task-2)")
            3.14159
        }
    }

    // Submit tasks
    scheduler.submit(task1 as Task<Any>)
    scheduler.submit(task2 as Task<Any>)
    scheduler.submit(task3 as Task<Any>)

    // Wait for results
    val result1 = scheduler.getResult("task-1")
    val result2 = scheduler.getResult("task-2")
    val result3 = scheduler.getResult("task-3")

    println("\nFinal Results:")
    println("Task 1: $result1")
    println("Task 2: $result2")
    println("Task 3: $result3")

    delay(6000) // Wait for metrics update
    scheduler.shutdown()
}