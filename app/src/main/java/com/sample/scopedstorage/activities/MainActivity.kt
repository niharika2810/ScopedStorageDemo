package com.sample.scopedstorage.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import com.sample.scopedstorage.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import kotlin.random.Random

/*
 The sample code is for Android 10 and above, Handle the below version as your were doing earlier
*/
class MainActivity : AppCompatActivity() {

    companion object {
        private const val OPEN_FILE_REQUEST_CODE = 1
        private const val OPEN_FOLDER_REQUEST_CODE = 2
        private const val MEDIA_LOCATION_PERMISSION_REQUEST_CODE = 3
        private const val CHOOSE_FILE = 4
        private const val PERMISSION_READ_EXTERNAL_STORAGE = 5
        var downloadImageUrl = "https://cdn.pixabay.com/photo/2020/04/21/06/41/bulldog-5071407_1280.jpg"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_create_file.setOnClickListener {
            startActivity(Intent(this@MainActivity, FileActivity::class.java))
        }

        btn_open_file.setOnClickListener {
            openFile()
        }

        btn_download_file.setOnClickListener {
            downloadFile()
        }
        open_folder.setOnClickListener {
            openFolder()
        }
        download_image_media_location.setOnClickListener {
            fetchMediaLocation()
        }

        download_image_external.setOnClickListener {
            downloadImage()
        }
        download_image_internal.setOnClickListener {
            downloadImageToAppFolder()
        }
    }

    private fun downloadImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {


            if (checkPermissionForReadWrite(this)) {
                downloadImageToDownloadFolder()
            } else {
                requestPermissionForReadWrite(this)
            }


        } else {
            downloadImageToDownloadFolder()
        }
    }

    private fun downloadImageToAppFolder() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkPermissionForReadWrite(this)) {
                downloadToInternalFolder()
            } else {
                requestPermissionForReadWrite(this)
            }

        } else {
            downloadToInternalFolder()
        }
    }

    //Downloading file to Internal Folder
    private fun downloadToInternalFolder() {
        try {
            val file = File(
                this.getExternalFilesDir(
                    null
                ), "SampleImageDemo.png"
            )

            if (!file.exists())
                file.createNewFile()

            var fileOutputStream: FileOutputStream? = null

            fileOutputStream = FileOutputStream(file)
            val bitmap = (image.drawable as BitmapDrawable).bitmap

            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            Toast.makeText(
                applicationContext,
                "Download successfully to " + file.absolutePath,
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Check if you already have read storage permission
    private fun checkPermissionForReadWrite(context: Context): Boolean {
        val result: Int =
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )

        return result == PackageManager.PERMISSION_GRANTED
    }

    //Request Permission For Read Storage
    private fun requestPermissionForReadWrite(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ), PERMISSION_READ_EXTERNAL_STORAGE
        )
    }

    private fun downloadImageToDownloadFolder() {
        val mgr = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(downloadImageUrl)
        val request = DownloadManager.Request(
            downloadUri
        )
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
        )
            .setAllowedOverRoaming(false).setTitle("Sample")
            .setDescription("Sample Image Demo New")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "SampleImage.jpg"
            )

        Toast.makeText(
            applicationContext,
            "Download successfully to ${downloadUri?.path}",
            Toast.LENGTH_LONG
        ).show()

        mgr.enqueue(request)

    }

    //Function for Image check on
    private fun fetchMediaLocation() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {

            openChooser()

        } else {

            if (isPermissionGrantedForMediaLocationAccess(this)) {

                openChooser()
            } else {
                Log.i("Tag", "else chooseFile")

                requestPermissionForAccessMediaLocation(this)
            }

        }
    }

//Request Permission if not given

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestPermissionForAccessMediaLocation(context: Context) {
        Log.i("Tag", "requestPermissionForAML")

        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(android.Manifest.permission.ACCESS_MEDIA_LOCATION),
            MEDIA_LOCATION_PERMISSION_REQUEST_CODE
        )

    }

    fun openChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHOOSE_FILE)
    }

    //Check if Permission granted for Accessing Media Location
    private fun isPermissionGrantedForMediaLocationAccess(context: Context): Boolean {
        Log.i("Tag", "checkPermissionForAML")
        val result: Int =
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun openFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_FOLDER_REQUEST_CODE)
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            //if you want to open PDF file
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            //Adding Read URI permission
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivityForResult(intent, OPEN_FILE_REQUEST_CODE)
    }

    @SuppressLint("NewApi")
    private fun downloadFile() {
        // create a new document
        val document = PdfDocument()
        // crate a page description
        val pageInfo = PdfDocument.PageInfo.Builder(400, 300, 1).create()
        // start a page
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        canvas.drawText("HelloWorld", 80F, 50F, paint)
        // finish the page
        document.finishPage(page)

        //Make IS_PENDING 1 so that it is not visible to other apps till the time this is downloaded
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "demofile_" + Random.nextInt(9999) + ".pdf")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = contentResolver

        //Storing at primary location
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        //Insert the item
        val item = resolver.insert(collection, values)


        if (item != null) {
            resolver.openOutputStream(item).use { out ->
                document.writeTo(out);
            }
        }
        values.clear()

        //Make it 0 when downloaded
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        item?.let { resolver.update(it, values, null, null) }

        Toast.makeText(
            applicationContext,
            "Download successfully to ${item?.path}",
            Toast.LENGTH_LONG
        ).show()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_FILE_REQUEST_CODE) {
                data?.data?.also { documentUri ->

                    //Permission needed if you want to retain access even after reboot
                    contentResolver.takePersistableUriPermission(
                        documentUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Toast.makeText(this, documentUri.path.toString(), Toast.LENGTH_LONG).show()
                }
            } else if (requestCode == OPEN_FOLDER_REQUEST_CODE) {
                val directoryUri = data?.data ?: return

                //Taking permission to retain access
                contentResolver.takePersistableUriPermission(
                    directoryUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                //Now you have access to the folder, you can easily view the content or do whatever you want.
                val documentsTree = DocumentFile.fromTreeUri(application, directoryUri) ?: return
                val childDocuments = documentsTree.listFiles().asList()
                Toast.makeText(
                    this,
                    "Total Items Under this folder =" + childDocuments.size.toString(),
                    Toast.LENGTH_LONG
                ).show()

            } else if (requestCode == CHOOSE_FILE) {
                if (data != null) {
                    var inputStream: InputStream? = null
                    //Not guaranteed to get the metadata
                    try {

                        inputStream = contentResolver.openInputStream(data.data!!)
                        val exifInterface = ExifInterface(inputStream!!)

                        Toast.makeText(
                            this,
                            "Path = " + data.data + "   ,Latitude = " + exifInterface.getAttribute(
                                ExifInterface.TAG_GPS_LATITUDE
                            ) + "   ,Longitude =" + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: IOException) {
                        // Handle any errors
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close()
                            } catch (ignored: IOException) {
                            }

                        }
                    }
                }
            }
        }
    }
}