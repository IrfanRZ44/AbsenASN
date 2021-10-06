package id.exomatik.absenasn.ui.main.account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.ui.auth.SplashActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment() {
    private lateinit var savedData: DataSave
    private var savedInstanceState: Bundle? = null
    private lateinit var v : View

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_account, paramViewGroup, false)

        savedInstanceState = paramBundle
        savedData = DataSave(context)
        setHasOptionsMenu(true)
        myCodeHere()
        onClick()

        return v
    }

    private fun myCodeHere() {
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.hide()
    }

    private fun onClick(){
        imgFoto.setOnClickListener {
            savedData.getDataUser()?.fotoProfil?.let { it1 -> activity?.let { it2 ->
                onClickFoto(it1,
                    it2
                )
            } }
        }

        btnEditProfil.setOnClickListener {
            val intent = Intent(activity, EditProfilActivity::class.java)
            startActivity(intent)
        }

        btnEditPassword.setOnClickListener {
            val intent = Intent(activity, EditPasswordActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }

        btnRate.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${activity?.packageName}")
                )
            )
        }

        btnAccount.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            activity?.startActivity(intent)
        }

        btnLogout.setOnClickListener {
            onClickLogout()
        }
    }

    private fun deleteToken(dataUser: ModelUser) {
        progress.visibility = View.VISIBLE

        val onCompleteListener = OnCompleteListener<Void> {
            progress.visibility = View.GONE
            if (it.isSuccessful){
                Toast.makeText(activity, "Berhasil Keluar", Toast.LENGTH_LONG).show()

                savedData.setDataObject(ModelUser(), Constant.reffUser)

                val intent = Intent(activity, SplashActivity::class.java)
                activity?.startActivity(intent)
                activity?.finish()
            }
            else{
                Toast.makeText(activity, it.exception?.message?:"Gagal Keluar", Toast.LENGTH_LONG).show()
            }
        }
        val onFailureListener = OnFailureListener {
            progress.visibility = View.GONE
            Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
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

    private fun onClickLogout(){
        val act = activity

        if (act != null){
            val alert = AlertDialog.Builder(act)
            alert.setTitle(Constant.attention)
            alert.setMessage(Constant.alertLogout)
            alert.setPositiveButton(
                Constant.iya
            ) { _, _ ->
                val dataUser = savedData.getDataUser()
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
}