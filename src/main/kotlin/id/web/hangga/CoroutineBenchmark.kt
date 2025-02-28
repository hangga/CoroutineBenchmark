package id.web.hangga

import kotlinx.benchmark.*
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
class CoroutineBenchmark {

    private val transactionCount = 100  // Number of transactions to be tested
    private val processedTransactions = AtomicInteger(0)
    private lateinit var transactions: List<Int>


    @Setup(Level.Iteration) // Menyiapkan transaksi sebelum setiap iterasi benchmark
    fun setup() {
        transactions = List(transactionCount) { it + 1 }
        processedTransactions.set(0)
    }

    // Simulate transaction validation (e.g., balance verification)
    private suspend fun validateTransaction(transactionId: Int): Boolean {
        delay(Random.nextLong(5, 20)) // Simulate validation latency
        return transactionId % 10 != 0  // Simulate some failed transactions (e.g., insufficient balance)
    }

    // Simulate transaction processing
    private suspend fun processTransaction(transactionId: Int) {
        if (validateTransaction(transactionId)) {
            delay(Random.nextLong(10, 50)) // Simulate transaction processing time
            processedTransactions.incrementAndGet()
        }
    }

    @Benchmark
    fun sequentialTransactions() = runBlocking {
        val time = measureTimeMillis {
            for (id in 1..transactionCount) {
                processTransaction(id)
            }
        }
        println("Sequential processing time: $time ms")
    }

    @Benchmark
    fun defaultDispatchersTransactions() = runBlocking {
        benchmarkDispatcher("Default", Dispatchers.Default)
    }

    @Benchmark
    fun iODispatchersTransactions() = runBlocking {
        benchmarkDispatcher("IO", Dispatchers.IO)
    }

    @Benchmark
    fun unconfinedDispatchersTransactions() = runBlocking {
        benchmarkDispatcher("Unconfined", Dispatchers.Unconfined)
    }

    private suspend fun benchmarkDispatcher(name: String, dispatcher: CoroutineDispatcher) {
        val time = measureTimeMillis {
            coroutineScope {
                (1..transactionCount).map { id ->
                    launch(dispatcher) { processTransaction(id) }
                }.joinAll()
            }
        }
        println("Concurrent processing time on $name: $time ms")
    }
}
