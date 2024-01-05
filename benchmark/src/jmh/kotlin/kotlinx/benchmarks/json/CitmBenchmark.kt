package kotlinx.benchmarks.json

import kotlinx.benchmarks.model.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.json.okio.*
import kotlinx.serialization.json.kotlinx.io.*
import okio.*
import okio.FileSystem
import okio.Path.Companion.toPath
import org.openjdk.jmh.annotations.*
import java.nio.channels.FileChannel
import java.nio.file.*
import java.util.concurrent.*

@Warmup(iterations = 7, time = 1)
@Measurement(iterations = 7, time = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(2)
open class CitmBenchmark {
    /*
     * For some reason Citm is kind of de-facto standard cross-language benchmark.
     * Order of magnitude: 200 ops/sec
     */
    private val input = CitmBenchmark::class.java.getResource("/citm_catalog.json").readBytes().decodeToString()
    private val citm = Json.decodeFromString(CitmCatalog.serializer(), input)
    private val devNullSink = blackholeSink().buffer()
    private val devNullKxIoSink = discardingSink().buffered()
    private val devNullFileSink = FileSystem.SYSTEM.sink("/dev/null".toPath()).buffer()
    private val devNullKxIoFileSink = SystemFileSystem.sink(Path("/dev/null")).buffered()
    private val devNullChannel = FileChannel.open(Paths.get("/dev/null"),
        StandardOpenOption.WRITE)

    @Setup
    fun init() {
        require(citm == Json.decodeFromString(CitmCatalog.serializer(), Json.encodeToString(citm)))
    }

    @TearDown
    fun closeFiles() {
        devNullFileSink.close()
        devNullKxIoFileSink.close()
        devNullChannel.close()
    }

    @Benchmark
    fun decodeCitm(): CitmCatalog = Json.decodeFromString(CitmCatalog.serializer(), input)

    @Benchmark
    fun encodeCitm(): String = Json.encodeToString(CitmCatalog.serializer(), citm)

    @Benchmark
    fun encodeCitmOkio() = Json.encodeToBufferedSink(CitmCatalog.serializer(), citm, devNullSink)

    @Benchmark
    fun encodeCitmOkioFile() {
        Json.encodeToBufferedSink(CitmCatalog.serializer(), citm, devNullFileSink)
        devNullFileSink.flush()
    }

    @Benchmark
    fun encodeCitmKotlinxIoileChannel() {
        val buffer = kotlinx.io.Buffer()
        Json.encodeToBufferedSink(CitmCatalog.serializer(), citm, buffer)
        devNullChannel.write(buffer)
    }

    @Benchmark
    fun encodeCitmKotlinxIo() = Json.encodeToBufferedSink(CitmCatalog.serializer(), citm, devNullKxIoSink)

    @Benchmark
    fun encodeCitmKotlinxIoFile() {
        Json.encodeToBufferedSink(CitmCatalog.serializer(), citm, devNullKxIoFileSink)
        devNullFileSink.flush()
    }

    @Benchmark
    fun encodeCitmKotlinxIoFileChannel() {
        val buffer = kotlinx.io.Buffer()
        Json.encodeToBufferedSink(CitmCatalog.serializer(), citm, buffer)
        devNullChannel.write(buffer)
    }
}
