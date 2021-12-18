package titans17576.freightfrenzy.meet2

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
    camera.startStreaming(640,480, OpenCvCameraRotation.SIDEWAYS_LEFT)
    //use GPU acceleration for faster render time
    camera.setViewportRenderer(OpenCvCamera.ViewportRenderer.GPU_ACCELERATED)
    camera.setPipeline(TeamShippingElementPipeline(marker_pos, op))

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
    var ret: Mat = Mat()

    var maxRect: Rect = Rect()
    val rectWidth
        get() = maxRect.size().width
    val rectHeight
        get() = maxRect.size().height
    val rectSize
        get() = maxRect.size()

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
        img_copy = input!!.clone()
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
        Imgproc.rectangle(ret, maxRect, Scalar(0.0, 0.0, 255.0), 2)

        /** checking if widest width is greater than equal to minimum width
         * using Kotlin if expression (Java ternary) to set height variable
         *
         * height = maxWidth >= MIN_WIDTH ? aspectRatio > BOUND_RATIO ? FOUR : ONE : ZERO
         **/
        val inputWidth = input.size().width
        op.launch {
            val barcode_pos: Barcode
            val third_width = inputWidth / 3
            val left_third_coord = 0 + third_width
            val center_third_coord = left_third_coord + third_width

            /** checks if aspectRatio is greater than BOUND_RATIO
             * to determine whether stack is ONE or FOUR
             */
            if (maxRect.x < left_third_coord)
                barcode_pos = Barcode.Left // height variable is now FOUR
            else if (maxRect.x < center_third_coord)
                barcode_pos = Barcode.Center // height variable is now ONE
            else
                barcode_pos = Barcode.Right // height variable is now ZERO
            barcode.update(barcode_pos)
        }

        // releasing all mats after use
        hsv.release()
        dst.release()
        placeholder.release()

        return img_copy
    }
}
