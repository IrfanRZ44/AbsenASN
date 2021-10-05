package id.exomatik.absenasn.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.ui.auth.SplashActivity
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.FirebaseUtils
import id.exomatik.absenasn.utils.onClickFoto
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave
    val dataUser = MutableLiveData<ModelUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account)
        myCodeHere()
    }

    private fun myCodeHere() {
        supportActionBar?.hide()

        dataUser.value = savedData.getDataUser()

        imgFoto.setOnClickListener {
            savedData.getDataUser()?.fotoProfil?.let { it1 -> onClickFoto(it1, this) }
        }
    }

    private fun deleteToken(dataUser: ModelUser) {
        progress.visibility = View.VISIBLE

        val onCompleteListener = OnCompleteListener<Void> {
            progress.visibility = View.GONE
            if (it.isSuccessful){
                Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_LONG).show()

                savedData.setDataObject(ModelUser(), Constant.reffUser)

                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this, it.exception?.message?:"Gagal Keluar", Toast.LENGTH_LONG).show()
            }
        }
        val onFailureListener = OnFailureListener {
            progress.visibility = View.GONE
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
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
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${packageName}")
            )
        )
    }

    fun onClickProfil(){
        navController.navigate(R.id.aboutFragment)
    }

    fun onClickLogout(){
        val alert = AlertDialog.Builder(this)
        alert.setTitle(Constant.attention)
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
}