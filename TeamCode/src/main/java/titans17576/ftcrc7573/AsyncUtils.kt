package titans17576.ftcrc7573

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.yield
import org.openftc.easyopencv.OpenCvCamera
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend fun<L: Any, R: Any> race(a: suspend () -> L, b: suspend () -> R, scope: CoroutineScope) : Either<L, R> {
    val channel = Channel<Either<L, R>>(1)
    scope.launch { channel.send(Either.Left(a())) }
    scope.launch { channel.send(Either.Right(b())) }
    return channel.receive()
}

/*suspend fun<T> launch_with_timeout(func: suspend CoroutineScope.() -> T, milliseconds: Long, scope: CoroutineScope) : Result<T> {
    val channel = Channel<Boolean>(1)
    val box = Box<T?>(null)
    scope.launch { box.v = func(); channel.send(true) }
    scope.launch { channel.send(false) }
    if (channel.receive()) return Result.success(box.v!!)
    else return Result.failure(TimeoutException())
}*/

class Signal {
    private var result: Result<Unit>? = null
    internal var waiting: ArrayList<Continuation<Unit>> = ArrayList()
    internal val mutex = Mutex()

    private fun resume() {
        val mywaiting = waiting
        waiting = ArrayList()
        for (cont in mywaiting) {
            cont.resumeWith(result!!)
        }
    }
    private fun _greenlight() {
        result = Result.success(Unit)
        resume()
    }
    public suspend fun greenlight() {
        mutex.lock()
        _greenlight()
        mutex.unlock()
    }
    public fun try_greenlight(): Boolean {
        if (!mutex.tryLock()) return false
        _greenlight()
        mutex.unlock()
        return true
    }
    private fun _redlight() {
        result = null
    }
    public suspend fun redlight() {
        mutex.lock()
        _redlight()
        mutex.unlock()
    }
    public fun try_redlight(): Boolean {
        if (!mutex.tryLock()) return false
        _redlight()
        mutex.unlock()
        return true
    }
    private fun _blow_up_everything(reason: Throwable) {
        result = Result.failure(reason)
        resume()
    }
    public suspend fun blow_up_everything(reason: Throwable = Exception("Signal blew up")) {
        mutex.lock()
        _blow_up_everything(reason)
        mutex.unlock()
    }
    public fun try_blow_up_everything(reason: Throwable = Exception("Signal blew up")): Boolean {
        if (!mutex.tryLock()) return false
        _blow_up_everything(reason)
        mutex.unlock()
        return true
    }

    public suspend fun await() {
        mutex.lock()
        if (result == null) {
            suspendCoroutine<Unit> { cont ->
                waiting.add(cont)
                mutex.unlock()
            }
        } else {
            mutex.unlock()
        }
    }
    fun is_greenlight() : Boolean {
        return result != null;
    }
}

class FeedSource<T>(value: T) {
    private var _update_count = 0;
    var update_count: Int = 0
        get() = _update_count
    var value = value;
    val mutex = Mutex()
    internal val waiting = ArrayList<Continuation<Unit>>();

    suspend fun update(new_val: T) {
        this.mutex.lock()
        this._update_count += 1
        this.value = new_val
        for (waiter in this.waiting) waiter.resumeWith(Result.success(Unit))
        this.waiting.clear()
        this.mutex.unlock()
    }

    fun fork() = FeedListener(this)
}

class FeedListener<T>(source: FeedSource<T>) {
    private var last_updated = -1
    private val source = source
    private var signal: Signal? = null;
    var value = source.value;

    public suspend fun get_next(scope: CoroutineScope): T {
        this.source.mutex.lock()
        if (last_updated < source.update_count) {
            this.value = source.value
            this.last_updated = source.update_count
            this.source.mutex.unlock()
            return this.value
        } else {
            suspendCoroutine<Unit> { cont ->
                this.source.waiting.add(cont)
                this.source.mutex.unlock()
            }
            this.source.mutex.lock()
            this.value = source.value
            this.last_updated = source.update_count
            this.source.mutex.unlock()
            return this.value
        }
    }

    public fun get_now() = value
}

suspend fun open_opencv_camera(camera: OpenCvCamera) {
    suspendCoroutine<Unit> {
        camera.openCameraDeviceAsync(object : OpenCvCamera.AsyncCameraOpenListener {
            override fun onOpened() {
                it.resumeWith(Result.success(Unit))
            }

            override fun onError(errorCode: Int) {
                it.resumeWithException(RuntimeException("Died of camera init code " + errorCode))
            }
        })
    }
}

/*interface Trajectoryable {
    fun followTrajectorySequenceAsync(trajectorySeq: TrajectorySequence)
    fun isBusy(): Boolean
    fun update()
}
suspend fun follow_trajectory(trajectorySeq: TrajectorySequence, drive: Trajectoryable, op: AsyncOpMode) {
    drive.followTrajectorySequenceAsync(trajectorySeq)
    while (op.start_signal.is_greenlight() && !op.stop_signal.is_greenlight() && drive.isBusy()) {
        drive.update()
        yield()
    }
}
 */