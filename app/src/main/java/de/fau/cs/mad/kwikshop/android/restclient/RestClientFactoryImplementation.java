package de.fau.cs.mad.kwikshop.android.restclient;


import android.content.Context;


import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.BasicAuthenticationRequestInterceptor;
import de.fau.cs.mad.kwikshop.android.model.SessionHandler;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.restclient.RecipeResource;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;


public class RestClientFactoryImplementation implements RestClientFactory {

    private final Context context;
    private final ResourceProvider resourceProvider;

    @Inject
    public RestClientFactoryImplementation(Context context, ResourceProvider resourceProvider){

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        this.context = context;
        this.resourceProvider = resourceProvider;
    }


    @Override
    public ListClient<ShoppingListServer> getShoppingListClient() {
        return new ShoppingListClient(getRestAdapter().create(ShoppingListResource.class));
    }

    @Override
    public ListClient<RecipeServer> getRecipeClient() {
        return new RecipeClient(getRestAdapter().create(RecipeResource.class));
    }

    private RestAdapter getRestAdapter() {

        OkHttpClient client = new OkHttpClient();
        try {
            // Load cert from raw res
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getResources().openRawResource(R.raw.server);
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

            client.setSslSocketFactory(context.getSocketFactory());
            client.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    //return hv.verify("HOSTNAME", session);
                    return hostname.equals(getApiHost());
                }
            });

        } catch(Exception e) {
            //TODO: throw appropriate exception instead of returning null
            return null;
        }



        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(getApiEndPoint())
                .setConverter(new JacksonConverter())
                .setRequestInterceptor(new BasicAuthenticationRequestInterceptor(SessionHandler.getSessionUser(context), SessionHandler.getSessionToken(context)))
                .build();
    }

    private String getApiEndPoint() {
        return resourceProvider.getString(R.string.API_PROTOCOL) +
                getApiHost() +
                ":" + resourceProvider.getString(R.string.API_PORT);
    }

    private String getApiHost() {
        return SharedPreferencesHelper.loadString(SharedPreferencesHelper.API_ENDPOINT, resourceProvider.getString(R.string.API_HOST), context);
    }

}
