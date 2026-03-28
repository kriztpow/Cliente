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
    // CAMBIA ESTA IP POR LA QUE TENGA TU XIAOMI EN ESE MOMENTO
    private val XIAOMI_IP = "192.168.100.3" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configuración de botones con protección anti-cierre
        findViewById<Button>(R.id.btnTglScreen)?.setOnClickListener { enviar("START_SCREEN") }
        findViewById<Button>(R.id.btnTglBack)?.setOnClickListener { enviar("START_BACK") }
        findViewById<Button>(R.id.btnTglFront)?.setOnClickListener { enviar("START_FRONT") }
        findViewById<Button>(R.id.btnTglAudio)?.setOnClickListener { enviar("START_AUDIO") }

        // Iniciar receptores
        val imgScreen = findViewById<ImageView>(R.id.viewScreen)
        val imgBack = findViewById<ImageView>(R.id.viewBack)
        
        if (imgScreen != null) startListener(9000, imgScreen)
        if (imgBack != null) startListener(9002, imgBack)
    }

    private fun enviar(cmd: String) {
        thread { 
            try { 
                Socket(XIAOMI_IP, 9001).use { it.getOutputStream().write("$cmd\n".toByteArray()) } 
            } catch(e: Exception) { e.printStackTrace() } 
        }
    }

    private fun startListener(port: Int, view: ImageView) {
        thread {
            try {
                val server = ServerSocket(port)
                while (true) {
                    val sock = server.accept()
                    val input = DataInputStream(sock.getInputStream())
                    while (true) {
                        val len = input.readInt()
                        val bytes = ByteArray(len).apply { input.readFully(this) }
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { 
                            runOnUiThread { view.setImageBitmap(it) }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
