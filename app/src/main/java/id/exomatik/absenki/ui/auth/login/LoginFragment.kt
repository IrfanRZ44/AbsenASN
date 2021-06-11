package id.exomatik.absenki.ui.auth.login

import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import id.exomatik.absenki.R
import id.exomatik.absenki.databinding.FragmentLoginBinding
import id.exomatik.absenki.base.BaseFragmentBind

class LoginFragment : BaseFragmentBind<FragmentLoginBinding>(){
    override fun getLayoutResource(): Int = R.layout.fragment_login
    lateinit var viewModel: LoginViewModel

    override fun myCodeHere() {
        supportActionBar?.hide()
        bind.lifecycleOwner = this
        viewModel = LoginViewModel(findNavController(), savedData, activity, bind.etUsername, bind.etPassword)
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