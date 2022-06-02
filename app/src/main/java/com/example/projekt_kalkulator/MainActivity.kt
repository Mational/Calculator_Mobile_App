package com.example.projekt_kalkulator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt_kalkulator.databinding.ActivityMainBinding
import decln
import declog
import decpow
import decsqrt
import java.math.*
import java.text.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding                                               //dodawanie bindinga

    //opcje
    private var colorRedScreen: Int = 110
    private var colorGreenScreen: Int = 177
    private var colorBlueScreen: Int = 113

    private var numOfDecPlaces: Int = 5


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)                                       //dodawanie bindinga
        val view = binding.root                                                                     //dodawanie bindinga
        setContentView(view)                                                                        //dodawanie bindinga

        //pobieranie opcji
        //pobieranie i ładowniae koloru tła
        colorRedScreen = intent.getIntExtra("colorRedScreen", colorRedScreen)
        colorGreenScreen = intent.getIntExtra("colorGreenScreen", colorGreenScreen)
        colorBlueScreen = intent.getIntExtra("colorBlueScreen", colorBlueScreen)
        binding.screen.setBackgroundColor(Color.rgb(colorRedScreen, colorGreenScreen, colorBlueScreen))

        //pobieranie i ładowanie liczby miejsc po przecinku
        numOfDecPlaces = intent.getIntExtra("numOfDecPlaces", numOfDecPlaces)

        //łodowanie koloru tła kalkulatora
        binding.fullCalc.setBackgroundColor(Color.rgb(0, 0, 0))
    }

    private var stack = Vector<String>()
    private var clearFlag: Boolean = false      //flaga - czy wyczyścić całe okno
    private var memoryValue: String = ""

    //przejście do aktywności ustawienia
    fun goToOptions(v: View){
        val i = Intent(this, Ustawienia::class.java)
        i.putExtra("colorRedScreen", colorRedScreen)
        i.putExtra("colorGreenScreen", colorGreenScreen)
        i.putExtra("colorBlueScreen", colorBlueScreen)

        i.putExtra("numOfDecPlaces", numOfDecPlaces)
        startActivity(i)
    }

    private fun updateStack(t: String){
        //aktualizacja stosu
        stack.removeLast()
        stack.addElement(t)
        binding.stackSize.text = stack.size.toString()
    }

    fun write(v: View){
        var text = ""

        //wyjdź jeśli więcej niż 16 znaków
        if(text.length >= 17)
            return

        //jeżeli stos jest pusty dodaj pusty string na stos
        if(stack.isEmpty())
            stack.addElement("")

        //jeśli clearFlag nieaktywne nadpisz pusty string wartością ze stosu
        if(!clearFlag)
            text = stack.lastElement()

        //sprawdzanie czy kropka nie jest wpisywana na początku lub więcej niż raz
        if(v.tag == "." && (text.isEmpty() || text.indexOf('.') != -1))
            return

        //sprawdzanie poprawności wpisanych zer
        text = if(text == "0" && v.tag != ".")
            v.tag.toString()
        else
            text + v.tag.toString()

        updateStack(text)
        binding.stack1.text = stack.lastElement()
        clearFlag = false
    }

    fun changeSign(v: View){
        if(stack.isEmpty())
            return

        var text: String = stack.lastElement()

        //sprawdzanie czy liczba nie jest pusta albo zerem
        text = if (text.isEmpty() || text == "0")
            return
        else if (text[0] == '-')
            text.drop(1)
        else
            '-'.plus(text)

        updateStack(text)
        binding.stack1.text = stack.lastElement()
        clearFlag = false
    }

    fun clearAll(v :View){
        //czyszczenie stosu
        while(stack.isNotEmpty())
            stack.removeFirst()

        //czyszczenie okienek
        binding.stack1.text = ""
        binding.stack2.text = ""
        binding.stack3.text = ""
        binding.stack4.text = ""
        binding.stackSize.text = "0"
        clearFlag = false
        memoryValue = ""
    }

    private fun dotOnEndCheck(): Boolean {
        val text = binding.stack1.text.toString()
        if(text.indexOf('.') == text.length-1 || text.isEmpty())
            return true
        return false
    }

    private fun updateEnterDropStack(){
        val stackSize = stack.size

        // Wartości są zaokrąglane dlatego np. dla   4999999999999999999.50000 wypisuje 5E18
        // (przechowywany wynik jest prawidłowy
        val formatter: NumberFormat = DecimalFormat("0.############E0", DecimalFormatSymbols.getInstance(Locale.ROOT))
        binding.stackSize.text = stackSize.toString()

        if(stackSize > 0)
            if (stack[stackSize - 1].length > 17)
                binding.stack1.text = formatter.format(stack[stackSize - 1].toBigDecimal())
            else
                binding.stack1.text = stack[stackSize - 1]
        else
            binding.stack1.text = ""

        if(stackSize > 1)
            if (stack[stackSize - 2].length > 17)
                binding.stack2.text = formatter.format(stack[stackSize - 2].toBigDecimal())
            else
                binding.stack2.text = stack[stackSize - 2]
        else
            binding.stack2.text = ""

        if(stackSize > 2)
            if (stack[stackSize - 3].length > 17)
                binding.stack3.text = formatter.format(stack[stackSize - 3].toBigDecimal())
            else
                binding.stack3.text = stack[stackSize - 3]
        else
            binding.stack3.text = ""

        if(stackSize > 3)
            if (stack[stackSize - 4].length > 17)
                binding.stack4.text = formatter.format(stack[stackSize - 4].toBigDecimal())
            else
                binding.stack4.text = stack[stackSize - 4]
        else
            binding.stack4.text = ""
    }

    fun enterFun(v: View){
        if(dotOnEndCheck() || stack.isEmpty() || stack.size == 999) return

        stack.addElement(stack.lastElement())
        binding.stackSize.text = stack.size.toString()
        updateEnterDropStack()
        clearFlag = true
    }

    fun dropFun(v: View){
        if(stack.isEmpty()) return

        stack.removeLast()
        binding.stackSize.text = stack.size.toString()
        updateEnterDropStack()
        clearFlag = false
    }

    fun swapFun(v: View){
        if(!(stack.size >= 2 && !dotOnEndCheck()))    return

        val tmp = stack.lastElement()
        stack[stack.size-1] = stack[stack.size - 2]
        stack[stack.size - 2] = tmp
        updateEnterDropStack()
        clearFlag = false

    }

    fun undoFun(v: View){
        if(stack.isEmpty()) return

        var text = stack.lastElement()
        if(text.isEmpty())  return

        text = text.removeRange(text.lastIndex, text.lastIndex + 1)
        stack.removeLast()
        stack.addElement(text)
        updateEnterDropStack()
        clearFlag = false
    }

    fun mathFun(v: View){
        //sprawdzenie, czy dane znajdują się w dziedzinach funkcji
        if((stack.isEmpty()) ||
            (v.tag.toString() in listOf("log", "ln") && stack.lastElement().toBigDecimal() <= BigDecimal.valueOf(0)) ||
            (v.tag.toString() == "sqrt" && stack.lastElement().toBigDecimal() < BigDecimal.valueOf(0)) ||
            (v.tag.toString() in listOf("/", "1/x") && stack.lastElement() == "0"))
                return

        //wejście do części wykonawczej
        val forbiddenRemoveSigns: List<String>  = listOf("^2", "sqrt", "1/x", "log", "ln")
        if(((v.tag.toString() in forbiddenRemoveSigns && stack.size >=1) ||
            (v.tag.toString() !in forbiddenRemoveSigns && stack.size >= 2)) &&
            !dotOnEndCheck()) {

            //ładowanie wartości do zmiennych
            val num1: BigDecimal = stack.lastElement().toBigDecimal()
            var num2 : BigDecimal = BigDecimal.valueOf(0)
            if(stack.size >=2)
                num2 = stack[stack.size - 2].toBigDecimal()

            //usuwanie pierwszego elementu jeżeli operator dwuargumentowy
            if(v.tag.toString() !in forbiddenRemoveSigns && v.tag.toString() != "%")
                stack.removeLast()

            //wykonanie odpowiedniego działania
            when (v.tag.toString()) {
                "/" -> num2 = try{
                    num2.divide(num1)
                }catch(e: ArithmeticException){
                    (num2.toDouble() / num1.toDouble()).toBigDecimal()
                }
                "*" -> num2 = num2.multiply(num1)
                "-" -> num2 = num2.subtract(num1)
                "+" -> num2 = num2.add(num1)
                "^" -> num2 = decpow(num2, num1)
                "%" -> num2 = (num2.multiply(num1)).divide(BigDecimal.valueOf(100))
                "sqrt" -> num2 = decsqrt(num1)
                "^2" -> num2 = decpow(num1, BigDecimal.valueOf(2))
                "1/x" -> num2 = try{
                    BigDecimal.valueOf(1).divide(num1)
                }catch(e: ArithmeticException){
                    (1 / num1.toDouble()).toBigDecimal()
                }
                "log" -> num2 = declog(num1)
                "ln" -> num2 = decln(num1, 30)
            }

            //zamiana w liczbę całkowitą jeśli można
            val num3: BigDecimal = num2.toBigInteger().toBigDecimal()
            if (num3.compareTo(num2) != 0)
                stack[stack.size - 1] = num2.setScale(numOfDecPlaces, RoundingMode.HALF_UP).toString()
            else
                stack[stack.size - 1] = num3.toString()

            //aktualizacja stosus
            updateEnterDropStack()
            clearFlag = false
        }
    }

    fun memoryStore(v: View){
        if(stack.isNotEmpty() && !dotOnEndCheck() && stack.lastElement() != "") {
            memoryValue = stack.lastElement()
            dropFun(v)
        }
    }

    fun memoryRecall(v: View){
        if(memoryValue == "")   return

        if(stack.isEmpty()){
            stack.addElement("")
            binding.stack1.text = memoryValue
            updateStack(memoryValue)
        }else{
            enterFun(v)
            stack[stack.size - 1] = memoryValue
            updateEnterDropStack()
            clearFlag = false
        }
    }

    fun addSubToMemory(v: View){
        if(!(memoryValue != "" && !dotOnEndCheck() && stack.lastElement() != ""))   return

        val num1 = stack.lastElement().toBigDecimal()
        var num2 = memoryValue.toBigDecimal()

        when (v.tag.toString()) {
            "m-" -> num2 = num2.subtract(num1)
            "m+" -> num2 = num2.add(num1)
        }

        //zamiana w liczbę całkowitą jeśli można
        val num3: BigDecimal = num2.toBigInteger().toBigDecimal()
        memoryValue = if (num3.compareTo(num2) != 0)
            num2.setScale(numOfDecPlaces, RoundingMode.HALF_UP).toString()
        else
            num3.toString()
    }
}