package com.example.projekt_kalkulator

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.example.projekt_kalkulator.databinding.ActivityUstawieniaBinding                         //dodawanie bindinga


class Ustawienia : AppCompatActivity() {
    private lateinit var binding: ActivityUstawieniaBinding                                         //dodawanie bindinga

    //tworzenie zmiennych pod opcje
    private var colorRedScreen: Int = 0
    private var colorGreenScreen: Int = 0
    private var colorBlueScreen: Int = 0

    private var numOfDecPlaces: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUstawieniaBinding.inflate(layoutInflater)                                 //dodawanie bindinga
        val view = binding.root                                                                     //dodawanie bindinga
        setContentView(view)    //dodawanie bindinga

        //pobieranie domyślnych wartości opcji
        //kolor tła ekranu
        colorRedScreen = intent.getIntExtra("colorRedScreen", colorRedScreen)
        binding.redSliderScreen.progress = colorRedScreen
        binding.redValueScreen.text = colorRedScreen.toString()

        colorGreenScreen = intent.getIntExtra("colorGreenScreen", colorGreenScreen)
        binding.greenSliderScreen.progress = colorGreenScreen
        binding.greenValueScreen.text = colorGreenScreen.toString()

        colorBlueScreen = intent.getIntExtra("colorBlueScreen", colorBlueScreen)
        binding.blueSliderScreen.progress = colorBlueScreen
        binding.blueValueScreen.text = colorBlueScreen.toString()

        binding.showScreenColor.setBackgroundColor(Color.rgb(colorRedScreen, colorGreenScreen, colorBlueScreen))

        //liczba miejsc po przecinku
        numOfDecPlaces = intent.getIntExtra("numOfDecPlaces", numOfDecPlaces)
        binding.decPlaces.text = numOfDecPlaces.toString()
        if(numOfDecPlaces == 0){
            binding.decreaseDecPlacesButton.isEnabled = false
            binding.decreaseDecPlacesButton.isClickable = false
        }else if(numOfDecPlaces == 10){
            binding.increaseDecPlacesButton.isEnabled = false
            binding.increaseDecPlacesButton.isClickable = false
        }


        binding.redSliderScreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, currentValue: Int, p2: Boolean) {
                binding.redValueScreen.text = currentValue.toString()
                colorRedScreen = currentValue
                binding.showScreenColor.setBackgroundColor(Color.rgb(colorRedScreen, colorGreenScreen, colorBlueScreen))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.greenSliderScreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, currentValue: Int, p2: Boolean) {
                binding.greenValueScreen.text = currentValue.toString()
                colorGreenScreen = currentValue
                binding.showScreenColor.setBackgroundColor(Color.rgb(colorRedScreen, colorGreenScreen, colorBlueScreen))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        binding.blueSliderScreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, currentValue: Int, p2: Boolean) {
                binding.blueValueScreen.text = currentValue.toString()
                colorBlueScreen = currentValue
                binding.showScreenColor.setBackgroundColor(Color.rgb(colorRedScreen, colorGreenScreen, colorBlueScreen))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    fun changeNumOfDecPlaces(v: View){
        if(v.tag.toString() == "-"){
            binding.increaseDecPlacesButton.isClickable = true
            binding.increaseDecPlacesButton.isEnabled = true
            numOfDecPlaces--
            binding.decPlaces.text = numOfDecPlaces.toString()
            if(numOfDecPlaces == 0){
                v.isClickable = false
                v.isEnabled = false
            }
        }else if(v.tag.toString() == "+"){
            binding.decreaseDecPlacesButton.isClickable = true
            binding.decreaseDecPlacesButton.isEnabled = true
            numOfDecPlaces++
            binding.decPlaces.text = numOfDecPlaces.toString()
            if(numOfDecPlaces == 10){
                v.isClickable = false
                v.isEnabled = false
            }
        }
    }

    fun backToMainActivity(v: View){
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("colorRedScreen", colorRedScreen)
        i.putExtra("colorGreenScreen", colorGreenScreen)
        i.putExtra("colorBlueScreen", colorBlueScreen)
        i.putExtra("numOfDecPlaces", numOfDecPlaces)
        startActivity(i)
    }
}