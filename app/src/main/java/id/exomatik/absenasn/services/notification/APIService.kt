package id.exomatik.absenasn.services.notification

import id.exomatik.absenasn.services.notification.model.MyResponse
import id.exomatik.absenasn.services.notification.model.Sender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers("Content-Type:application/json", "Authorization:key=AAAAT7W9i_Q:APA91bFBt3xcIc_3ybBLbXJ3PZA1ljYzUgOuJIrupejzGmzmYhSn0wAQFNMPTXDdap46q7EbkMhd7SLCcz7m2CGfipoUrzGbnfbOSHyU-FiMKoOtEI_J956EDcCTCtkWcstCoQ1q7xJJ")
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse?>?
}