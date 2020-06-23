package dv.dimonvideo.dvadmin;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;



public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        Log.v("DVPic", "!!!! ====== NOTICE ======== !!!! ");

    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("DVPIC", "Refreshed token: " + token);

    }

}
