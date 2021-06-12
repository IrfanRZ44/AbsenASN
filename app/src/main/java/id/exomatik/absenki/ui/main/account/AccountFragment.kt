package id.exomatik.absenki.ui.main.account

import androidx.navigation.fragment.findNavController
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.databinding.FragmentAccountBinding
import id.exomatik.absenki.utils.onClickFoto

class AccountFragment : BaseFragmentBind<FragmentAccountBinding>() {
    override fun getLayoutResource(): Int = R.layout.fragment_account
    lateinit var viewModel: AccountViewModel

    override fun myCodeHere() {
        supportActionBar?.hide()
        init()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel =
            AccountViewModel(
                savedData,
                activity,
                findNavController(),
                context
            )
        bind.viewModel = viewModel
        viewModel.dataUser.value = savedData.getDataUser()

        bind.imgFoto.setOnClickListener {
            savedData.getDataUser()?.fotoProfil?.let { it1 -> onClickFoto(it1, findNavController()) }
        }
    }
}