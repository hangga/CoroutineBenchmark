package id.web.hangga

import kotlinx.benchmark.*
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.Fork
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 10)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
class CoroutineBenchmark {

    private val transactionCount = 100  // Jumlah transaksi yang akan diuji
    private val processedTransactions = AtomicInteger(0)

    // Simulasi validasi transaksi (misalnya verifikasi saldo)
    private suspend fun validateTransaction(transactionId: Int): Boolean {
        delay(Random.nextLong(5, 20)) // Simulasi latensi validasi
        return transactionId % 10 != 0  // Simulasi beberapa transaksi gagal (misalnya saldo kurang)
    }

    // Simulasi pemrosesan transaksi
    private suspend fun processTransaction(transactionId: Int) {
        if (validateTransaction(transactionId)) {
            delay(Random.nextLong(10, 50)) // Simulasi waktu proses transaksi
            processedTransactions.incrementAndGet()
        }
    }

    @Benchmark
    fun benchmarkSequentialTransactions() = runBlocking {
        val time = measureTimeMillis {
            for (id in 1..transactionCount) {
                processTransaction(id)
            }
        }
        println("Sequential processing time: $time ms")
    }

    @Benchmark
    fun benchmarkDefaultDispatchers() = runBlocking {
        benchmarkDispatcher("Default", Dispatchers.Default)
    }

    @Benchmark
    fun benchmarkIODispatchers() = runBlocking {
        benchmarkDispatcher("IO", Dispatchers.IO)
    }

    @Benchmark
    fun benchmarkUnconfinedDispatchers() = runBlocking {
        benchmarkDispatcher("Unconfined", Dispatchers.Unconfined)
    }

    @Benchmark
    fun benchmarkMainDispatchers() = runBlocking {
        benchmarkDispatcher("Main", Dispatchers.Main)
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
