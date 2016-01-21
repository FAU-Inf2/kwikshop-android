package de.fau.cs.mad.kwikshop.android.view;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.glassfish.jersey.client.proxy.WebResourceFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.BuildConfig;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.model.messages.LoginEvent;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.common.rest.UserResource;
import de.greenrobot.event.EventBus;

public class LoginActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "LoginActivity";



    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 0;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    /* Is there a connection attempt to the backend in progress? */
    private boolean mIsConnecting = false;

    private ProgressDialog progressDialog;

    /* UI elements */
    @InjectView(R.id.login_sign_in_button)
    SignInButton login_sign_in_button;

    @InjectView(R.id.login_sign_out_button)
    Button login_sign_out_button;

    @InjectView(R.id.login_skip_button)
    Button login_skip_button;

    @InjectView(R.id.login_back_button)
    Button login_back_button;

    @InjectView(R.id.login_retry_button)
    Button login_retry_button;

    @InjectView(R.id.login_debug_login)
    Button login_debug_login;

    @InjectView(R.id.login_status)
    TextView mStatus;

    @InjectView(R.id.login_debug_status)
    TextView mDebugStatus;

    @InjectView(R.id.login_logo)
    ImageView login_logo;

    @InjectView(R.id.login_help)
    TextView mHelpText;


    public static Intent getIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    /**
     * Determines if the login activity is supposed to be skipped
     * Also used by ErrorReportingActivity to go directly to the following activity
     */
    public static boolean skipActivity(Context context) {

        Context applicationContext = context.getApplicationContext();

        return SessionHandler.isAuthenticated(applicationContext) ||
               SharedPreferencesHelper.loadInt(SharedPreferencesHelper.SKIP_LOGIN, 0, applicationContext) == 1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);


        boolean force = false;
        Bundle b = getIntent().getExtras();
        if(b != null) {
             force = b.getBoolean("FORCE", false);
        }

        // If the user is logged in or has skipped the login, go to the main Activity - except if the user opens the Activity from the menu
        if(!force) {
            if (skipActivity(this))
                exitLoginActivity();
        }

        // Restore from saved instance state
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }
        // [END restore_saved_instance_state]

        // Set up button click listeners
        login_sign_in_button.setOnClickListener(this);
        login_sign_out_button.setOnClickListener(this);
        login_skip_button.setOnClickListener(this);
        login_debug_login.setOnClickListener(this);
        login_retry_button.setOnClickListener(this);
        login_back_button.setOnClickListener(this);

        // Large sign-in
        login_sign_in_button.setSize(SignInButton.SIZE_WIDE);

        // [START create_google_api_client]
        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
        // [END create_google_api_client]

    }

    private void updateUI() {

        mDebugStatus.setText(SessionHandler.getSessionUser(getApplicationContext()) + " - " + SessionHandler.getSessionToken(getApplicationContext()));

        login_debug_login.setVisibility(BuildConfig.DEBUG_MODE ? View.VISIBLE : View.GONE);
        mDebugStatus.setVisibility(BuildConfig.DEBUG_MODE ? View.VISIBLE : View.GONE);


        if (mGoogleApiClient.isConnected()) {
            login_sign_in_button.setVisibility(View.GONE);
            login_sign_out_button.setVisibility(View.VISIBLE);

            if(!SessionHandler.isAuthenticated(getApplicationContext())) {
                login_skip_button.setVisibility(View.VISIBLE);
                login_back_button.setVisibility(View.GONE);

                if(!mIsConnecting) {
                    login_retry_button.setVisibility(View.VISIBLE);
                } else {
                    login_retry_button.setVisibility(View.GONE);
                }
            } else {
                login_skip_button.setVisibility(View.GONE);
                login_back_button.setVisibility(View.VISIBLE);

                mStatus.setVisibility(View.VISIBLE);
                mStatus.setText(getString(R.string.kwikshop_login_success));

                if(!SessionHandler.getSessionToken(getApplicationContext()).equals("DEBUG")) {
                    // Show signed-in user's name
                    Person p = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                    if (p != null) {
                        mHelpText.setVisibility(View.GONE);
                        String name = p.getDisplayName();
                        mStatus.setText(getText(R.string.signed_in_fmt) + " " + name);
                    }
                } else {
                    mStatus.setText("DEBUG logged in");
                }

                login_retry_button.setVisibility(View.GONE);
                login_debug_login.setVisibility(View.GONE);

                boolean force = false;
                Bundle b = getIntent().getExtras();
                if(b != null) {
                    force = b.getBoolean("FORCE", false);
                }
                if(!force)
                    exitLoginActivity();
            }

        } else {
            mStatus.setVisibility(View.GONE);
            mHelpText.setVisibility(View.VISIBLE);

            // Set button visibility
            login_sign_in_button.setVisibility(View.VISIBLE);
            login_sign_out_button.setVisibility(View.GONE);
            login_debug_login.setVisibility(BuildConfig.DEBUG_MODE ? View.VISIBLE : View.GONE);
            login_retry_button.setVisibility(View.GONE);
            login_skip_button.setVisibility(View.VISIBLE);
            login_back_button.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    // [START on_start_on_stop]
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    // [END on_start_on_stop]

    // [START on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mIsResolving);
    }
    // [END on_save_instance_state]

    // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }
    // [END on_activity_result]

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);

        mIsConnecting = true;

        if(!SessionHandler.isAuthenticated(getApplicationContext()))
            new GetIdTokenTask().execute(null, null, null);

        // Show the signed-in UI
        updateUI();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    // [START on_connection_failed]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            updateUI();
        }
    }
    // [END on_connection_failed]

    private void showErrorDialog(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
                            updateUI();
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            mShouldResolve = false;
            updateUI();
        }
    }

    /* This task retrieves the user's session token from the server */
    private class GetIdTokenTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            EventBus.getDefault().post(new LoginEvent(LoginEvent.EventType.Started));

            String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
            Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            String scopes = "audience:server:client_id:" + getString(R.string.login_server_client_id);
            String idToken;
            try {
                idToken = GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
            } catch (Exception e) {
                EventBus.getDefault().post(new LoginEvent(LoginEvent.EventType.Failed));
                Log.e(TAG, "Error retrieving ID token.", e);
                return false;
            }

            //TODO: Use RestClientFactoryImplementation if possible
            String authResponse;
            try {
                // Load cert from raw res
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = getResources().openRawResource(R.raw.server);
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                    //System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } finally {
                    caInput.close();
                }

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);

                String uri = getString(R.string.API_PROTOCOL) +
                        SharedPreferencesHelper.loadString(SharedPreferencesHelper.API_ENDPOINT, getString(BuildConfig.DEBUG_MODE ? R.string.API_HOST_DEV : R.string.API_HOST), getApplicationContext()) +
                                ":" + getString(R.string.API_PORT);

                WebTarget target = ClientBuilder.newBuilder()
                        .sslContext(context)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {

                                return (hostname.equals(SharedPreferencesHelper.loadString(SharedPreferencesHelper.API_ENDPOINT, getString(BuildConfig.DEBUG_MODE ? R.string.API_HOST_DEV : R.string.API_HOST), getApplicationContext())));
                            }
                        })
                        .build().register(JacksonJsonProvider.class).target(uri);

                UserResource endpoint = WebResourceFactory.newResource(UserResource.class, target);

                authResponse = endpoint.auth(idToken);
            } catch (Exception e) {
                EventBus.getDefault().post(new LoginEvent(LoginEvent.EventType.Failed));
                Log.e(TAG, "Error contacting server.", e);
                return false;
            }

            Boolean success = false;

            if(authResponse != null) {
                if (authResponse.length() > 0) {
                    String[] authResponseSplit = authResponse.split(":");
                    if (authResponseSplit.length == 2) {
                        SessionHandler.setSessionUser(getApplicationContext(), authResponseSplit[0]); // save session user
                        SessionHandler.setSessionToken(getApplicationContext(), authResponseSplit[1]); // save session token
                        success = true;
                    }
                }
            }

            return success;

        }

        @Override
        protected void onPostExecute(Boolean success) {
            mIsConnecting = false;

            if(!success) {
                EventBus.getDefault().post(new LoginEvent(LoginEvent.EventType.Failed));
                //Toast.makeText(getApplicationContext(), R.string.kwikshop_login_failed, Toast.LENGTH_LONG).show();
                mStatus.setText(R.string.kwikshop_login_failed);
            } else {
                EventBus.getDefault().post(new LoginEvent(LoginEvent.EventType.Success));
            }
            updateUI();
        }

    }

    public void onEventMainThread(LoginEvent event) {
        switch(event.getEventType()) {
            case Started:
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage(LoginActivity.this.getResources().getString(R.string.loading));
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();
                break;
            case Failed:
                progressDialog.setMessage(getApplicationContext().getResources().getString(R.string.error));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 1000);
                break;
            case Success:
                progressDialog.dismiss();
                break;
        }
    }

    private void exitLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), ListOfShoppingListsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_sign_in_button:
                // User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
                mStatus.setText(R.string.signing_in);
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
                // [START sign_in_clicked]
                mShouldResolve = true;
                mGoogleApiClient.connect();
                // [END sign_in_clicked]
                break;
            case R.id.login_sign_out_button:
                // Clear the default account so that GoogleApiClient will not automatically
                // connect in the future.
                // [START sign_out_clicked]
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
                // [END sign_out_clicked]
                SessionHandler.logout(getApplicationContext());
                updateUI();
                break;
            case R.id.login_skip_button:
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(R.string.login_skip);
                builder.setMessage(R.string.login_skip_message);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        SharedPreferencesHelper.saveInt(SharedPreferencesHelper.SKIP_LOGIN, 1, getApplicationContext());
                        exitLoginActivity();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        return;
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.login_retry_button:
                mStatus.setText(R.string.signing_in);
                mIsConnecting = true;
                new GetIdTokenTask().execute(null, null, null);
                updateUI();
                break;
            case R.id.login_debug_login:
                SessionHandler.setSessionUser(getApplicationContext(), "DEBUG");
                SessionHandler.setSessionToken(getApplicationContext(), "DEBUG");
                updateUI();
                break;
            case R.id.login_back_button:
                finish();
                break;
        }
    }
}
