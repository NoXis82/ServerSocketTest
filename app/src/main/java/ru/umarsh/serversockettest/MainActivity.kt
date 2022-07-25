package ru.umarsh.serversockettest

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

class MainActivity : AppCompatActivity() {

    lateinit var info: TextView
    lateinit var infoip: TextView
    lateinit var msg: TextView
    var serverSocket: ServerSocket? = null
    var message = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        infoip.text = getIpAddress()
        val socketServerThread = Thread(SocketServerThread())
        socketServerThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serverSocket != null) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun initView() {
        info = findViewById(R.id.info)
        infoip = findViewById(R.id.infoip)
        msg = findViewById(R.id.msg)

    }

    private fun getIpAddress(): String {
        var ip = ""
        try {
            val enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (enumNetworkInterfaces.hasMoreElements()) {
                val networkInterface = enumNetworkInterfaces.nextElement()
                val enumInetAddress = networkInterface.inetAddresses
                while (enumInetAddress.hasMoreElements()) {
                    val inetAddress = enumInetAddress.nextElement()
                    if (inetAddress.isSiteLocalAddress) {
                        ip += ("SiteLocalAddress: " + inetAddress.hostAddress) + "\n"
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
            ip += """Something Wrong! $e""".trimIndent()
        }
        return ip
    }

    inner class SocketServerThread : Thread() {

        val SocketServerPORT = 8080
        var count = 0

        override fun run() {
            try {
                serverSocket = ServerSocket(SocketServerPORT)
                runOnUiThread {
                    info.text = "I'm waiting here: ${serverSocket?.localPort}"
                }
                while (true) {
                    val socket = serverSocket!!.accept()
                    count++;
                    message += "# $count from ${socket.inetAddress} : ${socket.port} \n"
                    runOnUiThread {
                        msg.text = message
                    }
                    val socketServerReplyThread = SocketServerReplyThread(socket, count)
                    socketServerReplyThread.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class SocketServerReplyThread(private val socket: Socket, private val c: Int) : Thread() {
        val hostThreadSocket: Socket = socket
        val cnt: Int = c

        override fun run() {
            val outputStream: OutputStream
            val msgReply = "Hello from Android, you are # $cnt"
            try {
                outputStream = hostThreadSocket.getOutputStream()
                val printStream = PrintStream(outputStream)
                printStream.print(msgReply)
                printStream.close()
                message += "replayed: $msgReply \n"
                runOnUiThread {
                    msg.text = message
                }
            } catch (e: java.lang.Exception) {
                message += "Something wrong! $e \n"
                e.printStackTrace()
            }
            runOnUiThread {
                msg.text = message
            }
        }
    }

}