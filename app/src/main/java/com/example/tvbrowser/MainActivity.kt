package com.example.remoteclient

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val LISTEN_PORT = 9000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuración de la interfaz
        imageView = ImageView(this)
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setBackgroundColor(android.graphics.Color.BLACK)
        // Mantiene la pantalla encendida mientras la app esté abierta
        imageView.keepScreenOn = true 
        setContentView(imageView)

        startListening()
    }

    private fun startListening() {
        thread {
            try {
                // Escucha en todas las interfaces de red (0.0.0.0)
                val serverSocket = ServerSocket(LISTEN_PORT, 50, InetAddress.getByName("0.0.0.0"))
                
                while (true) {
                    try {
                        // Espera la conexión del Xiaomi (.3)
                        val socket = serverSocket.accept()
                        socket.tcpNoDelay = true // Reduce la latencia
                        
                        val input = DataInputStream(socket.getInputStream())
                        
                        while (!socket.isClosed && socket.isConnected) {
                            val length = input.readInt()
                            if (length > 0) {
                                val buffer = ByteArray(length)
                                input.readFully(buffer)
                                
                                val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
                                if (bitmap != null) {
                                    runOnUiThread {
                                        imageView.setImageBitmap(bitmap)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Si el Xiaomi se desconecta o bloquea, vuelve a esperar conexión
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
