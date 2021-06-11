package id.exomatik.absenki.ui.auth.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.ui.main.MainActivity
import id.exomatik.absenki.utils.DataSave
import id.exomatik.absenki.utils.dismissKeyboard

class LoginViewModel(
    private val navController: NavController,
    private val savedData: DataSave?,
    private val activity: Activity?) : BaseViewModel() {
    val userName = MutableLiveData<String>()
    val dataUser = MutableLiveData<ModelUser>()

    fun onClickBack(){
        activity?.let { dismissKeyboard(it) }
        navController.navigate(R.id.splashFragment)
    }

    fun onClickRegister(){
        activity?.let { dismissKeyboard(it) }
        navController.navigate(R.id.registerFragment)
    }

    fun onClickLogin(){
        activity?.let { dismissKeyboard(it) }
        val intent = Intent(activity, MainActivity::class.java)
        activity?.startActivity(intent)
        activity?.finish()
    }
}