package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.services.timer.TListener
import id.exomatik.absenasn.services.timer.TimeFormatEnum
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.FirebaseUtils
import id.exomatik.absenasn.utils.dismissKeyboard
import kotlinx.android.synthetic.main.activity_verify_forget_password.*
import java.util.concurrent.TimeUnit

class VerifyForgetPasswordActivity : AppCompatActivity(){
    val phoneCode = MutableLiveData<String>()
    var unverify = true
    var verifyId = ""
    lateinit var dataUser : ModelUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forget_password)
        myCodeHere()
    }

    @SuppressLint("SetTextI18n")
    private fun myCodeHere() {
        supportActionBar?.hide()
        try {
            dataUser = intent.getParcelableExtra(Constant.reffUser)?:throw Exception("Error, terjadi kesalahan saat mengirimkan kode")
            textStatus.text = "SMS dengan kode verifikasi telah dikirim ke " + dataUser.phone

            progress.visibility = View.GONE
            progressTimer.isEnabled = false
            sendCode()
        }catch (e: Exception){
            Toast.makeText(this, e.message + "Error, mohon ulangi proses masuk Anda", Toast.LENGTH_LONG).show()
        }
        setUpEditText()
        setProgress()
        onBackPressed()
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
                    phoneCode.value = etText1.text.toString() + etText2.text.toString() +
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
        dismissKeyboard(this)
        val intent = Intent(this, LoginActivity::class.java)
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
                , phoneCode.value ?: throw Exception("Error, kode verifikasi tidak boleh kosong")
            )

            val onCoCompleteListener =
                OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        progress.visibility = View.GONE

                        changePassword()
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

    private fun sendCode() {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @SuppressLint("SetTextI18n")
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                unverify = false
                textStatus.text = "Berhasil memverifikasi nomor " + dataUser.phone
                progress.visibility = View.GONE
                progressTimer.isEnabled = false

                changePassword()
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
                progressTimer.isEnabled = false
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

                            textStatus.text = "Kami sudah mengirimkan kode verifikasi ke nomor ${dataUser.phone}"
                            unverify = false
                            setProgress()

                            verifyId = verificationId
                        }
                    }
                }.start()
            }
        }

        FirebaseUtils.registerUser(dataUser.phone, callbacks, this)
    }

    private fun changePassword(){
        dismissKeyboard(this)
        val intent = Intent(this, ChangePasswordActivity::class.java)
        intent.putExtra(Constant.reffUser, dataUser)
        startActivity(intent)
        finish()
    }
}