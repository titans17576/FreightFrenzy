package titans17576.freightfrenzy.meet2

import com.acmerobotics.dashboard.FtcDashboard
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import org.openftc.easyopencv.OpenCvPipeline
import titans17576.ftcrc7573.*
import java.util.*

enum class Barcode {
    Left,
    Center,
    Right
}

private class CameraDat(val camera: OpenCvCamera, val pipeline: TeamShippingElementPipeline)
private val camera_map: WeakHashMap<AsyncOpMode, CameraDat> = WeakHashMap()

suspend fun camera_init(op: AsyncOpMode): FeedListener<Barcode> {
    val marker_pos: FeedSource<Barcode> = FeedSource(Barcode.Left)
    val cameraMonitorViewId = op.hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id", op.hardwareMap.appContext.packageName)

    //naming webcam
    val webcam: WebcamName = op.hardwareMap.get("camera") as WebcamName
    //linking webcam with a camera object
    val camera: OpenCvCamera = OpenCvCameraFactory.getInstance().createWebcam(webcam)
    //open camera
    open_opencv_camera(camera)
    //set resolution and start streaming
    camera.startStreaming(640,480, OpenCvCameraRotation.UPRIGHT)
    //use GPU acceleration for faster render time
    camera.setViewportRenderer(OpenCvCamera.ViewportRenderer.GPU_ACCELERATED)
    val pipeline = TeamShippingElementPipeline(marker_pos, op)
    camera.setPipeline(pipeline)
    FtcDashboard.getInstance().startCameraStream(camera, 10.0)

    camera_map.put(op, CameraDat(camera, pipeline))
    return marker_pos.fork()
}

suspend fun camera_disable(op: AsyncOpMode) {
    val dat = camera_map.get(op)
    dat?.camera?.stopStreaming()
    dat?.pipeline?.release()
}

class TeamShippingElementPipeline(barcode: FeedSource<Barcode>, op: AsyncOpMode): OpenCvPipeline() {
    var barcode = barcode
    val op = op

    //var red:Mat = Mat()
    var dst: Mat = Mat()
    var img_copy: Mat = Mat()
    val contour_color: Scalar = Scalar(0.0, 0.0, 255.0)
    val placeholder: Mat = Mat()
    val hsv: Mat = Mat()
    var count7573 = 0

    var maxRect: Rect = Rect()

    override fun processFrame(input: Mat?): Mat {
        //extract the red channel from input
        //Core.extractChannel(input, red, 0)
        //converting rbg to hsv
        Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV)

        //tuned to taped green construction paper reflecting strong lights
        Core.inRange(hsv, Scalar(55.75,100.0,75.0), Scalar(100.0,255.0,255.0), dst)

        //finding the contours. p1 = img, p2 = contour retrieval mode, p3 = contour approx method
        val contours:List<MatOfPoint> = java.util.ArrayList()
        Imgproc.findContours(dst, contours, placeholder, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        //finding contour of actual team shipping element
        //src: https://github.com/FTCLib/FTCLib/blob/master/core/vision/src/main/java/com/arcrobotics/ftclib/vision/UGContourRingPipeline.kt
        //finding widths of each contour, comparing, and storing the widest
        var maxWidth = 0
        for (c: MatOfPoint in contours) {
            val copy = MatOfPoint2f(*c.toArray())
            val rect: Rect = Imgproc.boundingRect(copy)

            val w = rect.width
            if (w > maxWidth) {
                maxWidth = w
                maxRect = rect
            }

            //copy.release() // releasing the buffer of the copy of the contour, since after use, it is no longer needed
        }

        /**drawing widest bounding rectangle to ret in blue**/

        /** checking if widest width is greater than equal to minimum width
         * using Kotlin if expression (Java ternary) to set height variable
         *
         * height = maxWidth >= MIN_WIDTH ? aspectRatio > BOUND_RATIO ? FOUR : ONE : ZERO
         **/
        val inputWidth = input!!.size().width
        val center_threshold = (inputWidth * 0.01).toInt()
        val right_threshold = (inputWidth * 0.375).toInt()
        val is_valid_detection = maxRect.width > input!!.size().width * 0.12
        val barcode_pos = if (is_valid_detection)
            when (maxRect.x) {
                in right_threshold..Int.MAX_VALUE -> Barcode.Right
                in center_threshold..right_threshold -> Barcode.Center
                else -> Barcode.Left
            } else Barcode.Left
        op.launch {
            barcode.update(barcode_pos)
        }

        //drawing contours
        input!!.copyTo(img_copy)
        val area_color: Scalar = Scalar(255.0, 0.0, 0.0)
        Imgproc.line(img_copy, Point(right_threshold.toDouble(), 0.0), Point(right_threshold.toDouble(), input!!.size().height), area_color, 4)
        Imgproc.line(img_copy, Point(right_threshold.toDouble(), 0.0), Point(right_threshold.toDouble(), input!!.size().height), area_color, 4)
        try { Imgproc.drawContours(img_copy, contours, -1, contour_color, 2) }
        catch (e: Exception) { }
        Imgproc.rectangle(img_copy, maxRect, if (is_valid_detection) { Scalar(0.0, 255.0, 0.0) } else { Scalar(255.0, 0.0, 0.0) }, 4)

        // releasing all mats after use
        for (c: MatOfPoint in contours) {
            c.release() // releasing the buffer of the contour, since after use, it is no longer needed
        }
        hsv.release()
        dst.release()
        placeholder.release()

        op.telemetry.addData("Vision count", count7573)
        count7573++
        op.telemetry.addData("Barcode", barcode_pos)

        return img_copy
    }

    fun release() {
    }
}
