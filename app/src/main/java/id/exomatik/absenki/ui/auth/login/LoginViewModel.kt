@file:Suppress("DEPRECATION")

package id.exomatik.absenki.ui.auth.login

import android.annotation.SuppressLint
import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.InstanceIdResult
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.utils.*

@SuppressLint("StaticFieldLeak")
class LoginViewModel(
    private val navController: NavController,
    private val savedData: DataSave?,
    private val activity: Activity?,
    private val editUsername: TextInputLayout,
    private val editPassword: TextInputLayout
) : BaseViewModel() {
    val etUsername = MutableLiveData<String>()
    val etPassword = MutableLiveData<String>()

    fun onClickRegister(){
        activity?.let { dismissKeyboard(it) }
        navController.navigate(R.id.registerFragment)
    }

    fun onClickForgetPassword(){
        activity?.let { dismissKeyboard(it) }
        message.value = "Lupa Password"
//        navController.navigate(R.id.forgetPasswordFragment)
    }

    fun onClickLogin(){
        setNullError()
        activity?.let { dismissKeyboard(it) }
        val username = etUsername.value
        val password = etPassword.value

        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            isShowLoading.value = true
            getUserToken(username, password)
        }
        else {
            if (username.isNullOrEmpty()) {
                setTextError("Mohon masukkan username", editUsername)
            } else if (password.isNullOrEmpty()) {
                setTextError("Mohon masukkan password", editPassword)
            }
        }
    }

    private fun setNullError(){
        editUsername.error = null
        editPassword.error = null
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        message.value = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun getUserToken(textInput: String, password: String) {
        isShowLoading.value = true

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
                        isShowLoading.value = false
                        message.value = e.message
                    }
                } else {
                    isShowLoading.value = false
                    message.value = "Gagal mendapatkan token"
                }
            }

        FirebaseUtils.getUserToken(
            onCompleteListener
        )
    }

    private fun loginUsername(username: String, password: String, token: String) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                isShowLoading.value = false
                message.value = result.message
            }

            override fun onDataChange(result: DataSnapshot) {
                isShowLoading.value = false
                if (result.exists()) {
                    val data = result.getValue(ModelUser::class.java)

                    if (data != null && data.status == Constant.statusActive){
                        val md5Password = stringToMD5(password)
                        if (data.password == md5Password){
                            saveToken(token, data)
                        }
                        else{
                            editPassword.requestFocus()
                            editPassword.findFocus()
                            editPassword.error = "Maaf, password yang Anda masukkan salah"
                            message.value = "Maaf, password yang Anda masukkan salah"
                        }
                    }
                    else{
                        editUsername.requestFocus()
                        editUsername.findFocus()
                        editUsername.error = "Maaf, username belum terdaftar atau telah di banned oleh sistem"
                        message.value = "Maaf, username belum terdaftar atau telah di banned oleh sistem"
                    }
                } else {
                    editUsername.requestFocus()
                    editUsername.findFocus()
                    editUsername.error = "Maaf, username belum terdaftar"
                    message.value = "Maaf, username belum terdaftar"
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
                isShowLoading.value = false
                message.value = result.message
            }

            override fun onDataChange(result: DataSnapshot) {
                isShowLoading.value = false
                if (result.exists()) {
                    var dataUser: ModelUser? = null
                    for (snapshot in result.children) {
                        val data = result.getValue(ModelUser::class.java)

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
                            editPassword.requestFocus()
                            editPassword.findFocus()
                            editPassword.error = "Maaf, password yang Anda masukkan salah"
                            message.value = "Maaf, password yang Anda masukkan salah"
                        }
                    }
                    else{
                        editUsername.requestFocus()
                        editUsername.findFocus()
                        editUsername.error = "Maaf, nomor HP belum terdaftar atau telah di banned oleh sistem"
                        message.value = "Maaf, nomor HP belum terdaftar atau telah di banned oleh sistem"
                    }
                } else {
                    editUsername.requestFocus()
                    editUsername.findFocus()
                    editUsername.error = "Maaf, nomor HP belum terdaftar"
                    message.value = "Maaf, nomor HP belum terdaftar"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(Constant.reffUser, Constant.phone, phone, valueEventListener)
    }

    private fun saveToken(token: String, dataUser: ModelUser) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    savedData?.setDataObject(dataUser, Constant.reffUser)
                    message.value = "Berhasil login"
                    navController.navigate(R.id.splashFragment)
                } else {
                    isShowLoading.value = false
                    message.value = "Gagal menyimpan data user"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            isShowLoading.value = false
            message.value = result.message
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