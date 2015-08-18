package de.fau.cs.mad.kwikshop.android.model;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class OpenEANparser {

    private Context context;

    public OpenEANparser(Context context) {
        this.context = context;
    }


    public static OpenEANparser initiateOpenEANparserRequest(Context context){
        return new OpenEANparser(context);
    }


    public interface onEANParserResponseListener{
        void handleParserResult(String title);
    }

    public void parseWebsite(String EAN, final onEANParserResponseListener listener){

        String url = "http://www.opengtindb.org/index.php?cmd=ean1&ean=" + EAN + "&sq=1";
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                   onEANParserResponseListener mListener = listener;

                    @Override
                    public void onResponse(String data) {
                        Document doc = Jsoup.parse(data);

                        Elements link = doc.select("a[href*=/gp/]");
                        String linkText = link.text();
                        mListener.handleParserResult(linkText);
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
                return headers;
            }
        };



        queue.add(req);
    }
}
