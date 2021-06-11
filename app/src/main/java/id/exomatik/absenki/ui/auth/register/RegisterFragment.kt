package id.exomatik.absenki.ui.auth.register

import android.app.Activity
import android.content.Intent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import androidx.navigation.fragment.findNavController
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.exomatik.absenki.databinding.FragmentRegisterBinding
import id.exomatik.absenki.model.ModelUser
import id.exomatik.absenki.utils.Constant

class RegisterFragment : BaseFragmentBind<FragmentRegisterBinding>(){
    override fun getLayoutResource(): Int = R.layout.fragment_register
    lateinit var viewModel: RegisterViewModel

    override fun myCodeHere() {
        bind.lifecycleOwner = this
        viewModel = RegisterViewModel(activity, savedData, findNavController(),
            bind.etUsername, bind.etPassword, bind.etConfirmPassword, bind.etNamaLengkap,
            bind.etAlamat, bind.etTempatLahir, bind.etTglLahir, bind.etNoHpPemilik, bind.rgJK)
        bind.viewModel = viewModel

        val dataUser = this.arguments?.getParcelable<ModelUser>(Constant.reffUser)
        if (dataUser != null){
            viewModel.setDataUser(
                this.arguments?.getParcelable(Constant.reffFotoUser), dataUser
            )
        }

        onClick()
    }

    private fun onClick() {
        bind.etNoHpPemilik.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onClickRegister()
                return@OnEditorActionListener false
            }
            false
        })

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

            if (resultCode == Activity.RESULT_OK){
                val imageUri = result.uri
                viewModel.etFotoProfil.value = imageUri
            }
        }
    }
}