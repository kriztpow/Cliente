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
    // CAMBIA ESTO A LA IP QUE TENGA TU XIAOMI
    private val XIAOMI_IP = "192.168.100.3" 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Botones con hilos de seguridad para que no se bloquee la interfaz
        findViewById<Button>(R.id.btnTglScreen)?.setOnClickListener { enviar("START_SCREEN") }
        findViewById<Button>(R.id.btnTglBack)?.setOnClickListener { enviar("START_BACK") }
        findViewById<Button>(R.id.btnTglFront)?.setOnClickListener { enviar("START_FRONT") }
        findViewById<Button>(R.id.btnTglAudio)?.setOnClickListener { enviar("START_AUDIO") }

        val imgScreen = findViewById<ImageView>(R.id.viewScreen)
        val imgBack = findViewById<ImageView>(R.id.viewBack)
        
        // RECTIFICACIÓN CRÍTICA: Los listeners ahora se inician 
        // totalmente aislados del hilo principal.
        thread {
            imgScreen?.let { startListener(9000, it) }
        }
        thread {
            imgBack?.let { startListener(9002, it) }
        }
    }

    private fun enviar(cmd: String) {
        thread {
            var socket: Socket? = null
            try {
                socket = Socket()
                socket.connect(InetSocketAddress(XIAOMI_IP, 9001), 2000)
                socket.getOutputStream().write("$cmd\n".toByteArray())
                socket.getOutputStream().flush()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Xiaomi no responde", Toast.LENGTH_SHORT).show()
                }
            } finally {
                try { socket?.close() } catch (e: Exception) {}
            }
        }
    }

    private fun startListener(port: Int, view: ImageView) {
        var server: ServerSocket? = null
        try {
            server = ServerSocket(port)
            while (true) {
                val sock = server.accept()
                thread {
                    try {
                        val input = DataInputStream(sock.getInputStream())
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try { sock.close() } catch (e: Exception) {}
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { server?.close() } catch (e: Exception) {}
        }
    }
}
