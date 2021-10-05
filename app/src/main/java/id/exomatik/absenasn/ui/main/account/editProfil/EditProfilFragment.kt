package id.exomatik.absenasn.ui.main.account.editProfil

import android.app.Activity
import android.content.Intent
import id.exomatik.absenasn.R
import id.exomatik.absenasn.base.BaseFragmentBind
import androidx.navigation.fragment.findNavController
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.exomatik.absenasn.databinding.FragmentEditProfilBinding

class EditProfilFragment : BaseFragmentBind<FragmentEditProfilBinding>(){
    override fun getLayoutResource(): Int = R.layout.fragment_edit_profil
    lateinit var viewModel: EditProfilViewModel

    override fun myCodeHere() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profil"
        supportActionBar?.show()
        bind.lifecycleOwner = this
        viewModel = EditProfilViewModel(activity, savedData, findNavController(),
            bind.etNamaLengkap, bind.etAlamat, bind.etTempatLahir, bind.etTglLahir, bind.rgJK)
        bind.viewModel = viewModel

        val dataUser = savedData.getDataUser()
        if (dataUser != null){
            viewModel.setDataUser(dataUser)
        }

        onClick()
    }

    private fun onClick() {
        bind.cardFotoProfil.setOnClickListener {
            context?.let {
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAllowFlipping(true)
                    .setAllowRotation(true)
                    .setAspectRatio(1, 1)
                    .start(it, this)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            val username = savedData.getDataUser()?.username

            if (resultCode == Activity.RESULT_OK && !username.isNullOrEmpty()){
                val imageUri = result.uri
                viewModel.etFotoProfil.value = imageUri
                viewModel.saveFoto(imageUri, username)
            }
        }
    }
}