@file:Suppress("DEPRECATION")

package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.storage.UploadTask
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.services.timer.TListener
import id.exomatik.absenasn.services.timer.TimeFormatEnum
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_verify_register.*
import java.util.concurrent.TimeUnit

class VerifyRegisterActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave
    var phoneCode = ""
    var unverify = true
    var verifyId = ""
    var etFotoProfil: Uri? = null
    var dataUser : ModelUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_verify_register)
        myCodeHere()
        onClick()
    }

    @SuppressLint("SetTextI18n")
    private fun myCodeHere() {
        savedData = DataSave(this)

        supportActionBar?.hide()

        try {
            etFotoProfil = intent.getParcelableExtra(Constant.reffFotoUser)?:throw Exception("Error, terjadi kesalahan saat pendaftaran. Mohon mendaftar dari awal dan pastikan koneksi Anda stabil")
            dataUser = intent.getParcelableExtra("dataUser")?:throw Exception("Error, terjadi kesalahan saat pendaftaran. Mohon mendaftar dari awal dan pastikan koneksi Anda stabil")
            verifyId = intent.getStringExtra("verifyId")?:throw Exception("Error, terjadi kesalahan saat pendaftaran. Mohon mendaftar dari awal dan pastikan koneksi Anda stabil")
            textStatus.text = "SMS dengan kode verifikasi telah dikirim ke " + dataUser?.phone
            progress.visibility = View.GONE
            progressTimer.isEnabled = false
        }catch (e: Exception){
            Toast.makeText(this, e.message + "Error, mohon ulangi proses masuk Anda", Toast.LENGTH_LONG).show()
        }
        sendCode()
        setUpEditText()
        setProgress()
    }

    private fun onClick() {
        imgBack.setOnClickListener {
            onClickBack()
        }

        progressTimer.setOnClickListener {
            progress.visibility = View.VISIBLE
            sendCode()
        }
    }

    private fun setProgress() {
        progressTimer.setCircularTimerListener(object : TListener {
            override fun updateDataOnTick(remainingTimeInMs: Long): String {
                // long seconds = (milliseconds / 1000);
                val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeInMs)
                progressTimer.suffix = " s"
                return seconds.toString()
            }

            override fun onTimerFinished() {
                progressTimer.prefix = ""
                progressTimer.suffix = ""
                progressTimer.text = "Kirim Ulang?"
                progress.visibility = View.GONE
                progressTimer.isEnabled = true
            }
        }, 60, TimeFormatEnum.SECONDS, 1)

        progressTimer.progress = 0F
        progressTimer.startTimer()
    }

    private fun setUpEditText() {
        etText1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text1 = etText1.text.toString()
                if (text1.isNotEmpty()){
                    etText1.clearFocus()
                    etText2.findFocus()
                    etText2.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etText2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text1 = etText1.text.toString()
                val text2 = etText2.text.toString()
                if (text1.isNotEmpty() && text2.isNotEmpty()){
                    etText2.clearFocus()
                    etText3.findFocus()
                    etText3.requestFocus()
                }
                else if (text2.isEmpty()){
                    etText2.clearFocus()
                    etText1.findFocus()
                    etText1.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etText3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text1 = etText1.text.toString()
                val text2 = etText2.text.toString()
                val text3 = etText3.text.toString()
                if (text1.isNotEmpty() && text2.isNotEmpty() && text3.isNotEmpty()){
                    etText3.clearFocus()
                    etText4.findFocus()
                    etText4.requestFocus()
                }
                else if (text3.isEmpty()){
                    etText3.clearFocus()
                    etText2.findFocus()
                    etText2.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etText4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text1 = etText1.text.toString()
                val text2 = etText2.text.toString()
                val text3 = etText3.text.toString()
                val text4 = etText4.text.toString()
                if (text1.isNotEmpty() && text2.isNotEmpty() && text3.isNotEmpty() &&
                    text4.isNotEmpty()){
                    etText4.clearFocus()
                    etText5.findFocus()
                    etText5.requestFocus()
                }
                else if (text4.isEmpty()){
                    etText4.clearFocus()
                    etText3.findFocus()
                    etText3.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etText5.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text1 = etText1.text.toString()
                val text2 = etText2.text.toString()
                val text3 = etText3.text.toString()
                val text4 = etText4.text.toString()
                val text5 = etText5.text.toString()
                if (text1.isNotEmpty() && text2.isNotEmpty() && text3.isNotEmpty() &&
                    text4.isNotEmpty() && text5.isNotEmpty()){
                    etText5.clearFocus()
                    etText6.findFocus()
                    etText6.requestFocus()
                }
                else if (text5.isEmpty()){
                    etText5.clearFocus()
                    etText4.findFocus()
                    etText4.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etText6.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text1 = etText1.text.toString()
                val text2 = etText2.text.toString()
                val text3 = etText3.text.toString()
                val text4 = etText4.text.toString()
                val text5 = etText5.text.toString()
                val text6 = etText6.text.toString()
                if (text1.isNotEmpty() && text2.isNotEmpty() && text3.isNotEmpty() &&
                    text4.isNotEmpty() && text5.isNotEmpty() && text6.isNotEmpty()){
                    phoneCode = etText1.text.toString() + etText2.text.toString() +
                            etText3.text.toString() + etText4.text.toString() +
                            etText5.text.toString() + etText6.text.toString()
                    verifyUser()
                }
                else if (text6.isEmpty()){
                    etText6.clearFocus()
                    etText5.findFocus()
                    etText5.requestFocus()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onClickBack()
    }

    private fun onClickBack(){
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra(Constant.reffUser, dataUser)
        intent.putExtra(Constant.reffFotoUser, etFotoProfil)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun verifyUser() {
        dismissKeyboard(this)
        progress.visibility = View.VISIBLE

        try {
            val credential = PhoneAuthProvider.getCredential(
                verifyId
                , phoneCode
            )

            val onCoCompleteListener =
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        progress.visibility = View.GONE

                        etFotoProfil?.let { saveFoto(it) }
                    } else {
                        textStatus.text = "Error, kode verifikasi salah"
                        progress.visibility = View.GONE
                        setEmptyEditText()
                    }
                }

            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(onCoCompleteListener)
        } catch (e: java.lang.Exception) {
            textStatus.text = e.message
            progress.visibility = View.GONE
            setEmptyEditText()
        }
    }

    private fun setEmptyEditText() {
        etText6.setText("")
        etText5.setText("")
        etText4.setText("")
        etText3.setText("")
        etText2.setText("")
        etText1.setText("")
        etText6.clearFocus()
        etText1.findFocus()
        etText1.requestFocus()
    }

    @SuppressLint("SetTextI18n")
    fun saveFoto(image : Uri){
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            textStatus.text = "Berhasil menyimpan foto"
            getUrlFoto(it)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        dataUser?.username?.let {
            FirebaseUtils.simpanFoto(Constant.reffFotoUser,
                it, image, onSuccessListener, onFailureListener)
        }
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot) {
        val onSuccessListener = OnSuccessListener<Uri?>{
            dataUser?.fotoProfil = it.toString()

            getUserToken()
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.getUrlFoto(uploadTask, onSuccessListener, onFailureListener)
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    private fun getUserToken() {
        progress.visibility = View.VISIBLE
        val onCompleteListener =
            OnCompleteListener<InstanceIdResult> { result ->
                if (result.isSuccessful) {
                    val token = result.result?.token

                    if (!token.isNullOrEmpty()){
                        dataUser?.token = token
                        val md5Password = dataUser?.password?.let { stringToMD5(it) }
                        dataUser?.password = md5Password?:""

                        addUserToFirebase()
                    }
                    else{
                        progress.visibility = View.GONE
                        textStatus.text = "Gagal mendapatkan token"
                        setEmptyEditText()
                    }

                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal mendapatkan token"
                    setEmptyEditText()
                }
            }

        FirebaseUtils.getUserToken(
            onCompleteListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun addUserToFirebase() {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    savedData.setDataObject(
                        dataUser, Constant.reffUser
                    )

                    progress.visibility = View.GONE
                    textStatus.text = "Berhasil menyimpan user"
                    val intent = Intent(this, SplashActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal menyimpan data user"
                    setEmptyEditText()
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
            setEmptyEditText()
        }

        dataUser?.let {
            FirebaseUtils.setValueObject(
                Constant.reffUser, it.username, it, onCompleteListener, onFailureListener
            )
        }
    }

    private fun sendCode() {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @SuppressLint("SetTextI18n")
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                unverify = false
                textStatus.text =
                    "Berhasil memverifikasi nomor " + dataUser?.phone
                progress.visibility = View.GONE
                progressTimer.isEnabled = false

                etFotoProfil?.let { saveFoto(it) }
                progress.visibility = View.GONE
            }

            @SuppressLint("SetTextI18n")
            override fun onVerificationFailed(e: FirebaseException) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        textStatus.text = "Error, Nomor Handphone tidak Valid"
                        setEmptyEditText()
                    }
                    is FirebaseTooManyRequestsException -> {
                        textStatus.text = "Error, Anda sudah terlalu banyak mengirimkan permintaan"
                        setEmptyEditText()
                    }
                    else -> {
                        textStatus.text = e.message
                        setEmptyEditText()
                    }
                }
                progress.visibility = View.GONE
                progressTimer.isEnabled = true
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                object : CountDownTimer(2000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onFinish() {
                        if (unverify) {
                            progress.visibility = View.GONE
                            progressTimer.isEnabled = false

                            textStatus.text = "Kami sudah mengirimkan kode verifikasi ke nomor ${dataUser?.phone}"
                            unverify = false
                            setProgress()

                            verifyId = verificationId
                        }
                    }
                }.start()
            }
        }

        dataUser?.phone?.let { FirebaseUtils.registerUser(it, callbacks, this) }
    }
}