package com.example.smsdemoapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Telephony
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var button: Button

    private var messages = mutableListOf<SMS>()
    private val adapter = SmsAdapter()
    private lateinit var rvSMS: RecyclerView

    private val requestMultiplePermissionLauncher = registerForActivityResult(
        RequestMultiplePermissions()
    ) { resultsMap ->
        val isGranted = resultsMap.entries.all { it.value }
        if (isGranted) {
            receiveMsg()
        } else {
            Toast.makeText(this, "access denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissionsAndReadSms()
        button = findViewById(R.id.button)

        val rvSMS = findViewById<RecyclerView>(R.id.rvSms)
        rvSMS.layoutManager = LinearLayoutManager(this)

        button.setOnClickListener {
            readSMS()
            adapter.smsList = messages
            rvSMS.adapter = adapter
        }

    }

    @SuppressLint("Recycle")
    private fun readSMS() {
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
        cursor?.moveToFirst()
        messages.clear()
        var counter = 15
        while (cursor?.isAfterLast == false && counter > 0) {
            val date =  cursor.getColumnIndex("date")
            val smsBody = cursor.getColumnIndex("body")
            messages.add(
                SMS(
                    date = getDate(cursor.getString(date).toLong()),
                    message = cursor.getString(smsBody),
                )
            )
            cursor.moveToNext()
            counter--
        }
        cursor?.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            receiveMsg()
        }
    }

    private fun receiveMsg() {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (Telephony.Sms.Intents.getMessagesFromIntent(intent).isNotEmpty()) {
                        Toast.makeText(this@MainActivity, "New message", Toast.LENGTH_SHORT).show()
                        val timer = object : CountDownTimer(1000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                            }

                            override fun onFinish() {
                                readSMS()
                            }
                        }
                        timer.start()
                    }
                }
            }
        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(millis:Long) : String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val calendar = Calendar.getInstance();
        calendar.timeInMillis = millis
        return formatter.format(calendar.time)
    }

    private fun checkPermissionsAndReadSms() {
        if (permissionReceiveSmsGranted() && permissionReadSmsGranted()) {
            receiveMsg()
        } else {
            requestMultiplePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                )
            )
        }
    }

    private fun permissionReceiveSmsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

    private fun permissionReadSmsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

}




