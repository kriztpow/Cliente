package com.example.remoteclient

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.DataInputStream
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    
    // CAMBIA ESTO POR LA IP QUE APAREZCA EN TU XIAOMI
    private val SERVER_IP = "192.168.1.15" 
    private val SERVER_PORT = 8080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        imageView = findViewById(R.id.remoteScreen)

        // Iniciamos el hilo de conexión persistente
        startReception()
    }

    private fun startReception() {
        thread {
            while (true) {
                try {
                    val socket = Socket(SERVER_IP, SERVER_PORT)
                    val inputStream = DataInputStream(socket.getInputStream())

                    // Si llega aquí, la conexión es exitosa
                    runOnUiThread { 
                        Toast.makeText(this, "Conectado al Xiaomi", Toast.LENGTH_SHORT).show() 
                    }

                    while (socket.isConnected) {
                        // 1. Leer tamaño del frame (4 bytes)
                        val length = inputStream.readInt()
                        if (length > 0) {
                            // 2. Leer los bytes del JPEG
                            val buffer = ByteArray(length)
                            inputStream.readFully(buffer)

                            // 3. Decodificar y mostrar
                            val bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
                            runOnUiThread {
                                imageView.setImageBitmap(bitmap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Si el Xiaomi entra en reposo profundo o te alejas del Wi-Fi, cae aquí
                    e.printStackTrace()
                    runOnUiThread { 
                        // Opcional: limpiar pantalla o poner mensaje de "Reconectando..."
                    }
                    // Esperar 2 segundos antes de intentar reconectar solo
                    Thread.sleep(2000)
                }
            }
        }
    }
}
