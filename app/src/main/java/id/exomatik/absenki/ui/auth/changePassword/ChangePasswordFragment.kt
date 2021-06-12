package id.exomatik.absenki.ui.auth.changePassword

import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.databinding.FragmentChangePasswordBinding
import id.exomatik.absenki.utils.Constant

class ChangePasswordFragment : BaseFragmentBind<FragmentChangePasswordBinding>(){
    override fun getLayoutResource(): Int = R.layout.fragment_change_password
    lateinit var viewModel: ChangePasswordViewModel

    override fun myCodeHere() {
        init()
        onClick()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel = ChangePasswordViewModel(
            activity, bind.etPassword, bind.etConfirmPassword, findNavController()
        )
        bind.viewModel = viewModel
        viewModel.dataUser = this.arguments?.getParcelable(Constant.reffUser)
    }

    private fun onClick(){
        bind.etConfirmPassword.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onClickSave()
                return@OnEditorActionListener false
            }
            false
        })
    }
}