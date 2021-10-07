package id.exomatik.absenasn.ui.main.beranda

//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuInflater
//import android.view.MenuItem
//import androidx.navigation.fragment.findNavController
//import androidx.viewpager.widget.ViewPager
//import coil.load
//import coil.request.CachePolicy
//import coil.transform.CircleCropTransformation
//import id.exomatik.absenasn.R
//import id.exomatik.absenasn.base.BaseFragmentBind
//import id.exomatik.absenasn.utils.Constant
//import id.exomatik.absenasn.utils.adapter.SectionsPagerAdapter
//import id.exomatik.absenasn.utils.getDateNow
//import id.exomatik.absenasn.utils.onClickFoto
//
//class BerandaFragment : BaseFragmentBind<FragmentBerandaBinding>() {
//    override fun getLayoutResource(): Int = R.layout.fragment_admin
//
//    override fun myCodeHere() {
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.title = "Detail"
//        supportActionBar?.show()
//        init()
//        onClick()
//    }
//
//    private fun init() {
//        bind.lifecycleOwner = this
//
//        bind.imgFoto.setOnClickListener {
//            savedData.getDataUser()?.fotoProfil?.let { it1 -> onClickFoto(it1, findNavController()) }
//        }
//    }
//
//    private fun setData(){
//        bind.imgFoto.load(R.drawable.ic_logo) {
//            crossfade(true)
//            placeholder(R.drawable.ic_camera_white)
//            transformations(CircleCropTransformation())
//            error(R.drawable.ic_camera_white)
//            fallback(R.drawable.ic_camera_white)
//            memoryCachePolicy(CachePolicy.ENABLED)
//        }
//        bind.imgFoto.setBackgroundResource(android.R.color.transparent)
//
//        nama.value = "UIN Alauddin Makassar"
//        kodeInstansi.value = getDateNow(Constant.dateFormat2)
//        deskripsi.value = "Samata, Gowa"
//    }
//
//    private fun onClick(){
//    }
//
//    @Suppress("DEPRECATION")
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        setupViewPager(bind.viewPager)
//        bind.tabs.setupWithViewPager(bind.viewPager)
//    }
//
//    private fun setupViewPager(pager: ViewPager) {
//        val adapter = SectionsPagerAdapter(childFragmentManager)
//
//        if (viewModel.dataInstansi.value?.username_owner == savedData.getDataUser()?.username){
//            setHasOptionsMenu(true)
//            adapter.addFragment(SudahAbsenFragment(viewModel.dataInstansi.value), "Sudah Absen")
//            adapter.addFragment(BelumAbsenFragment(viewModel.dataInstansi.value), "Belum Absen")
//            adapter.addFragment(HariKerjaFragment(viewModel.dataInstansi.value), "Hari Kerja")
//            adapter.addFragment(RiwayatFragment(viewModel.dataInstansi.value), "Riwayat")
//            adapter.addFragment(DaftarPegawaiFragment(viewModel.dataInstansi.value), "Pegawai")
//        }
//        else{
//            setHasOptionsMenu(false)
//            adapter.addFragment(AbsensiFragment(viewModel.dataInstansi.value), "Absensi")
//            adapter.addFragment(RiwayatFragment(viewModel.dataInstansi.value), "Riwayat")
//        }
//
//        pager.adapter = adapter
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.toolbar_notif, menu)
//
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.actionNotif ->{
//                val bundle = Bundle()
//                val fragmentTujuan = NotifInstansiFragment()
//                bundle.putParcelable(Constant.reffItemInstansi, viewModel.dataInstansi.value)
//                fragmentTujuan.arguments = bundle
//
//                findNavController().navigate(R.id.notifInstansiFragment, bundle)
//                return false
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }
//}