package com.example.remoteclient

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val XIAOMI_IP = "192.168.100.3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Botones de mando
        findViewById<Button>(R.id.btnTglScreen).setOnClickListener { enviar("START_SCREEN") }
        findViewById<Button>(R.id.btnTglBack).setOnClickListener { enviar("START_BACK") }
        findViewById<Button>(R.id.btnTglFront).setOnClickListener { enviar("START_FRONT") }
        findViewById<Button>(R.id.btnTglAudio).setOnClickListener { enviar("START_AUDIO") }

        // Hilos de escucha
        startListener(9000, findViewById(R.id.viewScreen)) // Puerto Pantalla
        startListener(9002, findViewById(R.id.viewBack))   // Puerto Cámaras (compartido)
    }

    private fun enviar(cmd: String) {
        thread { try { Socket(XIAOMI_IP, 9001).use { it.getOutputStream().write("$cmd\n".toByteArray()) } } catch(e:Exception){} }
    }

    private fun startListener(port: Int, view: ImageView) {
        thread {
            val server = ServerSocket(port)
            while (true) {
                try {
                    val sock = server.accept()
                    val input = DataInputStream(sock.getInputStream())
                    while (true) {
                        val len = input.readInt()
                        val bytes = ByteArray(len).apply { input.readFully(this) }
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { 
                            runOnUiThread { view.setImageBitmap(it) }
                        }
                    }
                } catch (e: Exception) {}
            }
        }
    }
}
