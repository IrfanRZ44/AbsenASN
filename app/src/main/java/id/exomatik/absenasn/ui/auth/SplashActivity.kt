package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.BuildConfig
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelInfoApps
import id.exomatik.absenasn.ui.main.MainActivity
import id.exomatik.absenasn.ui.main.MainTesActivity
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.FirebaseUtils
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity(){
    private var isShowUpdate = false
    private lateinit var savedData : DataSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        myCodeHere()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)

        supportActionBar?.hide()
        getInfoApps()

        btnUpdate.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${packageName}")
                )
            )
        }
    }

    private fun getInfoApps() {
        progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                progress.visibility = View.GONE
                textStatus.text = result.message
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                progress.visibility = View.GONE
                if (result.exists()) {
                    val data = result.getValue(ModelInfoApps::class.java)

                    savedData.setDataObject(data, Constant.reffInfoApps)
                    checkingSavedData()
                }
                else{
                    textStatus.text = "Gagal mengambil data aplikasi"
                }
            }
        }

        FirebaseUtils.getDataObject(
            Constant.reffInfoApps
            , valueEventListener
        )
    }

    @SuppressLint("SetTextI18n")
    fun checkingSavedData() {
        val dataApps = savedData.getDataApps()

        if (dataApps == null){
            getInfoApps()
        }
        else{
            if (dataApps.versionApps == BuildConfig.VERSION_NAME){
                if (dataApps.statusApps == Constant.statusActive){
                    timerNavigation()
                }
                else{
                    textStatus.text = dataApps.statusApps
                }
            }
            else{
                textStatus.text = "Mohon perbarui versi aplikasi ke ${dataApps.versionApps}"
                isShowUpdate = true
            }
        }
    }

    private fun timerNavigation(){
        progress.visibility = View.VISIBLE

        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if (savedData.getDataUser()?.username.isNullOrEmpty() || savedData.getDataUser()?.phone.isNullOrEmpty()){
                    moveLoginAct()
                }
                else{
                    moveMainAct()
                }
            }
        }.start()
    }

    private fun moveMainAct(){
        progress.visibility = View.GONE
//        val intent = Intent(this, MainActivity::class.java)
        val intent = Intent(this, MainTesActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveLoginAct(){
        progress.visibility = View.GONE
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}