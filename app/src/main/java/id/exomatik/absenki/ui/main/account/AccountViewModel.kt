package id.exomatik.absenki.ui.main.account

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseViewModel
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.ui.auth.AuthActivity
import id.exomatik.absenki.utils.Constant
import id.exomatik.absenki.utils.Constant.attention
import id.exomatik.absenki.utils.DataSave
import id.exomatik.absenki.utils.FirebaseUtils

@SuppressLint("StaticFieldLeak")
class AccountViewModel(
    private val savedData: DataSave?,
    private val activity: Activity?,
    private val navController: NavController,
    private val context: Context?
) : BaseViewModel() {
    val dataUser = MutableLiveData<ModelUser>()

    private fun deleteToken(dataUser: ModelUser) {
        isShowLoading.value = true

        val onCompleteListener = OnCompleteListener<Void> {
            isShowLoading.value = false
            if (it.isSuccessful){
                Toast.makeText(context, "Berhasil Keluar", Toast.LENGTH_LONG).show()

                savedData?.setDataObject(ModelUser(), Constant.reffUser)

                val intent = Intent(context, AuthActivity::class.java)
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
            , dataUser.username
            , Constant.reffToken
            , ""
            , onCompleteListener
            , onFailureListener
        )
    }

    fun onClickEditProfil(){
        navController.navigate(R.id.editProfilFragment)
    }

    fun onClickEditPassword(){
        navController.navigate(R.id.editPasswordFragment)
    }

    fun onClickRating(){
        activity?.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${activity.packageName}")
            )
        )
    }

    fun onClickProfil(){
        navController.navigate(R.id.aboutFragment)
    }

    fun onClickLogout(){
        val ctx = context

        if (ctx != null){
            val alert = AlertDialog.Builder(ctx)
            alert.setTitle(attention)
            alert.setMessage(Constant.alertLogout)
            alert.setPositiveButton(
                Constant.iya
            ) { _, _ ->
                val dataUser = savedData?.getDataUser()
                if (dataUser != null){
                    FirebaseUtils.stopRefresh()
                    deleteToken(dataUser)
                }
            }
            alert.setNegativeButton(
                Constant.tidak
            ) { dialog, _ -> dialog.dismiss() }

            alert.show()
        }
        else {
            message.value = "Error, terjadi kesalahan yang tidak diketahui"
        }
    }
}