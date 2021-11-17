@file:Suppress("DEPRECATION")

package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.InstanceIdResult
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        myCodeHere()
        onClick()
    }

    fun myCodeHere() {
        savedData = DataSave(this)
        supportActionBar?.hide()

    }

    private fun onClick() {
        etPassword.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickLogin()
                return@OnEditorActionListener false
            }
            false
        })

        btnLogin.setOnClickListener {
            onClickLogin()
        }

        textSignUp.setOnClickListener {
            dismissKeyboard(this)
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnForgetPassword.setOnClickListener {
            dismissKeyboard(this)
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun onClickLogin(){
        setNullError()
        dismissKeyboard(this)
        val username = etUsername.editText?.text.toString()
        val password = etPassword.editText?.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            progress.visibility = View.VISIBLE
            getUserToken(username, password)
        }
        else {
            if (username.isEmpty()) {
                setTextError("Mohon masukkan username", etUsername)
            } else if (password.isEmpty()) {
                setTextError("Mohon masukkan password", etPassword)
            }
        }
    }

    private fun setNullError(){
        etUsername.editText?.error = null
        etPassword.editText?.error = null
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        textStatus.text = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
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
                        when {
                            textInput.take(1) == "0" -> {
                                val phone = textInput.replaceFirst("0", "+62")
                                loginPhone(phone, password, tkn)
                            }
                            textInput.take(3) == "+62" -> {
                                val phone = textInput.replaceFirst("0", "+62")
                                loginPhone(phone, password, tkn)
                            }
                            else -> {
                                loginUsername(textInput, password, tkn)
                            }
                        }
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
                            data.status == Constant.statusRequest -> {
                                dialogSucces()
                            }
                            data.status == Constant.statusRejected -> {
                                dialogRejected(data)
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

    private fun loginPhone(phone: String, password: String, token: String) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                progress.visibility = View.GONE
                textStatus.text = result.message
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                progress.visibility = View.GONE
                if (result.exists()) {
                    var dataUser: ModelUser? = null
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelUser::class.java)

                        if (data?.phone == phone){
                            dataUser = data
                        }
                    }

                    if (dataUser != null && dataUser.status == Constant.statusActive){
                        val md5Password = stringToMD5(password)
                        if (dataUser.password == md5Password){
                            saveToken(token, dataUser)
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
                            dataUser == null -> {
                                etUsername.editText?.requestFocus()
                                etUsername.editText?.findFocus()
                                etUsername.editText?.error = "Maaf, nomor HP belum terdaftar"
                                textStatus.text = "Maaf, nomor HP belum terdaftar"
                            }
                            dataUser.status == Constant.statusRequest -> {
                                dialogSucces()
                            }
                            dataUser.status == Constant.statusRejected -> {
                                dialogRejected(dataUser)
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
                    etUsername.editText?.error = "Maaf, nomor HP belum terdaftar"
                    textStatus.text = "Maaf, nomor HP belum terdaftar"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(Constant.reffUser, Constant.phone, phone, valueEventListener)
    }

    private fun registerAgain(dataUser: ModelUser){
        dismissKeyboard(this)
        val intent = Intent(this, RegisterAgainActivity::class.java)
        intent.putExtra(Constant.reffUser, dataUser)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun saveToken(token: String, dataUser: ModelUser) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    if (dataUser.imei.isEmpty()){
                        dataUser.token = token
                        saveIMEI(dataUser)
                    }
                    else{
                        dataUser.token = token
                        savedData.setDataObject(dataUser, Constant.reffUser)
                        textStatus.text = "Berhasil login"
                        moveSplashAct()
                    }

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

    @SuppressLint("SetTextI18n", "HardwareIds")
    private fun saveIMEI(dataUser: ModelUser) {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    dataUser.imei = androidId
                    savedData.setDataObject(dataUser, Constant.reffUser)
                    textStatus.text = "Berhasil login"
                    moveSplashAct()
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal menyimpan IMEI terbaru"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.setValueWith2ChildString(
            Constant.reffUser
            , dataUser.username
            , "imei"
            , androidId
            , onCompleteListener
            , onFailureListener
        )
    }

    private fun moveSplashAct(){

        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun dialogRejected(dataUser: ModelUser) {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Ditolak")
        alert.setMessage("Maaf, pendaftaran Anda ditolak oleh Admin dengan Alasan : \n\n${dataUser.comment}")
        alert.setPositiveButton(
            "Daftar Ulang"
        ) { dialog, _ ->
            dialog.dismiss()
            registerAgain(dataUser)
        }

        alert.setNegativeButton(
            "Batal"
        ) { dialog, _ ->
            dialog.dismiss()
        }

        alert.show()
    }

    private fun dialogSucces() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Pendaftaran Anda Sedang Di Proses")
        alert.setMessage("Mohon tunggu proses verifikasi dalam waktu 1x24 jam")
        alert.setPositiveButton(
            "Tutup"
        ) { dialog, _ ->
            dialog.dismiss()
        }

        alert.show()
    }
}