package id.exomatik.absenki.ui.auth.forgetPassword

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.ui.auth.verifyForgetPassword.VerifyForgetPasswordFragment
import id.exomatik.absenki.utils.*

@SuppressLint("StaticFieldLeak")
class ForgetPasswordViewModel(
    private val navController: NavController,
    private val activity: Activity?,
    private val editUsername: TextInputLayout,
) : BaseViewModel() {
    val etUsername = MutableLiveData<String>()

    fun onClickLogin(){
        editUsername.error = null
        activity?.let { dismissKeyboard(it) }
        val username = etUsername.value

        if (!username.isNullOrEmpty()) {
            isShowLoading.value = true
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
            message.value = "Mohon masukkan username"
            editUsername.error = "Mohon masukkan username"
            editUsername.requestFocus()
            editUsername.findFocus()
        }
    }

    private fun loginUsername(username: String) {
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
                        verifyNumber(data)
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

    private fun loginPhone(phone: String) {
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
                        verifyNumber(dataUser)
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

    private fun verifyNumber(dataUser: ModelUser) {
        val bundle = Bundle()
        val fragmentTujuan = VerifyForgetPasswordFragment()
        bundle.putParcelable(Constant.reffUser, dataUser)
        fragmentTujuan.arguments = bundle

        navController.navigate(R.id.verifyForgetPasswordFragment, bundle)
    }
}