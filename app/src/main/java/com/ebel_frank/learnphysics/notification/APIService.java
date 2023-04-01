package com.ebel_frank.learnphysics.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAIZgZX58:APA91bHS0EJHHdBvNsufGr3Bk4tWZVVUrh2L_1kNQTaseo3sAmbPrZTkHw_nh6pVR3mwubygFMs91NaWBaUZOjH9IqO_QI4sWM9X5-7DGtb2thnGgOln0zXxIu6vFyvycE-xdRVmOrYl"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
