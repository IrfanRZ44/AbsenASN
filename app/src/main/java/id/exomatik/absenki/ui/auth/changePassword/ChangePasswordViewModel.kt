package id.exomatik.absenki.ui.auth.changePassword

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.textfield.TextInputLayout
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.ui.auth.login.LoginFragment
import id.exomatik.absenki.utils.*

@SuppressLint("StaticFieldLeak")
class ChangePasswordViewModel(
    private val activity: Activity?,
    private val editPasswordNew: TextInputLayout,
    private val editConfirmPasswordNew: TextInputLayout,
    private val navController: NavController
) : BaseViewModel() {
    val etPasswordNew = MutableLiveData<String>()
    val etConfirmPasswordNew = MutableLiveData<String>()
    var dataUser: ModelUser? = null

    private fun setTextError(msg: String, editText: TextInputLayout){
        message.value = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun setNullError(){
        editPasswordNew.error = null
        editConfirmPasswordNew.error = null
    }

    fun onClickSave(){
        setNullError()
        activity?.let { dismissKeyboard(it) }

        val username = dataUser?.username
        val passwordNew = etPasswordNew.value
        val confirmPasswordNew = etConfirmPasswordNew.value


        if (!username.isNullOrEmpty()
            && !passwordNew.isNullOrEmpty() && !confirmPasswordNew.isNullOrEmpty()
            && passwordNew == confirmPasswordNew
            && passwordNew.length >= 6 && isContainNumber(passwordNew) && (isContainSmallText(passwordNew) || isContainBigText(passwordNew))
        ) {
            val md5PasswordNew = stringToMD5(passwordNew)

            isShowLoading.value = true
            updatePassword(username, md5PasswordNew)
        }
        else {
            if (username.isNullOrEmpty()) {
                message.value = "Error, terjadi kesalahan saat mengambil Username"
            }
            else if (passwordNew.isNullOrEmpty()) {
                setTextError("Error, mohon masukkan password baru", editPasswordNew)
            }
            else if (confirmPasswordNew.isNullOrEmpty()) {
                setTextError("Error, mohon masukkan konfirmasi password baru", editConfirmPasswordNew)
            }
            else if (passwordNew != confirmPasswordNew) {
                setTextError("Error, password yang Anda masukkan berbeda", editConfirmPasswordNew)
            }
            else if (passwordNew.length < 6){
                setTextError("Error, password harus minimal 6 digit", editPasswordNew)
            }
            else if (!isContainNumber(passwordNew)){
                setTextError("Error, password harus memiliki kombinasi angka", editPasswordNew)
            }
            else if (!isContainSmallText(passwordNew) && !isContainBigText(passwordNew)){
                setTextError("Error, password harus memiliki kombinasi huruf", editPasswordNew)
            }
        }
    }

    private fun updatePassword(username: String, passwordNew: String) {
        isShowLoading.value = true

        val onCompleteListener = OnCompleteListener<Void> {
            isShowLoading.value = false
            if (it.isSuccessful){
                Toast.makeText(activity, "Berhasil mengganti password", Toast.LENGTH_LONG).show()
                val bundle = Bundle()
                val fragmentTujuan = LoginFragment()
                bundle.putParcelable(Constant.reffUser, dataUser)
                fragmentTujuan.arguments = bundle

                val navOption = NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
                navController.navigate(R.id.loginFragment, null, navOption)
            }
            else{
                message.value = it.exception?.message?:"Gagal Mengganti Password"
            }
        }
        val onFailureListener = OnFailureListener {
            isShowLoading.value = false
            message.value = it.message
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
}