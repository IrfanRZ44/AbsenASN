package id.exomatik.absenki.ui.auth.forgetPassword

import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.databinding.FragmentForgetPasswordBinding

class ForgetPasswordFragment : BaseFragmentBind<FragmentForgetPasswordBinding>(){
    override fun getLayoutResource(): Int = R.layout.fragment_forget_password
    lateinit var viewModel: ForgetPasswordViewModel

    override fun myCodeHere() {
        supportActionBar?.hide()
        bind.lifecycleOwner = this
        viewModel = ForgetPasswordViewModel(findNavController(), activity, bind.etUsername)
        bind.viewModel = viewModel

        onClick()
    }

    private fun onClick() {
        bind.etUsername.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onClickLogin()
                return@OnEditorActionListener false
            }
            false
        })
    }
}