package com.example.sigma

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sigma.databinding.ActivityMainBinding
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.graphics.get
import androidx.core.graphics.set
import kotlin.math.roundToInt
import androidx.annotation.ColorInt


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setupViews()
        setContentView(binding.root)
    }

    private fun setupViews() {
        with(binding) {
            loadBtn.setOnClickListener {
                progressBar.progress = 0
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE)
            }
            saveBtn.setOnClickListener {
                val titlePhoto = "InvertedPhoto"
                val resultBitmap = (imageView.drawable as BitmapDrawable).bitmap.copy(
                    Bitmap.Config.RGB_565,
                    true
                )
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    resultBitmap,
                    titlePhoto,
                    "Image of $titlePhoto"
                )
                Toast.makeText(
                    applicationContext,
                    getString(R.string.savedPhotoText),
                    Toast.LENGTH_SHORT
                ).show()
            }
            asyncBtn.setOnClickListener {
                if (imageView.drawable != null) {
                    val task = MyAsyncTask()
                    task.execute((imageView.drawable as BitmapDrawable).bitmap)
                }
            }
            handlerBtn.setOnClickListener {
                if (imageView.drawable != null) {
                    handlerProcess()
                }
            }

        }
    }

    private fun handlerProcess() {
        val mainThreadHandler = Handler(Looper.getMainLooper())
        val bgThread = LooperThread().apply { start() }
        mainThreadHandler.post {
            bgThread.handler?.post {
                with(binding) {
                    layoutBtn.visibility = View.INVISIBLE

                    val result =
                        (imageView.drawable as BitmapDrawable).bitmap.copy(
                            Bitmap.Config.RGB_565,
                            true
                        )
                    val maxWidth = result.width
                    for (i in 0 until result.width) {
                        if (i % 200 == 0) progressBar.progress =
                            (((i * 1.0 / maxWidth) * 100).roundToInt())
                        for (j in 0 until result.height) {
                            val color = result[i, j]
                            @ColorInt val resColor = invertColor(color)
                            result[i, j] = resColor
                        }
                    }
                    imageView.setImageBitmap(result)
                    runOnUiThread {
                        layoutBtn.visibility = View.VISIBLE
                        progressBar.progress = 100
                    }

                }
            }
        }

    }

    private fun invertColor(color: Int): Int {
        return color xor 0x00ffffff
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            with(binding) {
                imageView.setImageURI(data?.data)
                textView.text =
                    (imageView.drawable as BitmapDrawable).bitmap.width.toString() + " x " + (imageView.drawable as BitmapDrawable).bitmap.height.toString()
            }
        }
    }

    inner class MyAsyncTask : AsyncTask<Bitmap, Int, Bitmap>() {

        override fun doInBackground(vararg inputBitmap: Bitmap): Bitmap {
            val result = inputBitmap[0].copy(Bitmap.Config.RGB_565, true)
            val maxWidth = result.width
            for (i in 0 until result.width) {
                if (i % 200 == 0) publishProgress(((i * 1.0 / maxWidth) * 100).roundToInt())
                for (j in 0 until result.height) {
                    val color = result[i, j]
                    @ColorInt val resColor = invertColor(color)
                    result[i, j] = resColor
                }
            }
            return result
        }


        override fun onPreExecute() {
            super.onPreExecute()
            binding.layoutBtn.visibility = View.INVISIBLE
        }

        override fun onProgressUpdate(vararg values: Int?) {
            values[0]?.let { binding.progressBar.setProgress(it, false) }
        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
            with(binding) {
                imageView.setImageBitmap(result)
                layoutBtn.visibility = View.VISIBLE
                progressBar.progress = 100
            }
        }
    }
}