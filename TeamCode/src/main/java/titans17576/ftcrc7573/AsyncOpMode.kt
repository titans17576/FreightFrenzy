package titans17576.ftcrc7573

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import kotlinx.coroutines.*
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

var RUNNING_OP: AsyncOpMode? = null;

public abstract class AsyncOpMode : OpMode() {
    abstract suspend fun op_mode()
    lateinit var start_signal: Signal;
    lateinit var stop_signal: Signal;

    private lateinit var dispatcher: AsyncOpModeDispatcher;
    private lateinit var async_scope: CoroutineScope;
    fun launch(task: suspend CoroutineScope.() -> Unit): Job {
        return async_scope.launch(block = task)
    }
    fun<R> async(task: suspend CoroutineScope.() -> R): Deferred<R> {
        return async_scope.async(block = task)
    }


    override fun init() {
        dispatcher = AsyncOpModeDispatcher()
        async_scope = CoroutineScope(EmptyCoroutineContext + dispatcher)
        start_signal = Signal()
        stop_signal = Signal()
        RUNNING_OP = this
        launch { op_mode() }
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

private class AsyncOpModeDispatcher : CoroutineDispatcher() {
    val dispatches = LinkedBlockingQueue<Runnable>()
    var error: Throwable? = null
    //val dispatches_unimportant = LinkedList<Runnable>()

    fun execute() {
        if (error != null) {
            dispatches.clear()
            throw error!!
        }

        //println("Begin dispatcher exec")
        val size = dispatches.size
        for (i in 0..size) {
            try {
                dispatches.poll()?.run()
            } catch(e: Throwable) {
                dispatches.clear()
                error = e
                System.out.println("==========================")
                System.out.println(e)
                System.out.println("==========================")
                throw e
            }
        }
        //println("End dispatcher exec")
    }
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
    /*@InternalCoroutinesApi
    override fun dispatchYield(context: CoroutineContext, block: Runnable) {
        dispatches_unimportant.add(block)
    }*/
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
fun register_defered_async_opmode(name: String, is_autonomous: Boolean, type: KClass<DeferredAsyncOpMode>) {
    class DeferredAsyncOpModeHolder : AsyncOpMode() {
        override suspend fun op_mode() {
            type.constructors.iterator().next().call(this).op_mode()
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