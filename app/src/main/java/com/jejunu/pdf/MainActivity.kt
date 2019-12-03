package com.jejunu.pdf

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val PERMISSION_REQUEST = 100
    val GETPDF = 999
    var granted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }
            startActivityForResult(intent, GETPDF)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GETPDF && resultCode == Activity.RESULT_OK) {
            data?.data?.path.also { uri ->
                Log.i("me", "Uri:$uri")
                viewPDF.text = uri.toString()
                Log.i("me", getPathFromUri(data?.data!!))
            }
        }
    }


    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야합니다.")
                    .setNeutralButton("설정", DialogInterface.OnClickListener { dialogInterface, i ->
                        val intentSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uriSettings = Uri.fromParts("package", packageName, null)
                        intentSettings.data = uriSettings
                        startActivity(intentSettings)
                    })
                    .setPositiveButton(
                        "확인",
                        DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                    .setCancelable(false)
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE
                    ), PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST ->
                for (i in grantResults) {
                    // -1일 경우 퍼미션 없음
                    if (grantResults[i] < 0) {
                        Toast.makeText(this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show()
                        return
                    }
                }

        }
    }

    fun getPathFromUri(uri: Uri): String {
        var proj = arrayOf(MediaStore.Files.FileColumns.DATA)
        var cursor = contentResolver.query(uri, proj, null, null, null)
        cursor?.moveToNext()
        var path: String = cursor!!.getString(cursor.getColumnIndex("_data"))
        var uri2 = Uri.fromFile(File(path))
        Log.i("me", uri2.toString())
        cursor.close()
        return path
    }

}

