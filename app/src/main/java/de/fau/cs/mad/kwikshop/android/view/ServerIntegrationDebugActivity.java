package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.greenrobot.event.EventBus;

public class ServerIntegrationDebugActivity extends BaseActivity {


    public static Intent getIntent(Context context) {

        return new Intent(context, ServerIntegrationDebugActivity.class);
    }


    private final EventBus privateBus = EventBus.builder().build();


    @InjectView(R.id.textView)
    TextView textView_Result;

    @Inject
    RestClientFactory clientFactory;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_integration_debug);


        ButterKnife.inject(this);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(this));
        objectGraph.inject(this);

        privateBus.register(this);

    }



    @OnClick(R.id.button_getShoppingLists)
    void getShoppingLists() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Getting shopping lists...");

                    ObjectMapper mapper = new ObjectMapper();

                    ShoppingListResource client = clientFactory.getShoppingListClient();

                    List<ShoppingListServer> shoppingLists = client.getListSynchronously();

                    String serialized = mapper.writeValueAsString(shoppingLists);
                    privateBus.post(serialized);

                } catch (Exception e) {

                    privateBus.post(e.toString());
                }

                return null;
            }
        }.execute();

    }



    @Override
    public void onStop() {
        super.onStop();
        privateBus.unregister(this);
    }


    public void onEventMainThread(String value) {
        textView_Result.setText(value);
    }


//    private String toString(ShoppingListServer shoppingList) {
//
//        String format = "   %s = %s\n";
//        String result = "";
//
//        result += "ShoppingList: \n";
//        result += String.format(format, "id", shoppingList.getId());
//        result += String.format(format, "name", shoppingList.getName());
//        result += String.format(format, "sortType", shoppingList.getSortTypeInt());
//        result += String.format(format, "location", toString(shoppingList.getLocation()));
//        result += String.format(format, "lastModifiedDate", shoppingList.getLastModifiedDate());
//        result += "   Items: \n";
//
//        for(Item item : shoppingList.getItems()) {
//
//            result += toString(item);
//            result += "\n";
//        }
//
//
//        return result;
//    }
//
//    private String toString(LastLocation location) {
//
//        if(location == null) {
//            return "";
//        }
//
//        return  String.format("latitide = %s, longitude = %s, address = %s, name = %s, timestamp = %s",
//                location.getLatitude(),
//                location.getLongitude(),
//                location.getAddress(),
//                location.getName(),
//                location.getTimestamp());
//    }
//



}
