package id.exomatik.absenasn.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.FirebaseUtils
import id.exomatik.absenasn.utils.dismissKeyboard
import kotlinx.android.synthetic.main.activity_forget_password.*

class ForgetPasswordActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_forget_password)
        myCodeHere()
        onClick()
    }

    private fun myCodeHere() {
        supportActionBar?.hide()
    }

    private fun onClick() {
        etUsername.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickLogin()
                return@OnEditorActionListener false
            }
            false
        })
    }

    @SuppressLint("SetTextI18n")
    fun onClickLogin(){
        etUsername.error = null
        dismissKeyboard(this)
        val username = etUsername.editText?.text.toString()

        if (username.isNotEmpty()) {
            progress.visibility = View.VISIBLE
            when {
                username.take(1) == "0" -> {
                    val phone = username.replaceFirst("0", "+62")
                    loginPhone(phone)
                }
                username.take(3) == "+62" -> {
                    val phone = username.replaceFirst("0", "+62")
                    loginPhone(phone)
                }
                else -> {
                    loginUsername(username)
                }
            }
        }
        else {
            textStatus.text = "Mohon masukkan username"
            etUsername.error = "Mohon masukkan username"
            etUsername.requestFocus()
            etUsername.findFocus()
        }
    }

    private fun loginUsername(username: String) {
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
                        verifyNumber(data)
                    }
                    else{
                        etUsername.requestFocus()
                        etUsername.findFocus()
                        etUsername.error = "Maaf, username belum terdaftar atau telah di banned oleh sistem"
                        textStatus.text = "Maaf, username belum terdaftar atau telah di banned oleh sistem"
                    }
                } else {
                    etUsername.requestFocus()
                    etUsername.findFocus()
                    etUsername.error = "Maaf, username belum terdaftar"
                    textStatus.text = "Maaf, username belum terdaftar"
                }
            }
        }

        FirebaseUtils.getData1Child(
            Constant.reffUser, username, valueEventListener
        )
    }

    private fun loginPhone(phone: String) {
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
                        val data = result.getValue(ModelUser::class.java)

                        if (data?.phone == phone){
                            dataUser = data
                        }
                    }

                    if (dataUser != null && dataUser.status == Constant.statusActive){
                        verifyNumber(dataUser)
                    }
                    else{
                        etUsername.requestFocus()
                        etUsername.findFocus()
                        etUsername.error = "Maaf, nomor HP belum terdaftar atau telah di banned oleh sistem"
                        textStatus.text = "Maaf, nomor HP belum terdaftar atau telah di banned oleh sistem"
                    }
                } else {
                    etUsername.requestFocus()
                    etUsername.findFocus()
                    etUsername.error = "Maaf, nomor HP belum terdaftar"
                    textStatus.text = "Maaf, nomor HP belum terdaftar"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(Constant.reffUser, Constant.phone, phone, valueEventListener)
    }

    private fun verifyNumber(dataUser: ModelUser) {
        dismissKeyboard(this)
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra(Constant.reffUser, dataUser)
        startActivity(intent)
        finish()
    }
}