package id.exomatik.absenasn.ui.main.account

import android.os.Bundle
import android.view.MenuItem
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tentang Aplikasi"
        supportActionBar?.show()

        textAbout.text = savedData.getDataApps()?.aboutApps
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}