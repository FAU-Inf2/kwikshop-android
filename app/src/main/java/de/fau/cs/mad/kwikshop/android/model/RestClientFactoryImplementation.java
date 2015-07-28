package de.fau.cs.mad.kwikshop.android.model;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Date;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.restclient.RecipeResource;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;


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
    public ShoppingListResource getShoppingListClient() {
        return getRestAdapter().create(ShoppingListResource.class);
    }

    @Override
    public RecipeResource getRecipeClient() {
        return getRestAdapter().create(RecipeResource.class);
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
                    return (hostname.equals(resourceProvider.getString(R.string.API_HOST)));
                }
            });

        } catch(Exception e) {
            return null;
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

                    @Override
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

                        return new Date(json.getAsLong());
                    }
                })
                .create();

        return new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(getApiEndPoint())
                .setConverter(new GsonConverter(gson))
                //TODO: get actual username / password
                .setRequestInterceptor(new BasicAuthenticationRequestInterceptor("DEBUG", "DEBUG"))
                .build();
    }

    private String getApiEndPoint() {
        return SharedPreferencesHelper.loadString(SharedPreferencesHelper.API_ENDPOINT,
                resourceProvider.getString(R.string.API_PROTOCOL) +
                        resourceProvider.getString(R.string.API_HOST) +
                        ":" + resourceProvider.getString(R.string.API_PORT),
                context);
    }

}
