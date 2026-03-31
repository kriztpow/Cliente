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
        // 1. Cargamos el diseño XML
        setContentView(R.layout.activity_main)

        // 2. Mantenemos la pantalla siempre encendida (como en tu versión anterior)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 3. Configuración de botones (con protección contra nulos)
        findViewById<Button>(R.id.btnTglScreen)?.setOnClickListener { enviar("START_SCREEN") }
        findViewById<Button>(R.id.btnTglBack)?.setOnClickListener { enviar("START_BACK") }
        findViewById<Button>(R.id.btnTglFront)?.setOnClickListener { enviar("START_FRONT") }
        findViewById<Button>(R.id.btnTglAudio)?.setOnClickListener { enviar("START_AUDIO") }

        val imgScreen = findViewById<ImageView>(R.id.viewScreen)
        val imgBack = findViewById<ImageView>(R.id.viewBack)
        
        // 4. Iniciamos la escucha en hilos separados (Evita el cierre al abrir)
        thread { imgScreen?.let { startListener(9000, it) } }
        thread { imgBack?.let { startListener(9002, it) } }
    }

    private fun enviar(cmd: String) {
        thread {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(XIAOMI_IP, 9001), 2000)
                val writer = socket.getOutputStream().bufferedWriter()
                writer.write(cmd)
                writer.newLine()
                writer.flush()
                socket.close()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Xiaomi no disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startListener(port: Int, view: ImageView) {
        try {
            val serverSocket = ServerSocket(port)
            while (true) {
                try {
                    val socket = serverSocket.accept()
                    socket.tcpNoDelay = true // Como en tu versión anterior para reducir lag
                    val input = DataInputStream(socket.getInputStream())
                    
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
                    e.printStackTrace() // Si se desconecta, vuelve al inicio del bucle
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
