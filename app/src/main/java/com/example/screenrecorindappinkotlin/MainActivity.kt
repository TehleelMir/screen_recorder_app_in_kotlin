package com.example.screenrecorindappinkotlin


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import at.markushi.ui.CircleButton
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.hbisoft.hbrecorder.HBRecorder
import com.hbisoft.hbrecorder.HBRecorderListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() , HBRecorderListener {
    lateinit var startButton: CircleButton
    var isRecording = false
    lateinit var recordingObject: HBRecorder
    private val PERMISSION_CODE_FOR_AUDIO = 235
    private val PERMISSION_CODE_FOR_STORAGE = 234
    var audioPermission = true
    lateinit var rootview: RelativeLayout
    lateinit var button2: View
    var flagForAduio = true

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
       // displayAds()
        recordingObject = HBRecorder(this, this)
        setUpButton()
        rootview = findViewById(R.id.rootView)
    }

    private fun displayAds(){
        AudienceNetworkAds.initialize(this)
        val adView = AdView(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50)
        val linearLayout = findViewById<LinearLayout>(R.id.banner_container)
        linearLayout.addView(adView)
        adView.loadAd(adView.buildLoadAdConfig().build())
    }

    private fun setUpButton(){
        startButton = findViewById(R.id.button)
        button2 = findViewById(R.id.button2)

        startButton.setOnClickListener{
            if(isRecording) stopRecording()
            else            startRecording()
        }

        button2.setOnClickListener {
            flagForAduio = if(flagForAduio){
                recordingObject.isAudioEnabled(false)
                false
            }else{
                recordingObject.isAudioEnabled(true)
                true
            }
        }

        button2.setOnLongClickListener {
            startActivity(Intent(this , AboutActivity::class.java))
            Toast.makeText(this , "You found me" , Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun startRecording(){
        if(checkPermission()) {
            rootview.setBackgroundColor(resources.getColor(R.color.backgroundColor2))
            startButton.setColor(resources.getColor(R.color.mainButtonColor2))
            startButton.setImageDrawable((resources.getDrawable(R.drawable.ic_launcher_background)))
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(permissionIntent, 121)
        }
    }

    private fun stopRecording(){
        rootview.setBackgroundColor(resources.getColor(R.color.backgroundColor1))
        startButton.setColor(resources.getColor(R.color.mainButtonColor1))
        recordingObject.stopScreenRecording()
    }

    override fun HBRecorderOnStart() {
        Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
        isRecording = true
        //starts here
    }

    override fun HBRecorderOnComplete() {
        Toast.makeText(this, "Stoping...", Toast.LENGTH_SHORT).show()
        isRecording = false
        //stops here
    }

    override fun HBRecorderOnError(errorCode: Int, reason: String?) {
        Toast.makeText(this, "error= $errorCode and $reason", Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 121 && resultCode == RESULT_OK) {
            setOutputPath()
            recordingObject.startScreenRecording(data, resultCode, this)
        }
    }

    private fun checkPermission(): Boolean{
        return (checkPermission(android.Manifest.permission.RECORD_AUDIO, PERMISSION_CODE_FOR_AUDIO) && checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_CODE_FOR_STORAGE))
    }

    private fun checkPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE_FOR_AUDIO -> {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    audioPermission = false
                    Toast.makeText(this, "You need to give audio record permission", Toast.LENGTH_SHORT).show()
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_CODE_FOR_STORAGE)
            }
            PERMISSION_CODE_FOR_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (audioPermission) startRecording()
                    else Toast.makeText(this, "Audio Permission needed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var resolver: ContentResolver? = null
    var contentValues: ContentValues? = null
    var mUri: Uri? = null
    private fun setOutputPath() {
        val filename: String = generateFileName()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver = contentResolver
            contentValues = ContentValues()
            contentValues!!.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "SR")
            contentValues!!.put(MediaStore.Video.Media.TITLE, filename)
            contentValues!!.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            contentValues!!.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            mUri = resolver!!.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            //FILE NAME SHOULD BE THE SAME
            recordingObject.fileName = filename
            recordingObject.setOutputUri(mUri)
        } else {
            createFolder()
            recordingObject.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/SR")
        }
    }

    private fun createFolder() {
        val f1 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "SR")
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created")
            }
        }
    }

    //Generate a timestamp to be used as a file name
    private fun generateFileName(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate).replace(" ", "")
    }

}