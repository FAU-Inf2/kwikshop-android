package de.fau.cs.mad.kwikshop.android.model.synchronization.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {

    private StubAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new StubAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
