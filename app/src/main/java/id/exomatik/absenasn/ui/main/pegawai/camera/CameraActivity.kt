@file:Suppress("DEPRECATION")

package id.exomatik.absenasn.ui.main.pegawai.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.hardware.Camera.ShutterCallback
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import id.exomatik.absenasn.R
import id.exomatik.absenasn.ui.main.MainActivity
import id.exomatik.absenasn.ui.main.pegawai.kirimAbsen.KirimAbsenActivity
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.getTimeStamp
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var savedData: DataSave
    private lateinit var camera1: Camera
    private lateinit var surfaceView: SurfaceView
    private lateinit var surfaceHolder: SurfaceHolder
    private var previewing = false
    private var modeCamera = 1
    private var modeFlash = false
    private var jenisAbsensi : String? = ""
    private var idHari : String? = ""
    private var idAbsensi : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_camera)
        savedData = DataSave(this)
        idHari = intent.getStringExtra(Constant.idHari)
        idAbsensi = intent.getStringExtra(Constant.idAbsen)
        jenisAbsensi = intent.getStringExtra(Constant.jenisAbsen)

        initCamera()
        framePreview.addView(surfaceView)

        fabFoto.setOnClickListener {
            camera1.takePicture(myShutterCallback, pictureCallbackRAW, pictureCallbackBMP)
        }

        fabRotate.setOnClickListener {
            modeCamera = if (modeCamera == 1){
                0
            } else{
                1
            }
            camera1.stopPreview()
            camera1.release()
            previewing = false
            showCamera()
        }

        fabFlash.setOnClickListener {
            try{
                if (hasFlash()){
                    val params = camera1.parameters
                    if (!modeFlash){
                        fabFlash.setImageResource(R.drawable.ic_flash_off_black)
                        params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                        modeFlash = true
                    }
                    else{
                        fabFlash.setImageResource(R.drawable.ic_flash_on_black)
                        params.flashMode = Camera.Parameters.FLASH_MODE_OFF
                        modeFlash = false
                    }
                    camera1.parameters = params
                }
                else{
                    Toast.makeText(
                        this,
                        "Error, Device Anda tidak support menggunakan blits",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception){
                Toast.makeText(
                    this,
                    "Error, Device Anda tidak support menggunakan blits",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: RuntimeException){
                Toast.makeText(
                    this,
                    "Error, Device Anda tidak support menggunakan blits",
                    Toast.LENGTH_LONG
                ).show()
            }
            camera1.startPreview()
        }
    }

    private fun hasFlash(): Boolean {
        val parameters = try { camera1.parameters }
        catch (ignored: RuntimeException) {
            return false
        }

        if (parameters.flashMode == null) {
            return false
        }

        val supportedFlashModes = parameters.supportedFlashModes
        return !(supportedFlashModes == null || supportedFlashModes.isEmpty() ||
                supportedFlashModes.size == 1 && supportedFlashModes[0] == Camera.Parameters.FLASH_MODE_OFF)
    }

    private fun initCamera(){
        window?.setFormat(PixelFormat.TRANSPARENT)
        surfaceView = SurfaceView(this)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        showCamera()
    }

    private fun showCamera(){
        if (!previewing) {
            camera1 = Camera.open(modeCamera)
            try {
                camera1.setDisplayOrientation(90)
                camera1.setPreviewDisplay(surfaceHolder)
                camera1.enableShutterSound(true)
                camera1.startPreview()
                previewing = true
            } catch (e: IOException) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private var myShutterCallback = ShutterCallback {
    }

    private var pictureCallbackRAW = PictureCallback { _, _ -> }

    @SuppressLint("SimpleDateFormat")
    private fun saveImage(finalBitmap: Bitmap) {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fname = "${savedData.getDataUser()?.username}__${getTimeStamp()}.jpg"
        val file = File(storageDir, fname)
        if (file.exists()) file.delete()

        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()

            val imagePath = Uri.fromFile(file)

            val intent = Intent(this, KirimAbsenActivity::class.java)
            intent.putExtra(Constant.idHari, idHari)
            intent.putExtra(Constant.idAbsen, idAbsensi)
            intent.putExtra(Constant.reffFotoUser2, imagePath)
            intent.putExtra(Constant.jenisAbsen, jenisAbsensi)
            startActivity(intent)
            finish()

            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var pictureCallbackBMP = PictureCallback { dataBitmap, _ ->
        val bitmapPicture = BitmapFactory.decodeByteArray(dataBitmap, 0, dataBitmap.size)
        val matrix = Matrix()
        if (modeCamera == 1){
            matrix.postRotate("270".toFloat())
        } else{
            matrix.postRotate("90".toFloat())
        }

        val correctBmp = Bitmap.createBitmap(
            bitmapPicture,
            0,
            0,
            bitmapPicture.width,
            bitmapPicture.height,
            matrix,
            true
        )

        saveImage(correctBmp)
    }

    override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        if (previewing) {
            camera1.stopPreview()
            previewing = false
        }

        try {
            camera1.setPreviewDisplay(surfaceHolder)
            camera1.startPreview()
            previewing = true
        } catch (e: IOException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera1.stopPreview()
        camera1.release()
        previewing = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
