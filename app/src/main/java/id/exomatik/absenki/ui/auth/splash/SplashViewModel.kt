package id.exomatik.absenki.ui.auth.splash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import id.exomatik.absenki.BuildConfig
import id.exomatik.absenki.R
import id.exomatik.absenki.ui.main.MainActivity
import id.exomatik.absenki.utils.Constant
import id.exomatik.absenki.utils.DataSave
import id.exomatik.absenki.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelInfoApps

@SuppressLint("StaticFieldLeak")
class SplashViewModel(
    private val navController: NavController,
    private val savedData: DataSave?,
    private val activity: Activity?
    ) : BaseViewModel() {
    val isShowUpdate = MutableLiveData<Boolean>()

    fun getInfoApps() {
        isShowLoading.value = true

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                isShowLoading.value = false
                message.value = "Gagal mengambil data aplikasi"
            }

            override fun onDataChange(result: DataSnapshot) {
                isShowLoading.value = false
                if (result.exists()) {
                    val data = result.getValue(ModelInfoApps::class.java)

                    savedData?.setDataObject(data, Constant.reffInfoApps)
                    checkingSavedData()
                }
                else{
                    message.value = "Gagal mengambil data aplikasi"
                }
            }
        }

        FirebaseUtils.getDataObject(
            Constant.reffInfoApps
            , valueEventListener
        )
    }

    fun checkingSavedData() {
        val dataApps = savedData?.getDataApps()

        if (dataApps == null){
            getInfoApps()
        }
        else{
            if (dataApps.versionApps == BuildConfig.VERSION_NAME){
                if (dataApps.statusApps == Constant.statusActive){
                    timerNavigation()
                }
                else{
                    message.value = dataApps.statusApps
                }
            }
            else{
                message.value = "Mohon perbarui versi aplikasi ke ${dataApps.versionApps}"
                isShowUpdate.value = true
            }
        }
    }

    private fun timerNavigation(){
        isShowLoading.value = true

        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if (savedData?.getDataUser()?.username.isNullOrEmpty() || savedData?.getDataUser()?.phone.isNullOrEmpty()){
                    navController.navigate(R.id.loginFragment)
                }
                else{
                    isShowLoading.value = false
                    val intent = Intent(activity, MainActivity::class.java)
                    activity?.startActivity(intent)
                    activity?.finish()
                }
            }
        }.start()
    }
}