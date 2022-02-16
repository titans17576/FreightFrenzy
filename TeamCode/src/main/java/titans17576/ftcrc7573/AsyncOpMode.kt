package titans17576.ftcrc7573

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import kotlinx.coroutines.*
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaType

lateinit var OP: AsyncOpMode;

/**
 * Asynchronous OP Mode
 *
 */
public abstract class AsyncOpMode : OpMode() {
    /**
     * The "main" method for the op mode. Run when the operator presses the "Init" button on the
     * driver station. It's primary purpose should be to launch subsystems.
     */
    abstract suspend fun op_mode()

    /**
     * The event that fires when the operator presses the "Start" button on the driver station.
     * The event will become invalid when the operator presses the "Stop" button, or the OP mode
     * stops in some other way.
     */
    lateinit var start_event: Event private set;

    /**
     * The event that fires when the operator presses the "Stop" button on the driver station,
     * or the OP mode stops in some other way.
     */
    lateinit var stop_event: Event private set;

    private lateinit var dispatcher: AsyncOpModeDispatcher
    private lateinit var exception_handler: ExceptionHandler
    private lateinit var async_scope: CoroutineScope;

    /**
     * Launch a new coroutine. This coroutine will run in parallel to other all others launched.
     * You should use this to launch your subsystems.
     * See https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/launch.html
     * @param task - The new coroutine to launch
     * Example: {@code OP.launch { my_subsystem() } }
     * @return the job representing the launched coroutine
     */
    fun launch(task: suspend CoroutineScope.() -> Unit): Job {
        return async_scope.launch(block = task)
    }
    /**
     * Launch a new coroutine to produce a value asynchronously. This coroutine will run in parallel to other all others launched.
     * Admittedly, this doesn't have as much use with respect to FTC. You're probably more interested with {@link titans17576.ftcrc7573.AsyncOpMode#launch}
     * See https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/async.html
     * @param task - The new coroutine to launch
     * Example: {@code OP.launch { my_subsystem() } }
     * @return the deferred value from the launched coroutine
     */
    fun<R> async(task: suspend CoroutineScope.() -> R): Deferred<R> {
        return async_scope.async(block = task)
    }

    /**
     * Returns an async scope, incase you need to partake in any *shenanigans*.
     * See TODO PUT LINK HERE
     * @return the async scope for this OP mode
     */
    fun mk_scope(): CoroutineScope = async_scope

    private class TelemetryStore(var name: String, var content: String, var ttl: Int)
    private val telemetry_store = ArrayList<TelemetryStore>()

    /**
     * Logs a value to FTC telemetry. Because of the basically random execution order of Kotlin's coroutines,
     * the default telemetry becomes very messy very fast. This system caches values for a slight bit and manually
     * handles clearing and updating to keep telemetry clean.
     * @param label - the label to log next to the value
     * @param value - the value to log
     * @param ttl - the amount of internal executor iterations to cache the value for. Defaults to 5. Set to -1 to keep forever
     */
    fun log(name: String, value: Any, ttl: Int = 5) {
        for (entry in telemetry_store) {
            if (entry.name == name) {
                entry.content = value.toString()
                entry.ttl = ttl
                return;
            }
        }
        telemetry_store.add(TelemetryStore(name, value.toString(), ttl))
    }

    private fun do_telemetry() {
        telemetry.clearAll()
        val items = telemetry_store.iterator()
        while (items.hasNext()) {
            val item = items.next()
            item.ttl--;
            if (item.ttl == 0) items.remove()
            else (telemetry.addData(item.name, item.content))
        }
        telemetry.update()
    }

    override fun init() {
        dispatcher = AsyncOpModeDispatcher(this)
        exception_handler = ExceptionHandler(dispatcher)
        async_scope = CoroutineScope(EmptyCoroutineContext + dispatcher + exception_handler)
        start_event = Event()
        stop_event = Event()
        OP = this
        launch {
            yield()
            op_mode()
        }
        dispatcher.execute()
        telemetry.isAutoClear = false
        do_telemetry()
    }
    override fun init_loop() {
        dispatcher.execute()
        do_telemetry()
    }
    override fun start() {
        launch { start_event.fire() }
        dispatcher.execute()
        do_telemetry()
    }
    override fun loop() {
        dispatcher.execute()
        do_telemetry()
    }
    override fun stop() {
        launch {
            start_event.throw_exception(Exception("Op Mode Stopped"))
            stop_event.fire()
        }
        dispatcher.execute()
        do_telemetry()
        dispatcher.finish()
        do_telemetry()
    }

