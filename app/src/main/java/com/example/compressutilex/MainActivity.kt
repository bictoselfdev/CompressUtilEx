package com.example.compressutilex

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.compressutilex.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val testDirectoryPath = "/sdcard/Download/test"
    private val testFilePath1 = "/sdcard/Download/test.txt"
    private val testFilePath2 = "/sdcard/Download/test/test.txt"

    private val testZipFilePath = "/sdcard/Download/test.zip"
    private val testTarGzipFilePath = "/sdcard/Download/test.tar.gz"

    private val downloadPath = "/sdcard/Download/"

    private val targetPath = "/sdcard/Download/test"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        requestPermission()

        binding.btnCreateTestFiles.setOnClickListener {
            createTestFiles()
        }

        binding.btnZip.setOnClickListener {
            val files = arrayOf(File(testDirectoryPath), File(testFilePath1))
            CompressUtil.zip(files, targetPath)
        }

        binding.btnUnZip.setOnClickListener {
            val file = File(testZipFilePath)
            CompressUtil.unzip(file, downloadPath)
        }

        binding.btnTarGzip.setOnClickListener {
            val files = arrayOf(File(testDirectoryPath), File(testFilePath1))
            CompressUtil.tarGzip(files, targetPath)
        }

        binding.btnTarGunzip.setOnClickListener {
            val file = File(testTarGzipFilePath)
            CompressUtil.tarGunzip(file, downloadPath)
        }
    }

    private fun createTestFiles() {
        val testDirectory = File(testDirectoryPath)
        if (!testDirectory.exists()) testDirectory.mkdirs()
        val testFile1 = File(testFilePath1)
        if (!testFile1.exists()) testFile1.createNewFile()
        val testFile2 = File(testFilePath2)
        if (!testFile2.exists()) testFile2.createNewFile()

        Toast.makeText(this, "Create testFiles", Toast.LENGTH_SHORT).show()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    startActivityForResult(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        addCategory("android.intent.category.DEFAULT")
                        data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    }, 100)
                } catch (e: Exception) {
                    startActivityForResult(Intent().apply {
                        action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    }, 100)
                }
            }
        } else {
            if (checkSelfPermission(permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        permission.READ_EXTERNAL_STORAGE,
                        permission.WRITE_EXTERNAL_STORAGE
                    ), 200
                )
            }
        }
    }
}