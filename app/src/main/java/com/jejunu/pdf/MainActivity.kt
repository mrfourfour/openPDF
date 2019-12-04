package com.jejunu.pdf

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.file.Files
import java.nio.file.Paths

class MainActivity : AppCompatActivity() {

    val PERMISSION_REQUEST = 100
    val GETPDF = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()

        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
                .apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }
            startActivityForResult(intent, GETPDF)
        }
    }


    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GETPDF && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
//                var path: String? = uri.path
//                Log.i("me", "Uri:$uri")
//                viewPDF.text = getPath(uri)

                var paths = Paths.get(getPath(uri))
                Log.i("me2",paths.toString())
//                var paths2 = File(getPath(uri))
//                var ioStream = FileInputStream(paths2)
//                var data= byteArrayOf(paths2.length().toByte())
//                ioStream.read(data)
//                var bos = ByteArrayOutputStream()
//                data = bos.toByteArray()
//                viewPDF.text = data.toString()
//                Log.i("me", data.toString())

                var byte2 = Files.readAllBytes(paths)
                Log.i("me2",byte2.contentToString())
//                viewPDF.text = byte2.toString()
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
                    .setNeutralButton("설정") { dialogInterface, i ->
                        val intentSettings =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val uriSettings = Uri.fromParts("package", packageName, null)
                        intentSettings.data = uriSettings
                        startActivity(intentSettings)
                    }
                    .setPositiveButton(
                        "확인"
                    ) { dialogInterface, i -> finish() }
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
        if (requestCode == PERMISSION_REQUEST) {
            for (i in grantResults) {
                // -1일 경우 퍼미션 없음
                if (grantResults[i] < 0) {
                    Toast.makeText(
                        applicationContext,
                        "해당 권한을 활성화 하셔야 합니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    break
                }
            }
        }
    }

    fun getPath(uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                val type = split[0]

                if ("primary".equals(type, false)) {
                    return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                }


            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
                return getDataColumn(this, contentUri, null, null)
            }
            else if(isMediaDocument(uri)){
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":")
                val type = split[0]

                var contentUri:Uri? = null

                if("image".equals(type)){
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }else if("video".equals(type)){
                    contentUri=MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }else if("audio".equals(type)){
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = split[1]
                return getDataColumn(this,contentUri,selection,selectionArgs )
            }

        }
        else if("content".equals(uri.scheme)){
            return getDataColumn(this,uri,null,null)
        }
        else if("file".equals(uri.scheme)){
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: String?
    ): String? {
        var cursor: Cursor? = null
        val column: String = "_data"
        val proj = arrayOf(column)

        try {
            cursor =
                context.contentResolver.query(uri!!, proj, selection.toString(),
                    arrayOf(selectionArgs), null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
            return null
        }
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents".equals(uri.authority)
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents".equals(uri.authority)
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents".equals(uri.authority)
    }

}