    /**
     * Run a block of code while the OP mode is in the "running" state. By default, it will return
     * immediately if the operator has not pressed the "Start" button on the driver station. The while loop will
     * yield after each iteration, allowing other coroutines to execute. A function is passed
     * as a parameter to act as a {@code break} statement. You can implicitly call it as {@code it()}
     * @param block - the code block to loop
     * @param require_started - whether to require that the operator has pressed the "Start" button on the driver station
     */
    suspend fun while_live(require_started: Boolean, f: suspend (it: () -> Unit) -> Unit) {
        var cancelled = false
        while ((!require_started || start_event.has_fired()) && !stop_event.has_fired()) {
            f { cancelled = true }
            if (cancelled) break
            yield()
        }
    }
    /**
     * Run a block of code while the OP mode is in the "running" state. By default, it will return
     * immediately if the operator has not pressed the "Start" button on the driver station. The while loop will
     * yield after each iteration, allowing other coroutines to execute. A function is passed
     * as a parameter to act as a {@code break} statement. You can implicitly call it as {@code it()}
     * @param block - the code block to loop
     * @param require_started - whether to require that the operator has pressed the "Start" button on the driver station
     */
    suspend fun while_live(f: suspend( it: () -> Unit) -> Unit) { while_live(true, f); }

    /**
     * Wait until a condition becomes true. The coroutine will yield between each
     * check, allowing other coroutines to run. This function will run regardless if the
     * operator has pressed the start button
     */
    suspend fun wait_for(f: suspend () -> Boolean) {
        while (!stop_event.has_fired()) {
            if (f()) return;
            yield();
        }
    }

}

private class AsyncOpModeDispatcher(op: AsyncOpMode) : CoroutineDispatcher() {
    val dispatches = LinkedBlockingQueue<Runnable>()
    var error: Throwable? = null
    val op = op
    //val dispatches_unimportant = LinkedList<Runnable>()

    fun execute() {
        //println("Begin execute")
        if (error != null) {
            op.telemetry.addData("Error", error.toString())
            println("Error! Alador!")
            println(error)
            dispatches.clear()
            op.requestOpModeStop()
            return
        }

        //println("Begin dispatcher exec")
        val size = dispatches.size
        for (i in 0..size) {
            try {
                dispatches.poll()?.run()
                if (error != null) return
            } catch(e: Throwable) {
                dispatches.clear()
                error = e
                op.telemetry.addData("Error", e.toString())
                op.telemetry.update()
                System.out.println("Alador! Throwable")
                System.out.println("==========================")
                System.out.println(e)
                e.printStackTrace()
                System.out.println("==========================")
                op.requestOpModeStop()
                return
            }
        }
    }
    //println("End dispatcher exec")

    fun finish() {
        //TODO potentially implement timeout to force kill
        var tries_left = 3
        while (dispatches.isNotEmpty() && tries_left > 0) {
            tries_left -= 1
            execute()
        }
        if (tries_left < 1) println("DISPATCHER FORCE STOPPING!!!!!!!!!!!!!!!!!!!")
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatches.put(block)
    }
}

private class ExceptionHandler(dispatcher: AsyncOpModeDispatcher,
                               override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler
) : CoroutineExceptionHandler {
    val dis = dispatcher

    override fun handleException(context: CoroutineContext, e: Throwable) {
        dis.dispatches.clear()
        dis.error = e
        dis.op.telemetry.addData("Error", e.toString())
        dis.op.telemetry.update()
        System.out.println("Alador Again!")
        System.out.println("==========================")
        System.out.println(e)
        e.printStackTrace()
        System.out.println("==========================")
        dis.op.requestOpModeStop()

    }
}

interface DeferredAsyncOpMode {
    suspend fun op_mode()
}

var current_op_mode_manager: OpModeManager? = null
fun register_opmode(name: String, is_autonomous: Boolean, type: OpMode) {
    current_op_mode_manager!!.register(OpModeMeta.Builder()
        .setName(name)
        .setFlavor(if (is_autonomous) OpModeMeta.Flavor.AUTONOMOUS else OpModeMeta.Flavor.TELEOP)
        .setSource(OpModeMeta.Source.ANDROID_STUDIO)
        .build(),
        type
    );
}
fun<T: DeferredAsyncOpMode> register_defered_async_opmode_legacy(name: String, is_autonomous: Boolean, type: KClass<T>) {
    class DeferredAsyncOpModeHolder : AsyncOpMode() {
        override suspend fun op_mode() {
            val yes = (type.constructors.iterator().next().call(this))
            yes.op_mode()
        }
    }
    println(type.constructors.iterator().next().parameters[0].type.javaType == AsyncOpMode::class.javaObjectType)
    println("Sir alador!")
    current_op_mode_manager!!.register(OpModeMeta.Builder()
        .setName(name)
        .setFlavor(if (is_autonomous) OpModeMeta.Flavor.AUTONOMOUS else OpModeMeta.Flavor.TELEOP)
        .setSource(OpModeMeta.Source.ANDROID_STUDIO)
        .build(),
        DeferredAsyncOpModeHolder()
    )
}
fun register_defered_async_opmode(name: String, is_autonomous: Boolean, factory: (op: AsyncOpMode) -> DeferredAsyncOpMode) {
    class DeferredAsyncOpModeHolder : AsyncOpMode() {
        override suspend fun op_mode() {
            val yes = factory(this)
            yes.op_mode()
        }
    }
    current_op_mode_manager!!.register(OpModeMeta.Builder()
        .setName(name)
        .setFlavor(if (is_autonomous) OpModeMeta.Flavor.AUTONOMOUS else OpModeMeta.Flavor.TELEOP)
        .setSource(OpModeMeta.Source.ANDROID_STUDIO)
        .build(),
        DeferredAsyncOpModeHolder()
    )
}