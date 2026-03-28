package com.example.remoteclient

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val XIAOMI_IP = "192.168.100.3" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnTglScreen)?.setOnClickListener { enviar("START_SCREEN") }
        findViewById<Button>(R.id.btnTglBack)?.setOnClickListener { enviar("START_BACK") }
        findViewById<Button>(R.id.btnTglFront)?.setOnClickListener { enviar("START_FRONT") }
        findViewById<Button>(R.id.btnTglAudio)?.setOnClickListener { enviar("START_AUDIO") }

        val imgScreen = findViewById<ImageView>(R.id.viewScreen)
        val imgBack = findViewById<ImageView>(R.id.viewBack)
        
        imgScreen?.let { startListener(9000, it) }
        imgBack?.let { startListener(9002, it) }
    }

    private fun enviar(cmd: String) {
        thread {
            val socket = Socket()
            try {
                socket.connect(InetSocketAddress(XIAOMI_IP, 9001), 2000)
                socket.getOutputStream().write("$cmd\n".toByteArray())
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Sin conexión con Xiaomi", Toast.LENGTH_SHORT).show()
                }
            } finally {
                try { socket.close() } catch (e: Exception) {}
            }
        }
    }

    private fun startListener(port: Int, view: ImageView) {
        thread {
            var server: ServerSocket? = null
            try {
                server = ServerSocket(port)
                while (true) {
                    val sock = server.accept()
                    val input = DataInputStream(sock.getInputStream())
                    try {
                        while (true) {
                            val len = input.readInt()
                            if (len > 0) {
                                val bytes = ByteArray(len)
                                input.readFully(bytes)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                if (bitmap != null) {
                                    runOnUiThread { view.setImageBitmap(bitmap) }
                                }
                            }
                        }
                    } catch (e: Exception) { e.printStackTrace() } finally { sock.close() }
                }
            } catch (e: Exception) { e.printStackTrace() } finally { server?.close() }
        }
    }
}
