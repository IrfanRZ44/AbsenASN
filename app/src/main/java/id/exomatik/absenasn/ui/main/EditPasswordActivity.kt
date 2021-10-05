package id.exomatik.absenasn.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.exomatik.absenasn.R
import id.exomatik.absenasn.utils.DataSave

class EditPasswordActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        myCodeHere()
    }

    private fun myCodeHere() {
        supportActionBar?.hide()
    }

}