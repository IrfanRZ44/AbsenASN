package id.exomatik.absenasn.ui.main.account

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.exomatik.absenasn.R
import id.exomatik.absenasn.utils.DataSave
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)
        myCodeHere()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)
        supportActionBar?.hide()

        textAbout.text = savedData.getDataApps()?.aboutApps
    }
}