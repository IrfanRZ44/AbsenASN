package id.exomatik.absenasn.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.exomatik.absenasn.R
import id.exomatik.absenasn.utils.DataSave

class BlankActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        myCodeHere()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)
        supportActionBar?.hide()
    }

}