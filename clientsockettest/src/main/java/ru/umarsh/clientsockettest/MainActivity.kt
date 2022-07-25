package ru.umarsh.clientsockettest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {

    lateinit var textResponse: TextView
    lateinit var editTextAddress: EditText
    lateinit var editTextPort: TextView
    lateinit var buttonConnect: Button
    lateinit var buttonClear: Button
    var response = ""
    var socket: Socket? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        buttonClear.setOnClickListener {
            textResponse.text = ""
        }
        buttonConnect.setOnClickListener {
            connectInit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (socket != null) {
            try {
                socket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun connectInit() {
        val address = editTextAddress.text.toString()
        val port = editTextPort.text.toString().toInt()
        val clientSocketThread = Thread(ClientSocket(address, port))
        clientSocketThread.start()
    }

    private fun initView() {
        editTextAddress = findViewById(R.id.address)
        editTextPort = findViewById(R.id.port)
        buttonConnect = findViewById(R.id.connect)
        buttonClear = findViewById(R.id.clear)
        textResponse = findViewById(R.id.response)
    }

    inner class ClientSocket(private val address: String, private val port: Int): Thread() {

        val addressIp = address
        val portIp = port

        override fun run() {
            try {
                socket = Socket(address, port)
                val byteArrayOutputStream = ByteArrayOutputStream(1024)
                val buffer = ByteArray(1024)
                val bytesRead: Int
                val inputStream = socket!!.getInputStream()
                bytesRead = inputStream.read(buffer)
                while (bytesRead != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                    response += byteArrayOutputStream.toString("UTF-8")
                    runOnUiThread {
                        textResponse.text = response
                    }
                }
                Log.d(this.javaClass.name, ">>>> Response: $response")

            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
    }
}