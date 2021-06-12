package id.exomatik.absenki.ui.main.account.editPassword

import android.view.inputmethod.EditorInfo
import android.widget.TextView
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.databinding.FragmentEditPasswordBinding

class EditPasswordFragment : BaseFragmentBind<FragmentEditPasswordBinding>(){
    override fun getLayoutResource(): Int = R.layout.fragment_edit_password
    lateinit var viewModel: EditPasswordViewModel

    override fun myCodeHere() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Password"
        supportActionBar?.show()
        init()
        onClick()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel = EditPasswordViewModel(
            savedData, activity, bind.etPasswordOld,
            bind.etPasswordNew, bind.etConfirmPasswordNew
        )
        bind.viewModel = viewModel
    }

    private fun onClick(){
        bind.etConfirmPasswordNew.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onClickEditPassword()
                return@OnEditorActionListener false
            }
            false
        })
    }
}