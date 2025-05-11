package com.ticojch.myapplication

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private val REQUEST_CODE_AUDIO = 1001
    private lateinit var btn_Play: ImageButton
    private lateinit var add_music: ImageButton
    private var isPlay = false
    private lateinit var nombre: TextView
    private lateinit var barraCancion: SeekBar
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var duracion: TextView
    private lateinit var btn_anterior: ImageButton
    private lateinit var btn_siguiente: ImageButton
    private lateinit var timeCancion: TextView
    private lateinit var timeProgreso: TextView
    private val canciones  = mutableListOf<Any>(
        R.raw.darylhall_maneater,
        R.raw.meetmehalfway_blackeyedpeas
    )
    private val nombreCanciones = mutableListOf(
        "Maneater - Dary Hall",
        "Meet me halfway - Black Eyed Peas"
    )
    private var cancion = 0
    private var isUsuarioChanging = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setLayouts()

        reproducirCancion(canciones[cancion])
        barraCancion.max = mediaPlayer.duration
        timeCancion.text = formatTime(mediaPlayer.duration)
        nombre.text = nombreCanciones[cancion]

        //Estados de la barra de reproduccion
        barraCancion.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUsuarioChanging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUsuarioChanging = false
                mediaPlayer.seekTo(seekBar?.progress ?: 0)
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    timeProgreso.text = formatTime(progress)
                }
            }
        })

        //Controlador de la barra de progreso
        handler.post(object : Runnable {
            override fun run() {
                if (!isUsuarioChanging) {
                    val current = mediaPlayer.currentPosition
                    barraCancion.progress = current
                    timeProgreso.text = formatTime(current)
                }
                handler.postDelayed(this, 500)
            }
        })

        add_music.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
            }
            startActivityForResult(intent, REQUEST_CODE_AUDIO)
        }


        //Reproducis-pausar cancion
        btn_Play.setOnClickListener{
            playCancion()
        }

        //Siguiente cancion
        btn_siguiente.setOnClickListener {
            siguienteCancion()
        }

        //anterior cancion
        btn_anterior.setOnClickListener {
            anteriorCancion()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_AUDIO && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    // Detener y liberar la canción anterior
                    mediaPlayer.stop()
                    mediaPlayer.release()

                    // Agregar a la lista
                    canciones.add(uri)
                    nombreCanciones.add(obtenerNombreArchivo(uri))
                    cancion = canciones.lastIndex

                    // Reproducir la nueva canción
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(this@MainActivity, uri)
                        prepare()
                        start()
                    }

                    isPlay = true
                    btn_Play.setImageResource(R.drawable.pause_music)

                    barraCancion.max = mediaPlayer.duration
                    timeCancion.text = formatTime(mediaPlayer.duration)
                    timeProgreso.text = formatTime(0)
                    barraCancion.progress = 0
                    nombre.text = nombreCanciones[cancion]

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }



    fun setLayouts(){
        this.btn_Play = findViewById<ImageButton>(R.id.playButton)
        this.btn_siguiente = findViewById<ImageButton>(R.id.siguienteMusic)
        this.btn_anterior = findViewById<ImageButton>(R.id.anteriorMusic)
        this.add_music = findViewById<ImageButton>(R.id.addMusic)
        this.barraCancion = findViewById<SeekBar>(R.id.seekBar)
        this.nombre = findViewById<TextView>(R.id.music_name)
        this.timeCancion = findViewById<TextView>(R.id.timeTotal)
        this.timeProgreso = findViewById<TextView>(R.id.timeInicio)
    }

    fun playCancion(){
        if (!isPlay) {
            mediaPlayer.start()
            isPlay = true
            btn_Play.setImageResource(R.drawable.pause_music)
        }else{
            mediaPlayer.pause()
            isPlay = false
            btn_Play.setImageResource(R.drawable.play_music)
        }
    }

    fun siguienteCancion(){
        // Libera el anterior
        mediaPlayer.stop()
        mediaPlayer.release()

        // Siguiente canción (cíclica)
        cancion = (cancion + 1) % canciones.size

        // Cargar nueva canción
        reproducirCancion(canciones[cancion])
    }

    fun anteriorCancion(){
        mediaPlayer.stop()
        mediaPlayer.release()

        // Siguiente canción (cíclica)
        if(cancion==0){
            cancion = canciones.size-1
        }else{
            cancion = cancion-1
        }

        // Cargar nueva canción
        reproducirCancion(canciones[cancion])
    }

    fun obtenerNombreArchivo(uri: android.net.Uri): String {
        var nombre = "Canción personalizada"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nombreIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nombreIndex != -1) {
                nombre = it.getString(nombreIndex)
            }
        }
        return nombre
    }
    fun reproducirCancion(item: Any) {
        mediaPlayer = MediaPlayer()
        try {
            if (item is Int) {
                mediaPlayer = MediaPlayer.create(this, item)
            } else if (item is Uri) {
                mediaPlayer.setDataSource(this, item)
                mediaPlayer.prepare()
            }

            if (isPlay) mediaPlayer.start()

            barraCancion.max = mediaPlayer.duration
            barraCancion.progress = 0
            timeCancion.text = formatTime(mediaPlayer.duration)
            timeProgreso.text = formatTime(0)
            nombre.text = nombreCanciones[cancion]

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    fun formatTime(ms: Int): String {
        val minutos = (ms / 1000) / 60
        val segundos = (ms / 1000) % 60
        return String.format("%02d:%02d", minutos, segundos)
    }

}