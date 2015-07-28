package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
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
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.greenrobot.event.EventBus;

public class ServerIntegrationDebugActivity extends BaseActivity {


    public static Intent getIntent(Context context) {

        return new Intent(context, ServerIntegrationDebugActivity.class);
    }


    private final EventBus privateBus = EventBus.builder().build();
    private final ObjectMapper mapper;

    @InjectView(R.id.textView)
    TextView textView_Result;

    @Inject
    RestClientFactory clientFactory;

    @Inject
    ViewLauncher viewLauncher;

    public ServerIntegrationDebugActivity() {

        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_integration_debug);


        ButterKnife.inject(this);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(this));
        objectGraph.inject(this);

        privateBus.register(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        privateBus.unregister(this);
    }


    @OnClick(R.id.button_getShoppingLists)
    void getShoppingLists() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Getting shopping lists...");

                    ShoppingListResource client = clientFactory.getShoppingListClient();

                    List<ShoppingListServer> shoppingLists = client.getListSynchronously();

                    String serialized = mapper.writeValueAsString(shoppingLists);
                    privateBus.post(serialized);

                } catch (Exception e) {

                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();
    }

    @OnClick(R.id.button_createShoppingList)
    void createShoppingList() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try{

                    privateBus.post("Creating sample shopping list on server...");

                    ShoppingListServer newList = new ShoppingListServer();
                    newList.setName("New List");
                    newList.setLastModifiedDate(new Date());


                    ShoppingListResource client = clientFactory.getShoppingListClient();
                    newList = client.createListSynchronously(newList);

                    privateBus.post(mapper.writeValueAsString(newList));

                } catch (Exception e) {
                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();
    }

    @OnClick(R.id.button_createShoppingListItem)
    void createShoppingListItem() {


        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        final int listId;

                        try{
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    Item newItem = new Item();
                                    newItem.setName("New Item on Server");
                                    newItem.setComment("Sample Comment");

                                    ShoppingListResource client = clientFactory.getShoppingListClient();
                                    newItem = client.createItemSynchronously(listId, newItem);

                                    privateBus.post(mapper.writeValueAsString(newItem));

                                } catch (Exception e) {
                                    privateBus.post(getStackTrace(e));
                                }

                                return null;

                            }
                        }.execute();


                    }
                },
                NullCommand.StringInstance);


    }

    public void onEventMainThread(String value) {
        textView_Result.setText(value);
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }


}
