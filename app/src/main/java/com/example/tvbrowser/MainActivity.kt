package com.example.remoteclient

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.net.ServerSocket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val LISTEN_PORT = 9000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageView = ImageView(this)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setBackgroundColor(android.graphics.Color.BLACK)
        setContentView(imageView)

        startListening()
    }

    private fun startListening() {
        thread {
            val serverSocket = ServerSocket(LISTEN_PORT)
            while (true) {
                try {
                    // Esperamos a que el Xiaomi nos encuentre
                    val socket = serverSocket.accept()
                    val input = DataInputStream(socket.getInputStream())

                    while (socket.isConnected) {
                        val length = input.readInt()
                        if (length > 0) {
                            val buffer = ByteArray(length)
                            input.readFully(buffer)
                            val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
                            runOnUiThread { imageView.setImageBitmap(bitmap) }
                        }
                    }
                } catch (e: Exception) {
                    // Si se corta, el bucle vuelve a accept() y espera que el Xiaomi reconecte
                }
            }
        }
    }
}
