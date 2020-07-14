package com.exomatik.ballighadmin.ui.main.pesan.adminToMB

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.exomatik.ballighadmin.R
import com.exomatik.ballighadmin.base.BaseViewModel
import com.exomatik.ballighadmin.model.ModelChat
import com.exomatik.ballighadmin.model.ModelDataChat
import com.exomatik.ballighadmin.model.ModelNotif
import com.exomatik.ballighadmin.model.ModelUser
import com.exomatik.ballighadmin.services.notification.model.Notification
import com.exomatik.ballighadmin.services.notification.model.Sender
import com.exomatik.ballighadmin.ui.main.profile.mb.AdminLihatProfileMBFragment
import com.exomatik.ballighadmin.utils.Constant
import com.exomatik.ballighadmin.utils.Constant.admin
import com.exomatik.ballighadmin.utils.Constant.chatMBtoAdmin
import com.exomatik.ballighadmin.utils.Constant.hapus_pesan
import com.exomatik.ballighadmin.utils.Constant.iya
import com.exomatik.ballighadmin.utils.Constant.noMessage
import com.exomatik.ballighadmin.utils.Constant.pending
import com.exomatik.ballighadmin.utils.Constant.read
import com.exomatik.ballighadmin.utils.Constant.referenceChat
import com.exomatik.ballighadmin.utils.Constant.referenceUser
import com.exomatik.ballighadmin.utils.Constant.sended
import com.exomatik.ballighadmin.utils.Constant.tidak
import com.exomatik.ballighadmin.utils.Constant.unread
import com.exomatik.ballighadmin.utils.FirebaseUtils
import com.exomatik.ballighadmin.utils.dismissKeyboard
import com.exomatik.ballighadmin.utils.onClickFoto
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*

