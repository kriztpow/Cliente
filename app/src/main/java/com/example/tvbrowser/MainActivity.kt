package com.example.remoteclient

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private val LISTEN_PORT = 9000
    private val XIAOMI_IP = "192.168.100.3" // IP fija de tu Xiaomi
    private val XIAOMI_CONTROL_PORT = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias del XML
        imageView = findViewById(R.id.remoteScreen)
        val btnStart = findViewById<Button>(R.id.btnStartRemote)
        val btnStop = findViewById<Button>(R.id.btnStopRemote)

        // Configuración de botones remotos
        btnStart.setOnClickListener { 
            enviarComando("START") 
        }
        
        btnStop.setOnClickListener { 
            enviarComando("STOP") 
        }

        // Inicia la escucha del flujo de video
        startListening()
    }

    // Envía la orden al Xiaomi por el puerto 9001
    private fun enviarComando(comando: String) {
        thread {
            try {
                val socket = Socket(XIAOMI_IP, XIAOMI_CONTROL_PORT)
                val writer = socket.getOutputStream().bufferedWriter()
                writer.write(comando)
                writer.newLine()
                writer.flush()
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startListening() {
        thread {
            try {
                // Escucha en todas las interfaces de red (0.0.0.0)
                val serverSocket = ServerSocket(LISTEN_PORT, 50, InetAddress.getByName("0.0.0.0"))
                
                while (true) {
                    try {
                        // Espera la conexión del Xiaomi (.3) en el puerto 9000
                        val socket = serverSocket.accept()
                        socket.tcpNoDelay = true 
                        
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
                        // Si hay error de red o desconexión, vuelve al bucle para esperar de nuevo
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
