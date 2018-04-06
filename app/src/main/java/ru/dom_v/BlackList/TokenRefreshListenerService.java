package ru.dom_v.BlackList;


import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by user on 28.11.2017.
 */

public class TokenRefreshListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent i = new Intent(this, RegistrationService.class);
        startService(i);
    }
}