class PesanAdmintoMBViewModel(private val rcChat: RecyclerView,
                              private val context: Context?,
                              private val activity: Activity?,
                              private val navController: NavController
) : BaseViewModel() {
    val dataUserMuballigh = MutableLiveData<ModelUser>()
    val chat = MutableLiveData<String>()
    val listChat = ArrayList<ModelChat?>()
    val urlFoto = MutableLiveData<Uri>()
    private var adapter: AdapterPesanAdmintoMB? = null
    val isShowFoto = MutableLiveData<Boolean>()
    var idChat = ""

    @Suppress("DEPRECATION")
    fun setData() {
        activity?.customeToolbar?.visibility = View.VISIBLE
        activity?.btnBack?.setOnClickListener {
            navController.popBackStack()
        }
        activity?.rlInfo?.setOnClickListener {
            val bundle = Bundle()
            val cariFragment = AdminLihatProfileMBFragment()
            bundle.putParcelable("dataUser", dataUserMuballigh.value)
            cariFragment.arguments = bundle
            val navOption = NavOptions.Builder().setPopUpTo(R.id.adminLihatProfileMBFragment, true).build()
            navController.navigate(R.id.adminLihatProfileMBFragment, bundle, navOption)
            dismissKeyboard(activity)
        }
        activity?.actionBarName?.text = dataUserMuballigh.value?.nama
        if (dataUserMuballigh.value?.status == Constant.online) {
            context?.resources?.getColor(R.color.white)?.let {
                activity?.actionBarStatus?.setTextColor(
                    it
                )
            }
            activity?.actionBarStatus?.text = Constant.online
        }
        else {
            context?.resources?.getColor(R.color.gray11)?.let {
                activity?.actionBarStatus?.setTextColor(
                    it
                )
            }
            activity?.actionBarStatus?.text = Constant.offline
        }
        activity?.contact_photo?.load(dataUserMuballigh.value?.foto) {
            crossfade(true)
            placeholder(R.drawable.ic_logo)
            transformations(CircleCropTransformation())
            error(R.drawable.ic_logo)
            fallback(R.drawable.ic_logo)
            memoryCachePolicy(CachePolicy.ENABLED)
        }
        activity?.contact_photo?.setOnClickListener {
            onClickFoto(dataUserMuballigh.value?.foto?:"", navController)
        }
    }

    fun initAdapter() {
        adapter = AdapterPesanAdmintoMB(listChat,
                { item: ModelChat, rlItem: RelativeLayout -> alertHapus(item, rlItem) },
                context)
        val layoutMgr = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        layoutMgr.stackFromEnd = true
        rcChat.layoutManager = layoutMgr
        rcChat.adapter = adapter
    }

    fun refreshDataUser(username: String) {
        isShowLoading.value = true
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                isShowLoading.value = false
                message.value = result.message
            }

            override fun onDataChange(result: DataSnapshot) {
                isShowLoading.value = false
                if (result.exists()) {
                    dataUserMuballigh.value = result.getValue(ModelUser::class.java)
                    setData()
                } else {
                    message.value = "Afwan, terjadi kesalahan database"
                }
            }
        }

        FirebaseUtils.refreshDataUserChatWith3Child(
            referenceUser,
            username,
            valueEventListener
        )
    }

    fun refreshDataChat() {
        isShowLoading.value = true

        val childEvent = object : ChildEventListener {
            override fun onChildAdded(result: DataSnapshot, key: String?) {
                isShowLoading.value = false
                if (result.exists()) {
                    status.value = ""
                    val data = result.getValue(ModelChat::class.java)

                    var chatBelumAda = true
                    for (a in listChat.indices) {
                        if (data?.timeStamp == listChat[a]?.timeStamp) {
                            chatBelumAda = false
                        }
                    }

                    if (chatBelumAda) {
                        listChat.add(data)
                        adapter?.notifyDataSetChanged()
                        rcChat.smoothScrollToPosition(listChat.size)
                    }

                    if (data?.senderId == admin) {
                        if (data.status == pending) {
                            setStatus(data, sended)
                        }
                        if (data.status == sended) {
                            setStatus(data, unread)
                        }
                    } else {
                        if (data?.status == unread) {
                            setStatus(data, read)
                        }
                    }
                }
            }

            override fun onChildMoved(result: DataSnapshot, key: String?) {

            }

            override fun onChildChanged(result: DataSnapshot, key: String?) {
                isShowLoading.value = false
                if (result.exists()) {
                    status.value = ""
                    val data = result.getValue(ModelChat::class.java)

                    for (a in listChat.indices) {
                        if (data?.timeStamp == listChat[a]?.timeStamp) {
                            listChat[a] = data
                            adapter?.notifyItemChanged(a)
                            rcChat.smoothScrollToPosition(listChat.size)
                        }
                    }

                    if (data?.senderId == admin) {
                        if (data.status == pending) {
                            setStatus(data, sended)
                        }
                        if (data.status == sended) {
                            setStatus(data, unread)
                        }
                    } else {
                        if (data?.status == unread) {
                            setStatus(data, read)
                        }
                    }
                }
            }

            override fun onChildRemoved(result: DataSnapshot) {
                isShowLoading.value = false
                if (result.exists()) {
                    status.value = ""
                    val data = result.getValue(ModelChat::class.java)

                    var doLoop = true
                    var a = 0
                    while(doLoop) {
                        if (data?.timeStamp == listChat[a]?.timeStamp) {
                            listChat.removeAt(a)
                            adapter?.notifyItemRemoved(a)
                            doLoop = false
                        }
                        a++
                    }
                }
            }

            override fun onCancelled(result: DatabaseError) {
                isShowLoading.value = false
                if (listChat.size == 0) status.value = noMessage
            }

        }

        isShowLoading.value = false
        FirebaseUtils.refreshDataChatWith2Child(
            referenceChat,
            chatMBtoAdmin,
            idChat,
            childEvent
        )
    }

    fun onClickSend() {
        val msg = chat.value
        val foto = urlFoto.value
        val timeStamp = System.currentTimeMillis()

        val dbChat = ModelDataChat(
            dataUserMuballigh.value?.username ?: "",
            admin,
            idChat, msg?:"", timeStamp
        )

        val dataChat = ModelChat(
            admin,
            dataUserMuballigh.value?.username ?: "", msg?:"",
            "", "",dataUserMuballigh.value?.username ?: "",
            admin,
            idChat,
            timeStamp, pending
        )

        listChat.add(dataChat)
        adapter?.notifyDataSetChanged()
        rcChat.smoothScrollToPosition(listChat.size)

        val onCompleteListener = OnCompleteListener<Void> {
            makeMessage(msg, foto, dataChat)
        }

        val onFailureListener = OnFailureListener {
            makeMessage(msg, foto, dataChat)
        }

        FirebaseUtils.setValueWith2ChildObject(
            Constant.referenceRuangChat, chatMBtoAdmin, dbChat.idChat,
            dbChat, onCompleteListener, onFailureListener
        )

        chat.value = ""
        urlFoto.value = null
        isShowFoto.value = false
    }

    private fun makeMessage(msg: String?, foto: Uri?, dataChat: ModelChat){
        if (!msg.isNullOrEmpty() || foto != null) {
            if (foto == null){
                sendChat(dataChat)
            }
            else{
                saveFoto(foto, dataChat)
            }
        }
    }

    private fun alertHapus(dataChat: ModelChat, rlItem: RelativeLayout) {
        try {
            val alert =
                AlertDialog.Builder(context ?: throw Exception("Afwan, mohon muat ulang aplikasi"))
            alert.setMessage(hapus_pesan)
            alert.setPositiveButton(
                iya
            ) { dialog, _ ->
                deleteChat(dataChat)
                rlItem.setBackgroundResource(android.R.color.transparent)
                dialog.dismiss()
            }
            alert.setNegativeButton(
                tidak
            ) { dialog, _ -> dialog.dismiss()
                rlItem.setBackgroundResource(android.R.color.transparent)
            }

            alert.show()
        } catch (e: Exception) {
            rlItem.setBackgroundResource(android.R.color.transparent)
            message.value = e.message
        }
    }

    private fun saveFoto(image : Uri, dataChat: ModelChat){
        isShowLoading.value = true
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            isShowLoading.value = false
            getUrlFoto(it, dataChat)
        }

        val onFailureListener = OnFailureListener {
            message.value = it.message
            isShowLoading.value = false
        }

        FirebaseUtils.simpanFoto3Child(
            referenceChat, chatMBtoAdmin, dataChat.idChat, dataChat.timeStamp.toString(),
            image, onSuccessListener, onFailureListener)
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot, dataChat: ModelChat) {
        isShowLoading.value = true
        val onSuccessListener = OnSuccessListener<Uri?>{
            isShowLoading.value = false
            dataChat.urlFoto = it.toString()
            sendChat(dataChat)
        }

        val onFailureListener = OnFailureListener {
            message.value = it.message
            isShowLoading.value = false
        }

        FirebaseUtils.getUrlFoto(uploadTask, onSuccessListener, onFailureListener)
    }

    private fun sendChat(dataChat: ModelChat) {
        val onCompleteListener = OnCompleteListener<Void> {
        }

        val onFailureListener = OnFailureListener {}

        FirebaseUtils.setValueWith3ChildObject(
            referenceChat, chatMBtoAdmin, dataChat.idChat,
            dataChat.timeStamp.toString(), dataChat,
            onCompleteListener, onFailureListener
        )
    }

    private fun setStatus(dataChat: ModelChat, status: String) {
        val onCompleteListener = OnCompleteListener<Void> {
            if (status == sended) {
                val notification = Notification(
                    "$admin : ${dataChat.message}",
                    "Pesan"
                    , "com.exomatik.balligh.fcm_TARGET_NOTIFICATION_PESAN_MB_Admin"
                )

                val token = dataUserMuballigh.value?.token ?: ""
                if (token.isEmpty()) {
                    FirebaseUtils.simpanNotif(
                        ModelNotif(
                            notification,
                            dataUserMuballigh.value?.idMuballigh,
                            ""
                        )
                    )
                } else {
                    val sender = Sender(notification, token)
                    FirebaseUtils.sendNotif(sender)
                }
            }
        }

        val onFailureListener = OnFailureListener {}

        FirebaseUtils.setValueWith4ChildString(
            referenceChat, chatMBtoAdmin, dataChat.idChat,
            dataChat.timeStamp.toString(), Constant.status, status,
            onCompleteListener, onFailureListener
        )
    }

    private fun deleteChat(dataChat: ModelChat) {
        isShowLoading.value = true

        val onCompleteListener = OnCompleteListener<Void> {
            isShowLoading.value = false
            if (it.isSuccessful) {
                status.value = ""

                if (dataChat.urlFoto.isNotEmpty()) FirebaseUtils.deleteUrlFoto(dataChat.urlFoto)
                checkLastMessage(dataChat)
            }
        }

        val onFailureListener = OnFailureListener {
            isShowLoading.value = false
        }

        FirebaseUtils.deleteValueWith3Child(
            referenceChat,
            chatMBtoAdmin,
            dataChat.idChat,
            dataChat.timeStamp.toString(),
            onCompleteListener,
            onFailureListener
        )
    }

    fun onClickCancelFoto(){
        isShowFoto.value = false
        urlFoto.value = null
    }

    private fun checkLastMessage(dataChat: ModelChat) {
        isShowLoading.value = true
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                isShowLoading.value = false
            }

            override fun onDataChange(result: DataSnapshot) {
                isShowLoading.value = false
                if (result.exists()) {
                    val data = result.getValue(ModelDataChat::class.java)

                    if (data?.timeStamp == dataChat.timeStamp){
                        emptyMessage(dataChat)
                    }
                }
            }
        }
        FirebaseUtils.getData2Child(
            Constant.referenceRuangChat,
            chatMBtoAdmin, dataChat.idChat, valueEventListener)
    }

    private fun emptyMessage(dataChat: ModelChat) {
        isShowLoading.value = true
        val onCompleteListener = OnCompleteListener<Void> {
            isShowLoading.value = false
        }

        val onFailureListener = OnFailureListener {
            isShowLoading.value = false
        }

        FirebaseUtils.setValueWith3ChildString(
            Constant.referenceRuangChat, chatMBtoAdmin, dataChat.idChat,
            Constant.message, "",
            onCompleteListener, onFailureListener
        )
    }
}