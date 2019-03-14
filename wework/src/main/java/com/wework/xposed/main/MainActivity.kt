package com.wework.xposed.main

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.wework.aidl.IWeWorkEmitListener

import com.wework.xposed.R
import com.wework.xposed.common.bean.Message
import com.wework.xposed.core.*

class MainActivity : AppCompatActivity() {

    private val app = App.instance as App
    lateinit var etLogs: EditText
    lateinit var tvUsername: TextView
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        val txtInput = findViewById<View>(R.id.et_test_msg) as EditText
        tvUsername = findViewById<View>(R.id.current_username) as TextView
        val idInput = findViewById<View>(R.id.et_msg_id) as EditText
        val btnTest = findViewById<View>(R.id.btn_text_send) as Button
        val btnSendAll = findViewById<View>(R.id.btn_send_all) as Button
        val btnGetCAll = findViewById<View>(R.id.btn_get_conversion_list) as Button
        etLogs = findViewById<View>(R.id.et_logs) as EditText


        btnSendAll.setOnClickListener {
            val message = Message()
            message.reciverId = 0
            message.message = txtInput.text.toString()
            Mainer.service?.send(WkClientType.WeWork, WkEvent.WK_CMD_SEND_MESSAGE, JSON.toJSONString(message))
            txtInput.text.clear()

            val intent = Intent()
            intent.action = "com.myapplication.xposed.broadcast.TEST"
            sendBroadcast(intent)
        }


        btnTest.setOnClickListener {
            val message = Message()
            message.reciverId = idInput.text.toString().toLong()
            message.message = txtInput.text.toString()
            Mainer.service?.send(WkClientType.WeWork, WkEvent.WK_CMD_SEND_MESSAGE, JSON.toJSONString(message))
            txtInput.text.clear()
        }
        fab.setOnClickListener { view ->
            val snackbar = Snackbar.make(view, "正在测试服务器链接...", Snackbar.LENGTH_LONG).setAction("Action", null)
            snackbar.show()
            //进行链接
            app.mainer.socker.connect()
            if (Mainer.socker.isConnected) {
                snackbar.setText("服务器已链接")
            }
            //测试发送消息
            snackbar.setText(Mainer.service?.workHandler?.sayHello() ?: "??")
        }
        btnGetCAll.setOnClickListener {
            val message = Message()
            //获取联系人列表
            Mainer.service?.send(WkClientType.WeWork, WkEvent.WK_CMD_GET_CONVERSATION_LIST, JSON.toJSONString(message))
        }
        initEvents()
    }


    override fun onStart() {
        super.onStart()
    }

    private fun initEvents() {
        Mainer.service?.all(WkClientType.Client, object : IWeWorkEmitListener.Stub() {
            override fun callback(senderType: Int, event: String?, data: String?) {
                val str = "$senderType,$event,$data"
                Logger.debug("客户端收到消息", str)
                runOnUiThread {
                    if (event == "UserLogin") {
                        val username = JSON.parseObject(data).getString("realname")
                        tvUsername.text = username
                        return@runOnUiThread
                    }
                    if (counter % 20 == 0) {
                        etLogs.setText("自动清空")
                        //每20次清空一次
                    }
                    etLogs.append(str + "\n")
                    etLogs.setSelection(etLogs.text.length)
                    counter++
                }
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

}