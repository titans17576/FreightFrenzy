package titans17576.freightfrenzy.meet2

import com.acmerobotics.dashboard.FtcDashboard
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvCamera
import org.openftc.easyopencv.OpenCvCameraFactory
import org.openftc.easyopencv.OpenCvCameraRotation
import org.openftc.easyopencv.OpenCvPipeline
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.FeedListener
import titans17576.ftcrc7573.FeedSource
import titans17576.ftcrc7573.open_opencv_camera
import java.util.*

enum class Barcode {
    Left,
    Center,
    Right
}

val camera_map: WeakHashMap<AsyncOpMode, OpenCvCamera> = WeakHashMap()

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
    camera.setPipeline(TeamShippingElementPipeline(marker_pos, op))
    FtcDashboard.getInstance().startCameraStream(camera, 10.0)

    camera_map.put(op, camera)
    return marker_pos.fork()
}

suspend fun camera_disable(op: AsyncOpMode) {
    camera_map.get(op)?.stopStreaming()
}

class TeamShippingElementPipeline(barcode: FeedSource<Barcode>, op: AsyncOpMode): OpenCvPipeline() {
    var barcode = barcode
    val op = op

    //var red:Mat = Mat()
    var dst: Mat = Mat()
    var img_copy: Mat = Mat()
    val color: Scalar = Scalar(0.0, 0.0, 255.0)
    val placeholder: Mat = Mat()
    val hsv: Mat = Mat()
    var count7573 = 0

    var maxRect: Rect = Rect()

    override fun processFrame(input: Mat?): Mat {
        //extract the red channel from input
        //Core.extractChannel(input, red, 0)
        //converting rbg to hsv
        Imgproc.cvtColor(input, hsv, Imgproc.COLOR_RGB2HSV)
        //binary threshold
        val min:Double = 150.0
        val max:Double = 255.0
        //Imgproc.threshold(red, dst, min, max, Imgproc.THRESH_BINARY)

        //RED
        //Core.inRange(hsv, Scalar(0.0, 120.0, 70.0), Scalar(10.0, 255.0, 255.0), dst)

        //GREEN
        //tuned to green wheel
        //Core.inRange(hsv, Scalar(40.0, 100.0, 70.0), Scalar(80.0, 255.0, 255.0), dst)

        //tuned to taped green construction paper reflecting strong lights
        Core.inRange(hsv, Scalar(60.0,50.0,20.0), Scalar(90.0,255.0,255.0), dst)

        //finding the contours. p1 = img, p2 = contour retrieval mode, p3 = contour approx method
        val contours:List<MatOfPoint> = java.util.ArrayList()
        Imgproc.findContours(dst, contours, placeholder, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        //drawing contours
        input!!.copyTo(img_copy)
        Imgproc.drawContours(img_copy, contours, -1, color, 2)

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
            c.release() // releasing the buffer of the contour, since after use, it is no longer needed
            copy.release() // releasing the buffer of the copy of the contour, since after use, it is no longer needed
        }

        /**drawing widest bounding rectangle to ret in blue**/
        Imgproc.rectangle(img_copy, maxRect, Scalar(255.0, 0.0, 0.0), 2)

        /** checking if widest width is greater than equal to minimum width
         * using Kotlin if expression (Java ternary) to set height variable
         *
         * height = maxWidth >= MIN_WIDTH ? aspectRatio > BOUND_RATIO ? FOUR : ONE : ZERO
         **/
        val inputWidth = input!!.size().width
        val center_threshold = (inputWidth * 0.01).toInt()
        val right_threshold = (inputWidth * 0.5).toInt()
        val barcode_pos = if (maxRect.width * maxRect.height > input!!.size().height * 0.1)
            when (maxRect.x) {
                in right_threshold..Int.MAX_VALUE -> Barcode.Right
                in center_threshold..right_threshold -> Barcode.Center
                else -> Barcode.Left
            } else Barcode.Left
        op.launch {
            barcode.update(barcode_pos)
        }

        // releasing all mats after use
        hsv.release()
        dst.release()
        placeholder.release()

        op.telemetry.addData("Vision count", count7573)
        count7573++
        op.telemetry.addData("Barcode", barcode_pos)

        return img_copy
    }
}
