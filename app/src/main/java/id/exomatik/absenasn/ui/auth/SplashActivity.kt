package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.InstanceIdResult
import id.exomatik.absenasn.BuildConfig
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelInfoApps
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.ui.main.MainActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_splash.progress
import kotlinx.android.synthetic.main.activity_splash.textStatus

class SplashActivity : AppCompatActivity(){
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
                btnUpdate.visibility = View.VISIBLE
            }
        }
    }

    private fun timerNavigation(){
        progress.visibility = View.VISIBLE

        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                showLog(savedData.getDataApps()?.auth)

                if (savedData.getDataUser()?.username.isNullOrEmpty() || savedData.getDataUser()?.phone.isNullOrEmpty()){
                    if (savedData.getDataApps()?.auth == Constant.statusActive){
                        moveLoginAct()
                    }
                    else{
                        getUserToken("Admin", "123456A")
                    }
                }
                else{
                    moveMainAct()
                }
            }
        }.start()
    }

    private fun moveMainAct(){
        progress.visibility = View.GONE
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveLoginAct(){
        progress.visibility = View.GONE
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetTextI18n")
    private fun getUserToken(textInput: String, password: String) {
        progress.visibility = View.VISIBLE

        val onCompleteListener =
            OnCompleteListener<InstanceIdResult> { result ->
                if (result.isSuccessful) {
                    try {
                        val tkn = result.result?.token
                            ?: throw Exception("Error, kesalahan saat menyimpan token")

                        loginUsername(textInput, password, tkn)
                    } catch (e: Exception) {
                        progress.visibility = View.GONE
                        textStatus.text = e.message
                    }
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal mendapatkan token"
                }
            }

        FirebaseUtils.getUserToken(
            onCompleteListener
        )
    }

    private fun loginUsername(username: String, password: String, token: String) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                progress.visibility = View.GONE
                textStatus.text = result.message
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                progress.visibility = View.GONE
                if (result.exists()) {
                    val data = result.getValue(ModelUser::class.java)

                    if (data != null && data.status == Constant.statusActive){
                        val md5Password = stringToMD5(password)
                        if (data.password == md5Password){
                            saveToken(token, data)
                        }
                        else{
                            etPassword.editText?.requestFocus()
                            etPassword.editText?.findFocus()
                            etPassword.editText?.error = "Maaf, password yang Anda masukkan salah"
                            textStatus.text = "Maaf, password yang Anda masukkan salah"
                        }
                    }
                    else{
                        when {
                            data == null -> {
                                etUsername.editText?.requestFocus()
                                etUsername.editText?.findFocus()
                                etUsername.editText?.error = "Maaf, username belum terdaftar"
                                textStatus.text = "Maaf, username belum terdaftar"
                            }
                            else -> {
                                etUsername.editText?.requestFocus()
                                etUsername.editText?.findFocus()
                                etUsername.editText?.error = "Maaf, akun telah di banned oleh sistem"
                                textStatus.text = "Maaf, akun telah di banned oleh sistem"
                            }
                        }
                    }
                } else {
                    etUsername.editText?.requestFocus()
                    etUsername.editText?.findFocus()
                    etUsername.editText?.error = "Maaf, username belum terdaftar"
                    textStatus.text = "Maaf, username belum terdaftar"
                }
            }
        }

        FirebaseUtils.getData1Child(
            Constant.reffUser, username, valueEventListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun saveToken(token: String, dataUser: ModelUser) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    dataUser.token = token
                    savedData.setDataObject(dataUser, Constant.reffUser)
                    textStatus.text = "Berhasil login"
                    val intent = Intent(this, SplashActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal menyimpan data user"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.setValueWith2ChildString(
            Constant.reffUser
            , dataUser.username
            , Constant.reffToken
            , token
            , onCompleteListener
            , onFailureListener
        )
    }
}