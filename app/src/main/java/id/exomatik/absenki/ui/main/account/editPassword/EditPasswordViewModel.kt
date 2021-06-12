package id.exomatik.absenki.ui.main.account.editPassword

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.textfield.TextInputLayout
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.ui.auth.AuthActivity
import id.exomatik.absenki.utils.*

@SuppressLint("StaticFieldLeak")
class EditPasswordViewModel(
    private val savedData: DataSave,
    private val activity: Activity?,
    private val editPasswordOld: TextInputLayout,
    private val editPasswordNew: TextInputLayout,
    private val editConfirmPasswordNew: TextInputLayout
) : BaseViewModel() {
    val etPasswordOld = MutableLiveData<String>()
    val etPasswordNew = MutableLiveData<String>()
    val etConfirmPasswordNew = MutableLiveData<String>()

    private fun setTextError(msg: String, editText: TextInputLayout){
        message.value = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun setNullError(){
        editPasswordOld.error = null
        editPasswordNew.error = null
        editConfirmPasswordNew.error = null
    }

    fun onClickEditPassword(){
        setNullError()
        activity?.let { dismissKeyboard(it) }

        val username = savedData.getDataUser()?.username
        val passwordOld = etPasswordOld.value
        val passwordNew = etPasswordNew.value
        val confirmPasswordNew = etConfirmPasswordNew.value


        if (!username.isNullOrEmpty() && !passwordOld.isNullOrEmpty()
            && !passwordNew.isNullOrEmpty() && !confirmPasswordNew.isNullOrEmpty()
            && passwordNew == confirmPasswordNew
            && passwordNew.length >= 6 && isContainNumber(passwordNew) && (isContainSmallText(passwordNew) || isContainBigText(passwordNew))
        ) {
            val md5PasswordOld = savedData.getDataUser()?.password
            val md5InputPasswordOld = stringToMD5(passwordOld)
            val md5PasswordNew = stringToMD5(passwordNew)

            if (md5PasswordOld == md5InputPasswordOld){
                isShowLoading.value = true
                updatePassword(username, md5PasswordNew)
            }
            else{
                setTextError("Error, password lama yang Anda masukkan salah", editPasswordOld)
            }
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
                deleteToken(username)
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

    private fun deleteToken(username: String) {
        isShowLoading.value = true
        FirebaseUtils.stopRefresh()

        val onCompleteListener = OnCompleteListener<Void> {
            isShowLoading.value = false
            if (it.isSuccessful){
                Toast.makeText(activity, "Berhasil mengganti password", Toast.LENGTH_LONG).show()

                savedData.setDataObject(ModelUser(), Constant.reffUser)

                val intent = Intent(activity, AuthActivity::class.java)
                activity?.startActivity(intent)
                activity?.finish()
            }
            else{
                message.value = it.exception?.message?:"Gagal Keluar"
            }
        }
        val onFailureListener = OnFailureListener {
            isShowLoading.value = false
            message.value = it.message
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
}