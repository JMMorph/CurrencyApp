package mx.ipn.cic.geo.currency_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import mx.ipn.cic.geo.currency_app.databinding.ActivityMainBinding
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se coloca como comentario, cambio por usar viewbinding.
        // setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sBar = binding.seekBar
        val eText = binding.editCantidad
        var pval:Float  = 10.0F
        var max = 40

        // Write a message to the database
        val database = Firebase.database
        val myRef = database.getReference("app_currency")



        // Invocar el método para equivalencia de monedas.
        getCurrencyData(pval).start()

        sBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    pval = progress.toFloat()
                    binding.editCantidad.setText(pval.toString(), TextView.BufferType.EDITABLE)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                getCurrencyData(pval).start()
            }
        })


        eText.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                pval = eText.getText().toString().toFloat()
                max = pval.toInt() * 100 / 25
                sBar.max = max
                sBar.setProgress(pval.toInt())
                getCurrencyData(pval).start()



                return@OnEditorActionListener true
            }
            false
        })

        binding.botonGuardar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {

                val hora = Calendar.getInstance().getTime().toString()

                var cantidad = eText.getText().toString()
                val user = consulta(hora,cantidad)

                myRef.child("Consultas").push().setValue(user)

            }
        })


    }


    private fun getCurrencyData(valor: Float): Thread
    {
        return Thread {
            val url = URL("https://open.er-api.com/v6/latest/mxn")
            val connection = url.openConnection() as HttpsURLConnection

            Log.d("Resultado Petición: ", connection.responseCode.toString())

            if(connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, Request::class.java)
                updateUI(request,valor)
                inputStreamReader.close()
                inputSystem.close()
            }
            else {
                binding.textMonedaBase.text = "PROBLEMA EN CONEXIÓN"
            }
        }
    }

    private fun updateUI(request: Request,valor: Float)
    {
        runOnUiThread {
            kotlin.run {
                binding.textUltimActualizacion.text = request.time_last_update_utc
                binding.textMonedaEuro.text = String.format("EUR: %.2f", request.rates.EUR * valor)
                binding.textMonedaDolar.text = String.format("USD: %.2f", request.rates.USD * valor)
                binding.textMonedaLibra.text = String.format("GBP: %.2f", request.rates.GBP * valor)
                binding.textMonedaDanesa.text = String.format("DKK: %.2f", request.rates.DKK * valor)
                binding.textMonedaCheca.text = String.format("CZK: %.2f", request.rates.CZK * valor)
                binding.textMonedaQatar.text = String.format("QAR: %.2f", request.rates.QAR * valor)
                binding.textMonedaPLN.text = String.format("PLN: %.2f", request.rates.PLN * valor)
                binding.textMonedaNOK.text = String.format("NOK: %.2f", request.rates.NOK * valor)

            }
        }


    }



}

