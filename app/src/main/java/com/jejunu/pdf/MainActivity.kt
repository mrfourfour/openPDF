package com.jejunu.pdf

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    val rootPath = File(Environment.getExternalStorageDirectory().absolutePath)
    var filename: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/*"
            startActivityForResult(intent, 10)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            10 -> {
                var path: String? = data?.data?.toString()
                Log.i("me", path)
                viewPDF.setText(path)
                openPdf(path)
            }
        }
    }


    fun openPdf(contentsPath: String?) {
        var pdfFile = File(contentsPath)
        Log.i("me", contentsPath)
        var path = Uri.fromFile(pdfFile)
        if (pdfFile.exists()) {

        }
        try {
            var intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                var contentUri =
                    FileProvider.getUriForFile(applicationContext,"com.jejunu.pdf",pdfFile )
                intent.setDataAndType(contentUri, "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                startActivity(intent)

            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "PDF 파일을 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show()
        }
//        else {
//            Toast.makeText(this, "PDF 파일이 없습니다.", Toast.LENGTH_SHORT).show()
    }

}
