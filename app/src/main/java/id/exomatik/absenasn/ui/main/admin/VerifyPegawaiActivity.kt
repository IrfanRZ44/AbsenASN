package id.exomatik.absenasn.ui.main.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.FirebaseUtils
import kotlinx.android.synthetic.main.activity_verify_pegawai.*

class VerifyPegawaiActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave
    val listData = ArrayList<ModelUser>()
    var adapter: AdapterDaftarPegawai? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_verify_pegawai)
        myCodeHere()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Verifikasi Pegawai"
        supportActionBar?.show()

        initAdapter()

        getDaftarPegawai()

        swipeRefresh.setOnRefreshListener {
            listData.clear()
            adapter?.notifyDataSetChanged()
            swipeRefresh.isRefreshing = false
            getDaftarPegawai()
        }
    }

    private fun initAdapter() {
        adapter = AdapterDaftarPegawai(
            listData, this,
        { dataData: ModelUser, position: Int -> onClickItemAccept(dataData, position) },
        { dataData: ModelUser, position: Int -> onClickItemRejected(dataData, position) })
        rcRequest.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rcRequest.adapter = adapter
        rcRequest.isNestedScrollingEnabled = false
    }

    private fun cekList() {
        progress.visibility = View.GONE

        if (listData.size == 0) textStatus.text = Constant.noDataPegawai
        else textStatus.text = ""
    }

    @SuppressLint("SetTextI18n")
    private fun onClickItemAccept(item: ModelUser, position: Int) {
        updateStatus(item.username, position, Constant.statusActive)
    }

    @SuppressLint("SetTextI18n")
    private fun onClickItemRejected(item: ModelUser, position: Int) {
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Mohon masukkan alasan penolakan :")

        val editText = EditText(this)
        val linearLayout = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        linearLayout.setPadding(20, 0, 20, 0)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = LinearLayout.VERTICAL
        editText.setLines(5)
        editText.minLines = 5

        linearLayout.addView(editText)
        alert.setView(linearLayout)

        alert.setPositiveButton(
            Constant.kirim
        ) { dialog, _ ->
            val comment = editText.text.toString()
            dialog.dismiss()
            if (comment.isNotEmpty()){
                updateComment(item.username, position, comment)
            }
            else{
                textStatus.text = "Error, mohon masukkan alasan penolakan"
            }
        }
        alert.setNegativeButton(
            Constant.batal
        ) { dialog, _ ->
            dialog.dismiss()
        }

        alert.show()
    }

    @SuppressLint("SetTextI18n")
    private fun updateStatus(username: String, position: Int, stats: String){
        progress.visibility = View.VISIBLE

        val onCompleteListener = OnCompleteListener<Void> { result ->
            progress.visibility = View.GONE
            if (result.isSuccessful) {
                textStatus.text = "Berhasil menerima permintaan"

                listData.removeAt(position)
                adapter?.notifyItemRemoved(position)
                adapter?.notifyDataSetChanged()

                cekList()
            } else {
                textStatus.text = "Gagal menerima permintaan"
            }
        }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.setValueWith2ChildString(Constant.reffUser,
            username, Constant.status,
            stats, onCompleteListener, onFailureListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateComment(username: String, position: Int, comment: String){
        progress.visibility = View.VISIBLE


        val onCompleteListener = OnCompleteListener<Void> { result ->
            progress.visibility = View.GONE
            if (result.isSuccessful) {
                updateStatus(username, position, Constant.statusRejected)
            } else {
                textStatus.text = "Gagal menerima permintaan"
            }
        }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.setValueWith2ChildString(Constant.reffUser,
            username, Constant.comment,
            comment, onCompleteListener, onFailureListener
        )
    }

    private fun getDaftarPegawai() {
        progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                textStatus.text = result.message
                progress.visibility = View.GONE
            }

            override fun onDataChange(result: DataSnapshot) {
                progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelUser::class.java)

                        if (data != null){
                            listData.add(data)
                            adapter?.notifyDataSetChanged()
                        }
                    }

                    cekList()
                }
                else{
                    textStatus.text = Constant.noDataPegawai
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.status, Constant.statusRequest, valueEventListener
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}