package com.example.knockknock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    private lateinit var uriServer: String //to config de uri of server

    private lateinit var stateText: TextView
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var configEditText: EditText

    private lateinit var openButton: Button
    private lateinit var closeButton: Button
    private lateinit var setButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)//Investigate how use  setContent -Jetpack Compose

        //Find the views by their Id's
        stateText = findViewById(R.id.stateTextView)
        configEditText = findViewById(R.id.configEditText)
        openButton = findViewById(R.id.openbutton)
        closeButton = findViewById(R.id.closebutton)
        setButton = findViewById(R.id.setbutton)

        uriServer = "http://192.168.1.3/uri"

        //Set up click listener for the button
        openButton.setOnClickListener {
            //Create a coroutine scope using the Dispatchers.IO dispatcher, which is
            //suitable for network operations.
            //launch, launches a new coroutine that executes the code within the curly
            //braces.
            CoroutineScope(Dispatchers.IO).launch {//Launch in background
                try {
                    fetchDataFromApi(uriServer,"?door=1&try=3") //door=1=Open, try=3= try tree time
                } catch (e: Exception){
                    //Handle exceptions
                    println("ERROR create instance HttpClient!!!")
                    println(e.message)
                }
            }
        }

        //Set up click listener for the button
        closeButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {//Launch in background
                fetchDataFromApi(uriServer,"?door=0&try=3") //door=0=Close, try=3= try tree time
            }
        }

        setButton.setOnClickListener {
            uriServer = configEditText.text.toString()
            println(uriServer)
        }

    }

    private fun updateStatus(newStatus: String) {
        mainHandler.post {
            //Update the variable Status here(safe for UI thread)
            stateText.text = newStatus
        }
    }
    private suspend fun fetchDataFromApi(uri: String, action: String){
        val client = HttpClient(CIO) //client is represented by the HttpClient class
        val url = uri + action //TODO: action= ?door=[1/0]&try=3  where 1=Open 0=Close

        //get action in text
        var viewAction: String = ""
        val actionCode = action[6]
        viewAction = if(actionCode == '1'){
            "Open"
        } else{
            "Close"
        }

        try{
            //Use the HttpClient.get() method to make GET request. A response will be
            //received as a HttpResponse class object.
            //get method need the main suspend, so Make main suspend
            val response: HttpResponse = client.get(url)
            if(response.status.value == 200)
            {
                println(response.status)//to print a status code returned by the server
                val responseBytes = response.call.response.readBytes()
                val textContent = String(responseBytes, Charset.forName("UTF-8")) //charset uses UTF-8 encoding

                val newStatusResp = viewAction + textContent
                updateStatus(newStatusResp) //Send UI update to main thread
            }
            else{
                println(response.status)//to print a status code returned by the server
            }
        } catch (e: Exception){
            //Handle exceptions
            println("ERROR GET function!!!")
            println(e.message)
        } finally {
            client.close()//to close the stream and release any resources associated with it.
        }

    }
}