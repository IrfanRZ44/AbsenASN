package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.textfield.TextInputLayout
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_change_password.*

class ChangePasswordActivity : AppCompatActivity(){
    private var dataUser: ModelUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_change_password)
        myCodeHere()
        onClick()
    }

    private fun myCodeHere() {
        supportActionBar?.hide()
        dataUser = intent.getParcelableExtra(Constant.reffUser)
    }

    private fun onClick(){
        etConfirmPassword.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickSave()
                return@OnEditorActionListener false
            }
            false
        })

        btnLogin.setOnClickListener {
            onClickSave()
        }
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        textStatus.text = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun setNullError(){
        etPassword.error = null
        etConfirmPassword.error = null
    }

    @SuppressLint("SetTextI18n")
    fun onClickSave(){
        setNullError()
        dismissKeyboard(this)

        val username = dataUser?.username
        val passwordNew = etPassword.editText?.text.toString()
        val confirmPasswordNew = etConfirmPassword.editText?.text.toString()


        if (!username.isNullOrEmpty()
            && passwordNew.isNotEmpty() && confirmPasswordNew.isNotEmpty()
            && passwordNew == confirmPasswordNew
            && passwordNew.length >= 6 && isContainNumber(passwordNew) && (isContainSmallText(passwordNew) || isContainBigText(passwordNew))
        ) {
            val md5PasswordNew = stringToMD5(passwordNew)

            progress.visibility = View.VISIBLE
            updatePassword(username, md5PasswordNew)
        }
        else {
            if (username.isNullOrEmpty()) {
                textStatus.text = "Error, terjadi kesalahan saat mengambil Username"
            }
            else if (passwordNew.isEmpty()) {
                setTextError("Error, mohon masukkan password baru", etPassword)
            }
            else if (confirmPasswordNew.isEmpty()) {
                setTextError("Error, mohon masukkan konfirmasi password baru", etConfirmPassword)
            }
            else if (passwordNew != confirmPasswordNew) {
                setTextError("Error, password yang Anda masukkan berbeda", etConfirmPassword)
            }
            else if (passwordNew.length < 6){
                setTextError("Error, password harus minimal 6 digit", etPassword)
            }
            else if (!isContainNumber(passwordNew)){
                setTextError("Error, password harus memiliki kombinasi angka", etPassword)
            }
            else if (!isContainSmallText(passwordNew) && !isContainBigText(passwordNew)){
                setTextError("Error, password harus memiliki kombinasi huruf", etPassword)
            }
        }
    }

    private fun updatePassword(username: String, passwordNew: String) {
        progress.visibility = View.VISIBLE

        val onCompleteListener = OnCompleteListener<Void> {
            progress.visibility = View.GONE
            if (it.isSuccessful){
                Toast.makeText(this, "Berhasil mengganti password", Toast.LENGTH_LONG).show()

                dismissKeyboard(this)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                textStatus.text = it.exception?.message?:"Gagal Mengganti Password"
            }
        }
        val onFailureListener = OnFailureListener {
            progress.visibility = View.GONE
            textStatus.text = it.message
        }

        FirebaseUtils.setValueWith2ChildString(
            Constant.reffUser
            , username
            , Constant.reffPassword
            , passwordNew
            , onCompleteListener
            , onFailureListener
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ForgetPasswordActivity::class.java)
        startActivity(intent)
        finish()
    }
}