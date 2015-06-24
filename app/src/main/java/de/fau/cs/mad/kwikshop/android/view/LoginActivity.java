package de.fau.cs.mad.kwikshop.android.view;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.BuildConfig;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;

import de.fau.cs.mad.kwikshop.common.UserResource;

public class LoginActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private static final String SKIPLOGIN = "SKIPLOGIN"; // Used in SharedPreferences

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

    /* UI elements */
    @InjectView(R.id.login_sign_in_button)
    SignInButton login_sign_in_button;

    @InjectView(R.id.login_sign_out_button)
    Button login_sign_out_button;

    @InjectView(R.id.login_skip_button)
    Button login_skip_button;

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
            if (SessionHandler.isAuthenticated(getApplicationContext()) || SharedPreferencesHelper.loadInt(SKIPLOGIN, 0, getApplicationContext()) == 1)
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

        // Large sign-in
        login_sign_in_button.setSize(SignInButton.SIZE_WIDE);

        // Start with sign-in button disabled until sign-in either succeeds or fails
        login_sign_in_button.setEnabled(false);

        if (!BuildConfig.DEBUG) {
            mDebugStatus.setEnabled(false);
            mDebugStatus.setVisibility(View.GONE);
        }

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

    private void updateUI(boolean isSignedIn) {
        mDebugStatus.setText(SessionHandler.getSessionToken(getApplicationContext()));

        if(SessionHandler.isAuthenticated(getApplicationContext()))
            isSignedIn = true;

        // Hide debug buttons if this is not a debug build
        if (!BuildConfig.DEBUG) {
            login_debug_login.setEnabled(false);
            login_debug_login.setVisibility(View.GONE);
        }

        if (isSignedIn) {
            login_sign_in_button.setEnabled(false);
            login_sign_in_button.setVisibility(View.GONE);
            login_sign_out_button.setEnabled(true);
            login_sign_out_button.setVisibility(View.VISIBLE);
            login_skip_button.setEnabled(false);
            login_skip_button.setVisibility(View.GONE);

            if(!SessionHandler.isAuthenticated(getApplicationContext())) {
                login_retry_button.setEnabled(true);
                login_retry_button.setVisibility(View.VISIBLE);
            } else {
                if(!SessionHandler.getSessionToken(getApplicationContext()).equals("DEBUG")) {
                    // Show signed-in user's name
                    Person p = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                    if(p != null) {
                        String name = p.getDisplayName();
                        mStatus.setText(getText(R.string.signed_in_fmt) + " " + name);
                    }
                } else {
                    mStatus.setText("DEBUG logged in");
                }

                login_retry_button.setEnabled(false);
                login_retry_button.setVisibility(View.GONE);
                login_debug_login.setEnabled(false);
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
            // Show signed-out message
            mStatus.setText(R.string.signed_out);

            // Set button visibility
            login_sign_in_button.setEnabled(true);
            login_sign_in_button.setVisibility(View.VISIBLE);
            login_sign_out_button.setEnabled(false);
            login_sign_out_button.setVisibility(View.GONE);
            login_debug_login.setEnabled(true);
            login_debug_login.setVisibility(View.VISIBLE);
            login_retry_button.setEnabled(false);
            login_retry_button.setVisibility(View.GONE);
            login_skip_button.setEnabled(true);
            login_skip_button.setVisibility(View.VISIBLE);
        }
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

        if(!SessionHandler.isAuthenticated(getApplicationContext()))
            new GetIdTokenTask().execute(null, null, null);

        // Show the signed-in UI
        updateUI(true);
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
            updateUI(false);
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
                            updateUI(false);
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            mShouldResolve = false;
            updateUI(false);
        }
    }

    /* This task retrieves the user's session token from the server */
    private class GetIdTokenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
            Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            String scopes = "audience:server:client_id:" + getString(R.string.login_server_client_id);
            String idToken;
            try {
                idToken = GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving ID token.", e);
                return null;
            }

            WebTarget target = ClientBuilder.newClient().register(JacksonJsonProvider.class).target(getString(R.string.API_URL));
            UserResource endpoint = WebResourceFactory.newResource(UserResource.class, target);
            String authResponse;
            try {
                authResponse = endpoint.auth(idToken);
            } catch (Exception e) {
                Log.e(TAG, "Error contacting server.", e);
                return null;
            }
            SessionHandler.setSessionToken(getApplicationContext(), authResponse); // save session token
            return authResponse;

        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) {
                Toast.makeText(getApplicationContext(), R.string.kwikshop_login_failed, Toast.LENGTH_LONG).show();
                mStatus.setText(R.string.kwikshop_login_failed);
            }
            updateUI(true);
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
                updateUI(false);
                break;
            case R.id.login_skip_button:
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(R.string.login_skip);
                builder.setMessage(R.string.login_skip_message);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        SharedPreferencesHelper.saveInt(SKIPLOGIN, 1, getApplicationContext());
                        exitLoginActivity();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        return;
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.login_retry_button:
                mStatus.setText(R.string.signing_in);
                new GetIdTokenTask().execute(null, null, null);
                break;
            case R.id.login_debug_login:
                SessionHandler.setSessionToken(getApplicationContext(), "DEBUG");
                updateUI(true);
                break;
        }
    }
}
