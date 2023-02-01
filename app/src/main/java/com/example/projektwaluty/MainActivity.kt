package com.example.projektwaluty

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

import java.net.URL
import java.util.*
import kotlin.math.sqrt

class MainActivity : AppCompatActivity()
{
    private var pierwszaWaluta = "PLN"
    private var drugaWaluta = "USD"
    private var kursWymiany2 = 0f
    private var kursWymiany1 = 0f


    // do shakowania
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var counter =0


    // do internetu
    private val testint = internetTest();


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)
        menu();
        textChangedStuff();
        linkButtonfun();
        linkButtonMapa();
        //shake
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)!!
            .registerListener(
                sensorListener, sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

    }

    private fun linkButtonMapa()
    {
        val ButtonMapa = findViewById(R.id.ButtonMapa) as Button

            ButtonMapa.setOnClickListener()
            {
                if(testint.isInternetAvailable(applicationContext))
                {
                    startActivity(Intent(this,MapsActivity::class.java))
                }
                else
                {
                    Toast.makeText(applicationContext,"Nie masz polaczenia z internetem",Toast.LENGTH_SHORT).show()
                }
            }



    }

    private fun linkButtonfun()
    {
        val buttonLink = findViewById(R.id.buttonLink) as Button
        buttonLink.setOnClickListener()
            {
                if(testint.isInternetAvailable(applicationContext))
                {
                    val url ="https://www.nbp.pl/home.aspx?f=/kursy/kursya.html"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
                else
                {
                    Toast.makeText(applicationContext,"Nie masz polaczenia z internetem",Toast.LENGTH_SHORT).show()
                }

            }
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener
    {
        override fun onSensorChanged(event: SensorEvent)
        {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta
            val ciekawostkiTab = applicationContext.resources.getStringArray(R.array.ciekawostkiWaluty)
            if (acceleration > 16)
            {
                counter++

            }
            if(counter==10)
            {
                counter=0
                val rand=(ciekawostkiTab.indices).random()
                Log.d("Main","test")
                val url="https://cdn.pixabay.com/download/audio/2021/08/04/audio_bb630cc098.mp3?filename=short-success-sound-glockenspiel-treasure-video-game-6346.mp3"
                var mediaPlayer = MediaPlayer();
                mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
                if(!mediaPlayer!!.isPlaying)
                {
                    try
                    {
                        mediaPlayer!!.setDataSource(url)
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                    }
                    catch(e:IOException)
                    {
                        e.printStackTrace()
                    }
                }
                Toast.makeText(applicationContext, ciekawostkiTab.get(rand), Toast.LENGTH_SHORT)
                    .show()

            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
    override fun onResume()
    {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
        Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        super.onResume()
    }
    override fun onPause()
    {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    fun menu()
    {
        val menu1: Spinner = findViewById(R.id.spinner_firstConversion)
        val menu2: Spinner = findViewById(R.id.spinner_secondConversion)

        ArrayAdapter.createFromResource(
            this,
            R.array.waluty1,
            android.R.layout.simple_spinner_item
        ).also{adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            menu1.adapter=adapter;
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.waluty2,
            android.R.layout.simple_spinner_item
        ).also{adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            menu2.adapter=adapter;
        }
        menu1.onItemSelectedListener=(object: AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            )
            {
                pierwszaWaluta = parent ?.getItemAtPosition(position).toString();
                getAPIwynik()
            }

        })

        menu2.onItemSelectedListener=(object: AdapterView.OnItemSelectedListener
        {
            override fun onNothingSelected(parent: AdapterView<*>?)
            {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            )
            {
                drugaWaluta = parent ?.getItemAtPosition(position).toString();
                getAPIwynik()
            }

        })


    }
    fun textChangedStuff()
    {
        et_firstConversion.addTextChangedListener(object: TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)

            {
                Log.e("Main","Beforech")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("Main","onTextch")
            }

            override fun afterTextChanged(s: Editable?)
            {
                try{
                    getAPIwynik()

                }
                catch(e:Exception)
                {
                    Toast.makeText(applicationContext, "Type a value", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun getAPIwynik()
    {
        if(et_firstConversion!=null && et_firstConversion.text.isNotEmpty() && et_firstConversion.text.isNotBlank()) {
            if (testint.isInternetAvailable(applicationContext) == false)
            {
                Toast.makeText(
                    applicationContext,
                    "Nie masz polaczenia z internetem",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else
            {


            if (drugaWaluta === pierwszaWaluta) {
                Toast.makeText(applicationContext, "Dwie te same waluty", Toast.LENGTH_SHORT).show()
            } else {
                GlobalScope.launch(Dispatchers.IO)
                {
                    try {

                        if (drugaWaluta != "PLN" && pierwszaWaluta == "PLN") {
                            val api2 =
                                "https://api.nbp.pl/api/exchangerates/rates/A/$drugaWaluta/?format=json"
                            val api2Wynik = URL(api2).readText()
                            val api2JsonArray = JSONObject(api2Wynik)
                            var kursWymiany2 = JSONObject(
                                api2JsonArray.getJSONArray("rates").getString(0)
                            ).getString("mid")
                            Log.d("Main", "$kursWymiany2");
                            Log.d("Main", api2Wynik);
                            withContext(Dispatchers.Main)
                            {
                                val text = (et_firstConversion.text.toString()
                                    .toFloat() / kursWymiany2.toFloat()).toString();
                                et_secondConversion?.setText(text);
                            }
                        } else if (pierwszaWaluta != "PLN" && drugaWaluta == "PLN") {
                            val api1 =
                                "https://api.nbp.pl/api/exchangerates/rates/A/$pierwszaWaluta/?format=json"
                            val api1Wynik = URL(api1).readText()
                            val api1JsonArray = JSONObject(api1Wynik)
                            var kursWymiany1 = JSONObject(
                                api1JsonArray.getJSONArray("rates").getString(0)
                            ).getString("mid")
                            Log.d("Main", "$kursWymiany1");
                            Log.d("Main", api1Wynik);
                            withContext(Dispatchers.Main)
                            {
                                val text = (et_firstConversion.text.toString()
                                    .toFloat() * kursWymiany1.toFloat()).toString();
                                et_secondConversion?.setText(text);
                            }
                        } else if (pierwszaWaluta != "PLN" && drugaWaluta != "PLN") {
                            val api1 =
                                "https://api.nbp.pl/api/exchangerates/rates/A/$pierwszaWaluta/?format=json"
                            val api1Wynik = URL(api1).readText()
                            val api1JsonArray = JSONObject(api1Wynik)
                            var kursWymiany1 = JSONObject(
                                api1JsonArray.getJSONArray("rates").getString(0)
                            ).getString("mid")
                            val api2 =
                                "https://api.nbp.pl/api/exchangerates/rates/A/$drugaWaluta/?format=json"
                            val api2Wynik = URL(api2).readText()
                            val api2JsonArray = JSONObject(api2Wynik)
                            var kursWymiany2 = JSONObject(
                                api2JsonArray.getJSONArray("rates").getString(0)
                            ).getString("mid")
                            withContext(Dispatchers.Main)
                            {
                                val text = ((et_firstConversion.text.toString()
                                    .toFloat() * kursWymiany1.toFloat()) / (kursWymiany2.toFloat())).toString();
                                et_secondConversion?.setText(text);
                            }

                        }
                    } catch (e: Exception) {
                        Log.e("Main", "$e")
                    }

                }

            }
        }
        }
    }
}