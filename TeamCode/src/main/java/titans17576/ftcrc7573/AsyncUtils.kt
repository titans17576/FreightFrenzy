package titans17576.ftcrc7573

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence
import org.openftc.easyopencv.OpenCvCamera
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Race two coroutines
 */
suspend fun<L: Any, R: Any> race(left: suspend () -> L, right: suspend () -> R, scope: CoroutineScope) : Either<L, R> {
    val channel = Channel<Either<L, R>>(1)
    scope.launch { channel.send(Either.Left(left())) }
    scope.launch { channel.send(Either.Right(right())) }
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

/** Represents a logical event, occurring ideally once */
class Event {
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
    /**
     * Fire the event
     */
    public suspend fun fire() {
        mutex.lock()
        _greenlight()
        mutex.unlock()
    }

    /**
     * Attempt to fire the event without suspending
     */
    public fun try_fire(): Boolean {
        if (!mutex.tryLock()) return false
        _greenlight()
        mutex.unlock()
        return true
    }

    private fun _redlight() {
        result = null
    }

    /**
     * Cancel the event (AKA un-fire the event)
     */
    public suspend fun cancel() {
        mutex.lock()
        _redlight()
        mutex.unlock()
    }

    /**
     * Attempt to cancel the event without suspending
     */
    public fun try_cancel(): Boolean {
        if (!mutex.tryLock()) return false
        _redlight()
        mutex.unlock()
        return true
    }

    private fun _blow_up_everything(reason: Throwable) {
        result = Result.failure(reason)
        resume()
    }

    /**
     * Declare the event invalid
     * @param reason - the throwable that will be thrown if anyone tries to .await the event
     */
    public suspend fun throw_exception(reason: Throwable = Exception("Signal blew up")) {
        mutex.lock()
        _blow_up_everything(reason)
        mutex.unlock()
    }

    /**
     * Try to declare the event invalid without suspending
     */
    public fun try_throw_exception(reason: Throwable = Exception("Signal blew up")): Boolean {
        if (!mutex.tryLock()) return false
        _blow_up_everything(reason)
        mutex.unlock()
        return true
    }

    /**
     * Suspend until this event fires
     */
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

    /**
     * @return whether the event has fired or not
     */
    fun has_fired() : Boolean {
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
    private var event: Event? = null;
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

/**
 * Kotlin wrapper around camera.openCameraDeviceAsync
 */
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

/**
 * Something that works with follow_trajectory_sequence
 */
interface Trajectoryable {
    fun followTrajectorySequenceAsync(trajectorySeq: TrajectorySequence)
    fun isBusy(): Boolean
    fun update()
}

/**
 * Follow a trajectory sequence asynchronously.
 * @param trajectorySeq - the sequence to follow
 * @param drive - The RoadRunner drive to follow on
 * @param op - The AsyncOpMode for a coroutine context
 */
suspend fun follow_trajectory_sequence(trajectorySeq: TrajectorySequence, drive: Trajectoryable, op: AsyncOpMode) {
    drive.followTrajectorySequenceAsync(trajectorySeq)
    while (op.start_event.has_fired() && !op.stop_event.has_fired() && drive.isBusy()) {
        drive.update()
        yield()
    }
}