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

public abstract class AsyncOpMode : OpMode() {
    abstract suspend fun op_mode()
    lateinit var start_event: Event;
    lateinit var stop_event: Event;

    private lateinit var dispatcher: AsyncOpModeDispatcher
    private lateinit var exception_handler: ExceptionHandler
    private lateinit var async_scope: CoroutineScope;
    fun launch(task: suspend CoroutineScope.() -> Unit): Job {
        return async_scope.launch(block = task)
    }
    fun<R> async(task: suspend CoroutineScope.() -> R): Deferred<R> {
        return async_scope.async(block = task)
    }
    fun mk_scope(): CoroutineScope = async_scope

    private class TelemetryStore(var ttl: Int, var content: String)
    private val telemetry_store = HashMap<String, TelemetryStore>()
    fun log(name: String, value: Any) {
        val store = telemetry_store.get(name)
        if (store != null) {
            store.content = value.toString()
            store.ttl = 5
        } else {
            telemetry_store.set(name, TelemetryStore(5, value.toString()))
        }
    }
    private fun do_telemetry() {
        telemetry.clearAll()
        val items = telemetry_store.iterator()
        while (items.hasNext()) {
            val item = items.next()
            item.value.ttl--;
            if (item.value.ttl == 0) items.remove()
            else (telemetry.addData(item.key, item.value.content))
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

    suspend fun while_live(f: suspend (it: () -> Unit) -> Unit) {
        var cancelled = false
        while (start_event.has_fired() && !stop_event.has_fired()) {
            f { cancelled = true }
            if (cancelled) break
            yield()
        }
    }

}

private class AsyncOpModeDispatcher(op: AsyncOpMode) : CoroutineDispatcher() {
    val dispatches = LinkedBlockingQueue<Runnable>()
    var error: Throwable? = null
    val op = op
    //val dispatches_unimportant = LinkedList<Runnable>()

    fun execute() {
        println("Begin execute")
        if (error != null) {
            op.telemetry.addData("Error", error.toString())
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
        System.out.println("Alador!")
        System.out.println("==========================")
        System.out.println(e)
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