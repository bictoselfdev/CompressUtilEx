package com.example.compressutilex

import android.util.Log
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


object CompressUtil {
    private const val TAG = "CompressUtil"

    private const val EXTENSION_ZIP = ".zip"
    private const val EXTENSION_TAR = ".tar"
    private const val EXTENSION_TAR_GZ = ".tar.gz"

    private const val BUFFER_SIZE = 1024

    /************************************************
     * zip related functions
     ************************************************/
    fun zip(files: Array<File>, destPath: String) {
        try {
            val zipArchiveOutputStream = ZipArchiveOutputStream(FileOutputStream(destPath + EXTENSION_ZIP))
            for (file in files) {
                if (!file.exists()) {
                    Log.e(TAG, "[zip] file is not exists : ${file.name}")
                    continue
                }
                Log.d(TAG, "[zip] file : ${file.name}")

                if (file.isDirectory) {
                    file.listFiles()?.let {
                        zipDirectory(zipArchiveOutputStream, it, "/${file.name}/")
                    }
                } else {
                    val fileInputStream = FileInputStream(file)
                    val entry = ZipArchiveEntry(file.name).apply { 
                        size = file.length()
                    }

                    zipArchiveOutputStream.putArchiveEntry(entry)
                    fileInputStream.copyTo(zipArchiveOutputStream, BUFFER_SIZE)

                    zipArchiveOutputStream.closeArchiveEntry()
                    fileInputStream.close()
                }
            }
            zipArchiveOutputStream.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun zipDirectory(zipArchiveOutputStream: ZipArchiveOutputStream, files: Array<File>, dirPath: String) {
        try {
            for (file in files) {
                Log.d(TAG, "[zipDirectory] file : $dirPath${file.name}")

                if (file.isDirectory) {
                    file.listFiles()?.let {
                        zipDirectory(zipArchiveOutputStream, it, "$dirPath${file.name}/")
                    }
                } else {
                    val fileInputStream = FileInputStream(file)
                    val entry = ZipArchiveEntry(dirPath + file.name).apply { 
                        size = file.length()
                    }

                    zipArchiveOutputStream.putArchiveEntry(entry)
                    fileInputStream.copyTo(zipArchiveOutputStream, BUFFER_SIZE)

                    zipArchiveOutputStream.closeArchiveEntry()
                    fileInputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unzip(zipFile: File, destPath: String) {
        if (!zipFile.exists() && !zipFile.name.endsWith(EXTENSION_ZIP)) {
            Log.e(TAG, "[unzip] The file does not exist or is not a zip file. : ${zipFile.name}")
            return
        }

        try {
            val zipArchiveInputStream = ZipArchiveInputStream(FileInputStream(zipFile))

            while (true) {
                val entry = zipArchiveInputStream.nextZipEntry ?: break
                if (!zipArchiveInputStream.canReadEntryData(entry)) {
                    Log.e(TAG, "[unzip] The entry is not readable : ${entry.name}")
                    continue
                }
                Log.d(TAG, "[unzip] entry : ${entry.name}")

                if (entry.name.lastIndexOf("/") > 0) {
                    val file = File("$destPath/${entry.name.substring(0, entry.name.lastIndexOf("/"))}")
                    if (!file.exists()) file.mkdirs()
                }

                if (!entry.isDirectory) {
                    val fileOutputStream = FileOutputStream("$destPath/${entry.name}")
                    zipArchiveInputStream.copyTo(fileOutputStream, BUFFER_SIZE)
                    fileOutputStream.close()
                }
            }
            zipArchiveInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /************************************************
     * tar, gzip related functions
     ************************************************/

    fun tarGzip(files: Array<File>, destPath: String) {
        val tarFile = tar(files, destPath)
        if (tarFile.exists()) {
            gzip(tarFile, destPath)
            tarFile.delete()
        }
    }

    private fun tar(files: Array<File>, destPath: String): File {
        try {
            val tarArchiveOutputStream = TarArchiveOutputStream(FileOutputStream(destPath + EXTENSION_TAR))

            for (file in files) {
                if (!file.exists()) {
                    Log.e(TAG, "[tar] file is not exists : ${file.name}")
                    continue
                }
                Log.d(TAG, "[tar] file : ${file.name}")

                if (file.isDirectory) {
                    file.listFiles()?.let {
                        tarDirectory(tarArchiveOutputStream, it, "/${file.name}/")
                    }
                } else {
                    val fileInputStream = FileInputStream(file)
                    val entry = TarArchiveEntry(file.name).apply {
                        size = file.length()
                    }

                    tarArchiveOutputStream.putArchiveEntry(entry)
                    fileInputStream.copyTo(tarArchiveOutputStream, BUFFER_SIZE)

                    tarArchiveOutputStream.closeArchiveEntry()
                    fileInputStream.close()
                }
            }
            tarArchiveOutputStream.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return File(destPath + EXTENSION_TAR)
    }

    private fun tarDirectory(tarArchiveOutputStream: TarArchiveOutputStream, files: Array<File>, dirPath: String) {
        try {
            for (file in files) {
                Log.d(TAG, "[tarDirectory] file : $dirPath${file.name}")

                if (file.isDirectory) {
                    file.listFiles()?.let { tarDirectory(tarArchiveOutputStream, it, "$dirPath${file.name}/") }
                } else {
                    val fileInputStream = FileInputStream(file)
                    val entry = TarArchiveEntry(dirPath + file.name).apply {
                        size = file.length()
                    }

                    tarArchiveOutputStream.putArchiveEntry(entry)
                    fileInputStream.copyTo(tarArchiveOutputStream, BUFFER_SIZE)

                    tarArchiveOutputStream.closeArchiveEntry()
                    fileInputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun gzip(tarFile: File, destPath: String) {
        val gzipCompressorOutputStream = GzipCompressorOutputStream(FileOutputStream(destPath + EXTENSION_TAR_GZ))
        val fileInputStream = FileInputStream(tarFile)
        fileInputStream.copyTo(gzipCompressorOutputStream, BUFFER_SIZE)
        fileInputStream.close()
        gzipCompressorOutputStream.finish()
    }

    fun tarGunzip(tarGzFile: File, destPath: String) {
        if (!tarGzFile.exists() && !tarGzFile.name.endsWith(EXTENSION_TAR_GZ)) {
            Log.e(TAG, "[unTarGzip] The file does not exist or is not a zip file. : ${tarGzFile.name}")
            return
        }

        try {
            val gzipCompressorInputStream = GzipCompressorInputStream(FileInputStream(tarGzFile))
            val tarArchiveInputStream = TarArchiveInputStream(gzipCompressorInputStream)

            while (true) {
                val entry = tarArchiveInputStream.nextTarEntry ?: break
                if (!tarArchiveInputStream.canReadEntryData(entry)) {
                    Log.e(TAG, "[unTarGzip] The entry is not readable : ${entry.name}")
                    continue
                }
                Log.d(TAG, "[unTarGzip] entry : ${entry.name}")

                if (entry.name.lastIndexOf("/") > 0) {
                    val file = File("$destPath/${entry.name.substring(0, entry.name.lastIndexOf("/"))}")
                    if (!file.exists()) file.mkdirs()
                }

                if (!entry.isDirectory) {
                    val fileOutputStream = FileOutputStream("$destPath/${entry.name}")
                    tarArchiveInputStream.copyTo(fileOutputStream, BUFFER_SIZE)
                    fileOutputStream.close()
                }
            }
            tarArchiveInputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}