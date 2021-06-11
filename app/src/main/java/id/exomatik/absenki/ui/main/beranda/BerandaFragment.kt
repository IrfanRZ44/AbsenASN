package id.exomatik.absenki.ui.main.beranda

import android.app.SearchManager
import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import androidx.navigation.fragment.findNavController
import id.exomatik.absenki.R
import id.exomatik.absenki.base.BaseFragmentBind
import id.exomatik.absenki.databinding.FragmentBerandaBinding
import id.exomatik.absenki.model.ModelBarang

class BerandaFragment : BaseFragmentBind<FragmentBerandaBinding>() {
    private lateinit var viewModel: BerandaViewModel
    private lateinit var searchView : SearchView
    private var queryTextListener : SearchView.OnQueryTextListener? = null
    private var onCloseListener : SearchView.OnCloseListener? = null

    override fun getLayoutResource(): Int = R.layout.fragment_beranda

    override fun myCodeHere() {
        setHasOptionsMenu(true)
        init()
    }

    private fun init() {
        bind.lifecycleOwner = this
        viewModel = BerandaViewModel(bind.rvData, context, findNavController())
        bind.viewModel = viewModel
        viewModel.initAdapter()

        bind.btnCreate.setOnClickListener {
            viewModel.createBarang(ModelBarang(0, "Teh Dingin", "Minuman", 3000))
        }

        bind.btnGet.setOnClickListener {
            viewModel.getBarang()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_search_and_notif, menu)

//        val searchItem = menu.findItem(R.id.actionSearch)
//        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
//
//        searchView = searchItem.actionView as SearchView
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(context.componentName))
//
//        queryTextListener = object : SearchView.OnQueryTextListener {
//            override fun onQueryTextChange(newText: String): Boolean {
//
//                return true
//            }
//
//            override fun onQueryTextSubmit(query: String): Boolean {
//                viewModel.listData.clear()
//                viewModel.adapter?.notifyDataSetChanged()
//
//                for (i in viewModel.listNama.indices){
//                    if (viewModel.listNama[i].nama.contains(query)){
//                        viewModel.listData.add(viewModel.listNama[i])
//                        viewModel.adapter?.notifyDataSetChanged()
//                    }
//                }
//
//                viewModel.cekList()
//                return true
//            }
//        }
//
//        onCloseListener = SearchView.OnCloseListener {
//            viewModel.listData.clear()
//            viewModel.adapter?.notifyDataSetChanged()
//
//            for (i in viewModel.listDataSearch.indices){
//                viewModel.listData.add(viewModel.listDataSearch[i])
//                viewModel.adapter?.notifyDataSetChanged()
//            }
//            viewModel.cekList()
//            false
//        }
//
//        searchView.setOnQueryTextListener(queryTextListener)
//        searchView.setOnCloseListener(onCloseListener)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.actionSearch ->{
                return false
            }
            R.id.actionNotif ->{
                viewModel.message.value = "Notif"
            }
        }

        searchView.setOnQueryTextListener(queryTextListener)
        searchView.setOnCloseListener(onCloseListener)
        return super.onOptionsItemSelected(item)
    }

}

