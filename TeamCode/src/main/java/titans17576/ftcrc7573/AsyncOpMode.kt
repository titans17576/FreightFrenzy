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
    lateinit var start_signal: Signal;
    lateinit var stop_signal: Signal;

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


    override fun init() {
        dispatcher = AsyncOpModeDispatcher(this)
        exception_handler = ExceptionHandler(dispatcher)
        async_scope = CoroutineScope(EmptyCoroutineContext + dispatcher + exception_handler)
        start_signal = Signal()
        stop_signal = Signal()
        OP = this
        launch {
            yield()
            op_mode()
        }
        dispatcher.execute()
        telemetry.update()
    }
    override fun init_loop() {
        dispatcher.execute()
        telemetry.update()
    }
    override fun start() {
        launch { start_signal.greenlight() }
        dispatcher.execute()
        telemetry.update()
    }
    override fun loop() {
        dispatcher.execute()
        telemetry.update()
    }
    override fun stop() {
        launch {
            start_signal.blow_up_everything(Exception("Op Mode Stopped"))
            stop_signal.greenlight()
        }
        dispatcher.execute()
        telemetry.update()
        dispatcher.finish()
        telemetry.update()
    }

    suspend fun while_live(f: suspend () -> Unit) {
        while (start_signal.is_greenlight() && !stop_signal.is_greenlight()) {
            f()
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