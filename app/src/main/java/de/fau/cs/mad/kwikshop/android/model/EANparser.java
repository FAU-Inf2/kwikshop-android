package de.fau.cs.mad.kwikshop.android.model;


import android.content.Context;

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

import de.fau.cs.mad.kwikshop.common.Item;

public class EANparser {

    private Context context;

    public EANparser(Context context) {
        this.context = context;
    }


    public static EANparser initiateOpenEANparserRequest(Context context){
        return new EANparser(context);
    }


    public interface onEANparserListener {
        void handleParserResponse(Item item);
    }

    public void parseWebsite(String EAN, final onEANparserListener listener){

        String url = "http://www.opengtindb.org/index.php?cmd=ean1&ean=" + EAN + "&sq=1";
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String data) {
                        Document doc = Jsoup.parse(data);
                        Elements link = doc.select("a[href*=/gp/]");
                        String linkText = link.text();

                        Item parsedItem = new Item();
                        parsedItem.setName(linkText);

                        listener.handleParserResponse(parsedItem);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //volleyError.getMessage();
                    }
                }
        )
            
        {
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
