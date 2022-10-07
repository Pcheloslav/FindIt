package com.example.readernfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity(){

    private var tvReceive: TextView? = null

    //Nfc adapter default value
    private var nfcAdapter: NfcAdapter? = null

    //String from nfc message
    private var nfcString: String? = null
    private var but1: Button? = null
    private var but2: Button? = null
    private var but3: Button? = null
    private var but4: Button? = null
    private var but5: Button? = null
    private var butSearch: Button? = null
    private var butLocate: Button? = null
    private var butMenu: Button? = null
    private var imperson: ImageView? = null
    private var impointer: ImageView? = null
    private var botBut: ImageView? = null



    private var maze: Maze? = null
    private var width: Int = 0
    private var height: Int = 0

    private var scaleX: Float = 0f
    private var scaleY: Float = 0f

    private fun getScreenSize(){
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels
        scaleX = maze!!.w.toFloat()/ width
        scaleY = maze!!.h.toFloat() / height
    }

    // Load icons from resources into dictionary
    // Load icons from resources into dictionary
    private fun loadIcons(): Map<String, Bitmap> {

        // Map name of icon -> resource id
        val idMap = mapOf(
            "chair" to R.drawable.chair,
            "table" to R.drawable.table,
            "armchair_top" to R.drawable.armchair_top,
            "armchair_left" to R.drawable.armchair_left,
            "armchair_right" to R.drawable.armchair_right,
            "table_large" to R.drawable.table_large,
            "table_large_2" to R.drawable.table_large_2,
            "table_large_2_pc_left" to R.drawable.table_large_2_pc_left,
            "table_large_2_pc_right" to R.drawable.table_large_2_pc_right,
            "desk_row" to R.drawable.desk_row,
            "stall" to R.drawable.stall,
            "department" to R.drawable.department
        )

        // Map name of icon -> bitmap
        val imgMap = mutableMapOf<String, Bitmap>()
        idMap.forEach { imgMap[it.key] = BitmapFactory.decodeResource(resources, it.value) }
        return imgMap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        this.tvReceive = findViewById(R.id.textReceiveNFC) //Match with text view
        this.but1 = findViewById(R.id.button3)
        this.but2 = findViewById(R.id.button4)
        this.but3 = findViewById(R.id.button)
        this.but4 = findViewById(R.id.button2)
        this.but5 = findViewById(R.id.button5)
        this.but1 = findViewById(R.id.button3)
        this.imperson = findViewById(R.id.personimage)
        this.impointer = findViewById(R.id.pointerimage)
        this.butSearch = findViewById(R.id.butsearch)
        this.butLocate = findViewById(R.id.butlocate)
        this.butMenu = findViewById(R.id.butmenu)
        this.botBut = findViewById(R.id.bottombuttons)
        botBut?.x = 0F
        botBut?.y = 1935F
        butSearch?.y = 1955F
        butSearch?.x = 100F
        butLocate?.y = 1955F
        butLocate?.x = 450F
        butMenu?.y = 1955F
        butMenu?.x = 750F



        but1?.y = getString(R.string.mark1y).toFloat()
        but1?.x = getString(R.string.mark1x).toFloat()
        but2?.y = getString(R.string.mark2y).toFloat()
        but2?.x = getString(R.string.mark2x).toFloat()
        but3?.y = getString(R.string.mark3y).toFloat()
        but3?.x = getString(R.string.mark3x).toFloat()
        but4?.y = getString(R.string.mark4y).toFloat()
        but4?.x = getString(R.string.mark4x).toFloat()
        but5?.y = getString(R.string.mark5y).toFloat()
        but5?.x = getString(R.string.mark5x).toFloat()





        but1?.setOnClickListener {
            pointClick(but1!!)
        }
        but2?.setOnClickListener {
            pointClick(but2!!)
        }

        but3?.setOnClickListener {
            pointClick(but3!!)
        }
        but4?.setOnClickListener {
            pointClick(but4!!)
        }
        but5?.setOnClickListener {
            pointClick(but5!!)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //if no nfc device
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        //if nfc is not enabled
        if (nfcAdapter?.isEnabled == false) {
            Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT).show()
        }

        //need this (not sure)
        if (intent != null) {
            nfcString = receiveMessage(intent)
        }
        mazeInit()
        getScreenSize()

    }

    private fun pointClick(button: Button) {
        val coordinate: Pair<Float, Float> = parseCoordinates(nfcString!!)
        val bx = (coordinate.first * scaleX).toInt()
        val by = (coordinate.second * scaleY).toInt()
        val start = (button.x * scaleX).toInt()
        val end = (button.y * scaleY).toInt()
        val path = maze?.findPath(
            Pair(bx, by),
            Pair(start, end)
        )
        val imgView = findViewById<ImageView>(R.id.imageView)
        imgView?.setImageBitmap(maze?.drawPath(path!!))
        impointer?.x = button.x-40
        impointer?.y = button.y-60
        impointer?.isVisible = true
        impointer?.z = 10f
    }


    private fun mazeInit() {
        val json = assets.open("210-4.json").bufferedReader().use {
            it.readText()
        }
        val icons = loadIcons()
        maze = Maze(json, 1080F, 2139F, icons)



    }


    // get intent and go to receiveMessage
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcString = receiveMessage(intent)
        if (nfcString != null) {

            val imgView = findViewById<ImageView>(R.id.imageView)
            val coord: Pair<Float, Float> = parseCoordinates(nfcString!!)
            imgView.setImageBitmap(maze?.drawMaze())

            imperson?.x = coord.first-30
            imperson?.y = coord.second-50
            imperson?.isVisible = true
            imperson?.z = 10f
            impointer?.isVisible = false
            butSearch?.isVisible = true
            butLocate?.isVisible = true
            butMenu?.isVisible = true
            butSearch?.z = 10F
            butLocate?.z = 10F
            butMenu?.z = 10F
            botBut?.isVisible = true
            botBut?.z = 5F
                // ордината = батон / высота картинки*высота лабиринта
        }
    }

    //check if intent is nfc detection and get string from tag
    private fun receiveMessage(checkIntent: Intent): String? {
        if (checkIntent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            // Retrieve the raw NDEF message from the tag
            val rawMessages = checkIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            val ndefMsg = rawMessages?.get(0) as NdefMessage
            val ndefRecord = ndefMsg.records[0]
            val inMessage = String(ndefRecord.payload)
            tvReceive?.text = inMessage

            but1?.isVisible = true
            but2?.isVisible = true
            but3?.isVisible = true
            but4?.isVisible = true
            but5?.isVisible = true

            return     inMessage
        } else
           return  null
    }

    private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {

        // here we are setting up receiving activity for a foreground dispatch
        // thus if activity is already started it will take precedence over any other activity or app
        // with the same intent filters

        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)

        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf<Array<String>>()

        filters[0] = IntentFilter()
        with(filters[0]) {
            this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
            this?.addCategory(Intent.CATEGORY_DEFAULT)
            try {
                this?.addDataType("text/plain")
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Check your MIME type")
            }
        }

        adapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
    }

    override fun onResume() {
        super.onResume()

        // foreground dispatch should be enabled here, as onResume is the guaranteed place where app
        // is in the foreground
        enableForegroundDispatch(this, this.nfcAdapter)
        receiveMessage(intent)

    }

    private fun disableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        adapter?.disableForegroundDispatch(activity)
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch(this, this.nfcAdapter)
    }

    private fun parseCoordinates(rawString: String): Pair<Float, Float> {
        val point: Array<Float> = arrayOf(0F, 0F)
        val pattern = Pattern.compile("\\d+")
        val matcher: Matcher = pattern.matcher(rawString)
        var start = 0

        for (i in point.indices) {
            matcher.find(start)
            val value = rawString.substring(matcher.start(), matcher.end())
            point[i] = value.toFloat()
            start = matcher.end()
        }
        return point[0] to point[1]
    }

}