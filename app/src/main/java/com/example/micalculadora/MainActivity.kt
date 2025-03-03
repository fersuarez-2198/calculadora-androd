package com.example.micalculadora

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    // Referencia al TextView donde se mostrará el resultado de la calculadora
    var tvRes: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ajuste de los márgenes para que la UI respete las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización del TextView donde se mostrarán los cálculos
        tvRes = findViewById(R.id.tvRes)
    }

    fun calcular(view: View) {
        val boton = view as Button // Se obtiene el botón que fue presionado
        val textoBoton = boton.text.toString() // Se obtiene el texto del botón
        val concatenar = tvRes?.text.toString() + textoBoton // Se concatena el texto actual con el nuevo input
        val concatenarSinCeros = quitarCerosIzquierda(concatenar) // Se eliminan ceros a la izquierda si los hay

        when (textoBoton) {
            "=" -> { // Si se presiona "=", se evalúa la expresión matemática
                try {
                    val resultado = eval(tvRes?.text.toString()) // Se calcula el resultado
                    tvRes?.text = resultado.toString() // Se actualiza el TextView con el resultado
                } catch (e: Exception) {
                    tvRes?.text = "Error" // Si hay un error en la evaluación, se muestra "Error"
                }
            }
            "AC" -> tvRes?.text = "0" // Si se presiona "AC", se reinicia el resultado a "0"
            else -> tvRes?.text = concatenarSinCeros // Se actualiza el TextView con la nueva expresión
        }
    }

    // Función para eliminar ceros innecesarios al inicio de la expresión
    fun quitarCerosIzquierda(str: String): String {
        return str.trimStart('0').ifEmpty { "0" }
    }

    // Función para evaluar una expresión matemática simple
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            // Avanza al siguiente carácter en la cadena
            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            // Verifica si el carácter actual coincide con el esperado y avanza
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar() // Ignora espacios en blanco
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            // Comienza la evaluación de la expresión
            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: ${ch.toChar()}")
                return x
            }

            // Procesa sumas y restas
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm() // Suma
                        eat('-'.code) -> x -= parseTerm() // Resta
                        else -> return x
                    }
                }
            }

            // Procesa multiplicaciones y divisiones
            fun parseTerm(): Double {
                var x = parseNumber()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseNumber() // Multiplicación
                        eat('/'.code) -> x /= parseNumber() // División
                        else -> return x
                    }
                }
            }

            // Procesa números en la expresión
            fun parseNumber(): Double {
                val startPos = pos
                while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                return str.substring(startPos, pos).toDouble()
            }
        }.parse()
    }
}
