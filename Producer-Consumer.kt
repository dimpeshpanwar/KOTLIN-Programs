import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

// Producer function to produce items and add them to the queue
fun producer(queue: BlockingQueue<Int>, stopFlag: BooleanArray) {
    while (!stopFlag[0]) {
        val item = (1..100).random()
        println("Producer produced: $item")
        queue.put(item)  // Adds item to the queue (blocks if full)
        Thread.sleep((500..1000).random().toLong())  // Simulate time to produce
    }
    println("Producer stopping...")
}

// Consumer function to consume items from the queue
fun consumer(queue: BlockingQueue<Int>, stopFlag: BooleanArray) {
    while (!stopFlag[0] || queue.isNotEmpty()) {
        val item = queue.take()  // Take item from the queue (blocks if empty)
        println("Consumer consumed: $item")
        Thread.sleep((200..500).random().toLong())  // Simulate processing time
    }
    println("Consumer stopping...")
}

fun main() {
    val queue: BlockingQueue<Int> = ArrayBlockingQueue(10)  // Capacity 10
    val stopFlag = booleanArrayOf(false)

    // Start producer and consumer threads
    val producerThread = thread { producer(queue, stopFlag) }
    val consumerThread = thread { consumer(queue, stopFlag) }

    // Let them run for 5 seconds
    Thread.sleep(5000)

    // Stop producer and consumer
    stopFlag[0] = true

    // Wait for both threads to finish
    producerThread.join()
    consumerThread.join()

    println("All tasks completed.")
}
