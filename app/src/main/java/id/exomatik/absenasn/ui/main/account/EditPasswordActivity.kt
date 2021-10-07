package id.exomatik.absenasn.ui.main.account

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import id.exomatik.absenasn.ui.auth.SplashActivity
import id.exomatik.absenasn.ui.main.MainActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_edit_password.*

class EditPasswordActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_password)
        myCodeHere()
        onClick()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Password"
        supportActionBar?.show()
    }

    private fun onClick(){
        etConfirmPasswordNew.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickEditPassword()
                return@OnEditorActionListener false
            }
            false
        })

        btnSave.setOnClickListener {
            onClickEditPassword()
        }
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        textStatus.text = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun setNullError(){
        etPasswordOld.error = null
        etPasswordNew.error = null
        etConfirmPasswordNew.error = null
    }

    @SuppressLint("SetTextI18n")
    fun onClickEditPassword(){
        setNullError()
        dismissKeyboard(this)

        val username = savedData.getDataUser()?.username
        val passwordOld = etPasswordOld.editText?.text.toString()
        val passwordNew = etPasswordNew.editText?.text.toString()
        val confirmPasswordNew = etConfirmPasswordNew.editText?.text.toString()


        if (!username.isNullOrEmpty() && passwordOld.isNotEmpty()
            && passwordNew.isNotEmpty() && confirmPasswordNew.isNotEmpty()
            && passwordNew == confirmPasswordNew
            && passwordNew.length >= 6 && isContainNumber(passwordNew) && (isContainSmallText(passwordNew) || isContainBigText(passwordNew))
        ) {
            val md5PasswordOld = savedData.getDataUser()?.password
            val md5InputPasswordOld = stringToMD5(passwordOld)
            val md5PasswordNew = stringToMD5(passwordNew)

            if (md5PasswordOld == md5InputPasswordOld){
                progress.visibility = View.VISIBLE
                updatePassword(username, md5PasswordNew)
            }
            else{
                setTextError("Error, password lama yang Anda masukkan salah", etPasswordOld)
            }
        }
        else {
            if (username.isNullOrEmpty()) {
                textStatus.text = "Error, terjadi kesalahan saat mengambil Username"
            }
            else if (passwordNew.isEmpty()) {
                setTextError("Error, mohon masukkan password baru", etPasswordNew)
            }
            else if (confirmPasswordNew.isEmpty()) {
                setTextError("Error, mohon masukkan konfirmasi password baru", etConfirmPasswordNew)
            }
            else if (passwordNew != confirmPasswordNew) {
                setTextError("Error, password yang Anda masukkan berbeda", etConfirmPasswordNew)
            }
            else if (passwordNew.length < 6){
                setTextError("Error, password harus minimal 6 digit", etPasswordNew)
            }
            else if (!isContainNumber(passwordNew)){
                setTextError("Error, password harus memiliki kombinasi angka", etPasswordNew)
            }
            else if (!isContainSmallText(passwordNew) && !isContainBigText(passwordNew)){
                setTextError("Error, password harus memiliki kombinasi huruf", etPasswordNew)
            }
        }
    }

    private fun updatePassword(username: String, passwordNew: String) {
        progress.visibility = View.VISIBLE

        val onCompleteListener = OnCompleteListener<Void> {
            progress.visibility = View.GONE
            if (it.isSuccessful){
                deleteToken(username)
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

    private fun deleteToken(username: String) {
        progress.visibility = View.VISIBLE
        FirebaseUtils.stopRefresh()

        val onCompleteListener = OnCompleteListener<Void> {
            progress.visibility = View.GONE
            if (it.isSuccessful){
                Toast.makeText(this, "Berhasil mengganti password", Toast.LENGTH_LONG).show()

                savedData.setDataObject(ModelUser(), Constant.reffUser)

                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                textStatus.text = it.exception?.message?:"Gagal Keluar"
            }
        }
        val onFailureListener = OnFailureListener {
            progress.visibility = View.GONE
            textStatus.text = it.message
        }

        FirebaseUtils.setValueWith2ChildString(
            Constant.reffUser
            , username
            , Constant.reffToken
            , ""
            , onCompleteListener
            , onFailureListener
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}