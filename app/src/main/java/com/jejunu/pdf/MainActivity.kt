package com.jejunu.pdf

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val READPERMISSIONREQUEST = 100
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
        val permissionCheck = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        when {
            permissionCheck != PackageManager.PERMISSION_GRANTED -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    READPERMISSIONREQUEST
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
            READPERMISSIONREQUEST -> {
                when {
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        granted = true
                    }
                    else -> {
                        granted = false
                    }
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

