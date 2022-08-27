package com.home.automation

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.home.automation.BluetoothControlActivity.Companion.bluetoothSocket
import com.home.automation.databinding.ActivitySerialMonitorBinding
import com.home.automation.utils.longShowToast
import java.io.IOException
import java.io.InputStream

class SerialMonitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySerialMonitorBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_serial_monitor)
        if (bluetoothSocket != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                ReadInput(bluetoothSocket!!, binding)
            }, 1000)
        } else {
            longShowToast("Something Went Wrong!")
            finish()
        }
        binding.btnSendMes.setOnClickListener {
            if (binding.edSendMessage.text.toString().isNotEmpty()) {
                sendSignal(
                    binding.edSendMessage.text.toString() + "*"
                )
            } else {
                binding.edSendMessage.error = "Message is Required!"
                longShowToast("Message is Required!")
            }
        }
        binding.txtReceive.movementMethod = ScrollingMovementMethod()

        binding.btnClearInput.setOnClickListener { binding.txtReceive.text = "" }
    }


    private class ReadInput(
        private val bluetoothSocket: BluetoothSocket,
        private val binding: ActivitySerialMonitorBinding,
    ) : Runnable {
        private var bStop = false
        private val t: Thread = Thread(this, "Input Thread")
        val isRunning: Boolean
            get() = t.isAlive

        override fun run() {
            val inputStream: InputStream
            try {
                inputStream = bluetoothSocket.inputStream
                while (!bStop) {
                    val buffer = ByteArray(256)
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer)
//                        var i = 0
//                        while (i < buffer.size && !buffer[i].equals(0)) {
//                            i++
//                        }
                        val strInput = String(buffer, 0, buffer.size)
                        if (binding.chkReceiveText.isChecked) {
                            binding.txtReceive.post {
                                binding.txtReceive.append(strInput)
//                                val txtLength: Int = binding.txtReceive.editableText.length
//                                if (txtLength > 50000) {
//                                    binding.txtReceive.editableText.delete(0, txtLength - 50000)
//                                }
                                if (binding.chkScroll.isChecked) { // Scroll only if this is checked
                                    binding.viewScroll.post { binding.viewScroll.fullScroll(View.FOCUS_DOWN) }
                                }
                            }
                        }
                    }
                    Thread.sleep(500)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        fun stop() {
            bStop = true
        }

        init {
            t.start()
        }
    }


    private fun sendSignal(number: String) {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.outputStream.write(number.toByteArray())
            } catch (e: IOException) {
                longShowToast("Something Went Wrong!")
            }
        }
    }
}