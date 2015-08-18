package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.fau.cs.mad.kwikshop.common.Item;

public class EANrestClient {

    public String API = "4ed09746870bec9ed85424a3ea49b834";

    private Context context;

    public EANrestClient(Context context){
        this.context = context;
    }

    public static EANrestClient initiateEANrest(Context context){
        return new EANrestClient(context);
    }

    public interface onEANrestResponse{
        void handleRESTresponse(Item restItem);
    }

    public void getRestResponse(String EAN, final onEANrestResponse listener){

        String url = "https://api.outpan.com/v1/products/" + EAN;
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    onEANrestResponse mListener = listener;

                    @Override
                    public void onResponse(String data) {
                        JSONObject json = null;
                        Item restItem = new Item();
                        try {
                            json = new JSONObject(data);
                            restItem.setName(json.getString("name"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mListener.handleRESTresponse(restItem);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        // Handle error
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0");
                String credentials = API + ":";
                String encodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + encodedCredentials);
                return headers;
            }
        };

        queue.add(req);
    }

}
