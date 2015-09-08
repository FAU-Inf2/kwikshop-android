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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.restclient.LeaseResource;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.SynchronizationLease;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import de.greenrobot.event.EventBus;

/*
    Quick & Dirty test UI for interaction with kwikshop-server
 */
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
    @SuppressWarnings("unused")
    void getShoppingLists() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Getting shopping lists...");

                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();

                    List<ShoppingListServer> shoppingLists = client.getLists();

                    String serialized = mapper.writeValueAsString(shoppingLists);
                    privateBus.post(serialized);

                } catch (Exception e) {

                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();
    }

    @OnClick(R.id.button_getDeletedShoppingLists)
    @SuppressWarnings("unused")
    void getDeletedShoppingLists() {


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Getting deleted shopping lists...");

                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();

                    List<DeletionInfo> shoppingLists = client.getDeletedLists();

                    String serialized = mapper.writeValueAsString(shoppingLists);
                    privateBus.post(serialized);

                } catch (Exception e) {

                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();

    }

    @OnClick(R.id.button_getShoppingList)
    @SuppressWarnings("unused")
    void getShoppingList() {

        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting shopping list...");


                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                    ShoppingListServer list = client.getLists(listId);

                                    privateBus.post(mapper.writeValueAsString(list));

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

    @OnClick(R.id.button_getShoppingListItems)
    @SuppressWarnings("unused")
    void getShoppingListItems() {

        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting shopping list items...");


                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                    List<Item> list = client.getListItems(listId);

                                    privateBus.post(mapper.writeValueAsString(list));

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

    @OnClick(R.id.button_getDeletedShoppingListItems)
    @SuppressWarnings("unused")
    void getDeletedShoppingListItems() {

        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting deleted shopping list items...");


                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                    List<DeletionInfo> list = client.getDeletedListItems(listId);

                                    privateBus.post(mapper.writeValueAsString(list));

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

    @OnClick(R.id.button_createShoppingList)
    @SuppressWarnings("unused")
    void createShoppingList() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {

                    privateBus.post("Creating sample shopping list on server...");

                    ShoppingListServer newList = new ShoppingListServer();
                    newList.setName("New List");
                    newList.setLastModifiedDate(new Date());


                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                    newList = client.createList(newList);

                    privateBus.post(mapper.writeValueAsString(newList));

                } catch (Exception e) {
                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();
    }

    @OnClick(R.id.button_createShoppingListItem)
    @SuppressWarnings("unused")
    void createShoppingListItem() {


        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Creating item in shopping list..");

                        final int listId;

                        try {
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

                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                    newItem = client.createItem(listId, newItem);

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

    @OnClick(R.id.button_deleteShoppingList)
    @SuppressWarnings("unused")
    void deleteShoppingList() {
        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                        privateBus.post("Deleting shopping list...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {


                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                    client.deleteList(listId);

                                    privateBus.post("OK");

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

    @OnClick(R.id.button_editShoppingList)
    @SuppressWarnings("unused")
    void editShoppingList() {
        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                        privateBus.post("Editing shopping list...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();

                                    ShoppingListServer list = client.getLists(listId);
                                    list.setName(list.getName() + "_edited");
                                    list.setLastModifiedDate(new Date());

                                    list = client.updateList(listId, list);

                                    privateBus.post(mapper.writeValueAsString(list));


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

    @OnClick(R.id.button_getShoppingListItem)
    @SuppressWarnings("unused")
    void getShoppingListItem() {

        viewLauncher.showTextInputDialog("Shopping List Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting shopping list item...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        viewLauncher.showTextInputDialog("Item Id", "",
                                new Command<String>() {
                                    @Override
                                    public void execute(String parameter) {


                                        final int itemId;
                                        try {
                                            itemId = Integer.parseInt(parameter);
                                        } catch (Exception e) {
                                            privateBus.post(getStackTrace(e));
                                            return;
                                        }


                                        new AsyncTask<Void, Void, Void>() {


                                            @Override
                                            protected Void doInBackground(Void... voids) {

                                                try {
                                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                                    Item item = client.getListItem(listId, itemId);

                                                    privateBus.post(mapper.writeValueAsString(item));

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
                },
                NullCommand.StringInstance);

    }

    @OnClick(R.id.button_editShoppingListItem)
    @SuppressWarnings("unused")
    void editShoppingListItem() {

        viewLauncher.showTextInputDialog("Shopping List Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting shopping list item...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        viewLauncher.showTextInputDialog("Item Id", "",
                                new Command<String>() {
                                    @Override
                                    public void execute(String parameter) {


                                        final int itemId;
                                        try {
                                            itemId = Integer.parseInt(parameter);
                                        } catch (Exception e) {
                                            privateBus.post(getStackTrace(e));
                                            return;
                                        }


                                        new AsyncTask<Void, Void, Void>() {


                                            @Override
                                            protected Void doInBackground(Void... voids) {

                                                try {
                                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                                    Item item = client.getListItem(listId, itemId);
                                                    item.setName(item.getName() + "_edited");

                                                    item = client.updateItem(listId, itemId, item);

                                                    privateBus.post(mapper.writeValueAsString(item));

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
                },
                NullCommand.StringInstance);

    }

    @OnClick(R.id.button_deleteShoppingListItem)
    @SuppressWarnings("unused")
    void deleteShoppingListItem() {

        viewLauncher.showTextInputDialog("Shopping List Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Deleting shopping list item...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        viewLauncher.showTextInputDialog("Item Id", "",
                                new Command<String>() {
                                    @Override
                                    public void execute(String parameter) {


                                        final int itemId;
                                        try {
                                            itemId = Integer.parseInt(parameter);
                                        } catch (Exception e) {
                                            privateBus.post(getStackTrace(e));
                                            return;
                                        }


                                        new AsyncTask<Void, Void, Void>() {


                                            @Override
                                            protected Void doInBackground(Void... voids) {

                                                try {
                                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();
                                                    client.deleteListItem(listId, itemId);

                                                    privateBus.post("OK.");

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
                },
                NullCommand.StringInstance);

    }

    @OnClick(R.id.button_getRecipes)
    @SuppressWarnings("unused")
    void getRecipes() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Getting Recipes...");

                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();

                    List<RecipeServer> shoppingLists = client.getLists();

                    String serialized = mapper.writeValueAsString(shoppingLists);
                    privateBus.post(serialized);

                } catch (Exception e) {

                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();
    }

    @OnClick(R.id.button_getDeletedRecipes)
    @SuppressWarnings("unused")
    void getDeletedRecipes() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Getting deleted Recipes...");

                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                    List<DeletionInfo> shoppingLists = client.getDeletedLists();

                    String serialized = mapper.writeValueAsString(shoppingLists);
                    privateBus.post(serialized);

                } catch (Exception e) {

                    privateBus.post(getStackTrace(e));
                }

                return null;

            }
        }.execute();
    }

    @OnClick(R.id.button_getRecipe)
    @SuppressWarnings("unused")
    void getRecipe() {

        viewLauncher.showTextInputDialog("Recipe Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting recipes...");


                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                    RecipeServer list = client.getLists(listId);

                                    privateBus.post(mapper.writeValueAsString(list));

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

    @OnClick(R.id.button_getRecipeItems)
    @SuppressWarnings("unused")
    void getRecipeItems() {
        viewLauncher.showTextInputDialog("Recipe Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting Recipe items...");


                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                    List<Item> list = client.getListItems(listId);

                                    privateBus.post(mapper.writeValueAsString(list));

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

    @OnClick(R.id.button_getDeletedRecipeItems)
    @SuppressWarnings("unused")
    void getDeletedRecipeItems() {

        viewLauncher.showTextInputDialog("Recipe Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting deleted recipe items...");


                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                    List<DeletionInfo> list = client.getDeletedListItems(listId);

                                    privateBus.post(mapper.writeValueAsString(list));

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


    @OnClick(R.id.button_createRecipe)
    @SuppressWarnings("unused")
    void createRecipe() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {

                    privateBus.post("Creating sample recipe on server...");

                    RecipeServer newList = new RecipeServer();
                    newList.setName("New Recipe");
                    newList.setLastModifiedDate(new Date());

                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                    newList = client.createList(newList);

                    privateBus.post(mapper.writeValueAsString(newList));

                } catch (Exception e) {
                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();
    }

    @OnClick(R.id.button_createRecipeItem)
    @SuppressWarnings("unused")
    void createRecipeItem() {


        viewLauncher.showTextInputDialog("Recipe List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Creating item in recipe..");

                        final int listId;

                        try {
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

                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                    newItem = client.createItem(listId, newItem);

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

    @OnClick(R.id.button_deleteRecipe)
    @SuppressWarnings("unused")
    void deleteRecipe() {
        viewLauncher.showTextInputDialog("Recipe Id ", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                        privateBus.post("Deleting recipe...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {


                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                    client.deleteList(listId);

                                    privateBus.post("OK");

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

    @OnClick(R.id.button_editRecipe)
    @SuppressWarnings("unused")
    void editRecipe() {
        viewLauncher.showTextInputDialog("Recipe Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                        privateBus.post("Editing recipe...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {


                                try {

                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();

                                    RecipeServer list = client.getLists(listId);
                                    list.setName(list.getName() + "_edited");
                                    list.setLastModifiedDate(new Date());

                                    list = client.updateList(listId, list);

                                    privateBus.post(mapper.writeValueAsString(list));


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

    @OnClick(R.id.button_getRecipeItem)
    @SuppressWarnings("unused")
    void getRecipeItem() {

        viewLauncher.showTextInputDialog("Recipe Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting recipe item...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        viewLauncher.showTextInputDialog("Item Id", "",
                                new Command<String>() {
                                    @Override
                                    public void execute(String parameter) {


                                        final int itemId;
                                        try {
                                            itemId = Integer.parseInt(parameter);
                                        } catch (Exception e) {
                                            privateBus.post(getStackTrace(e));
                                            return;
                                        }


                                        new AsyncTask<Void, Void, Void>() {


                                            @Override
                                            protected Void doInBackground(Void... voids) {

                                                try {
                                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                                    Item item = client.getListItem(listId, itemId);

                                                    privateBus.post(mapper.writeValueAsString(item));

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
                },
                NullCommand.StringInstance);

    }

    @OnClick(R.id.button_editRecipeItem)
    @SuppressWarnings("unused")
    void editRecipeItem() {

        viewLauncher.showTextInputDialog("Recipe Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Getting recipe item...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        viewLauncher.showTextInputDialog("Item Id", "",
                                new Command<String>() {
                                    @Override
                                    public void execute(String parameter) {


                                        final int itemId;
                                        try {
                                            itemId = Integer.parseInt(parameter);
                                        } catch (Exception e) {
                                            privateBus.post(getStackTrace(e));
                                            return;
                                        }


                                        new AsyncTask<Void, Void, Void>() {


                                            @Override
                                            protected Void doInBackground(Void... voids) {

                                                try {
                                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                                    Item item = client.getListItem(listId, itemId);
                                                    item.setName(item.getName() + "_edited");

                                                    item = client.updateItem(listId, itemId, item);

                                                    privateBus.post(mapper.writeValueAsString(item));

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
                },
                NullCommand.StringInstance);

    }

    @OnClick(R.id.button_deleteRecipeItem)
    @SuppressWarnings("unused")
    void deleteRecipeItem() {

        viewLauncher.showTextInputDialog("Recipe Id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {


                        privateBus.post("Deleting recipe item...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }


                        viewLauncher.showTextInputDialog("Item Id", "",
                                new Command<String>() {
                                    @Override
                                    public void execute(String parameter) {


                                        final int itemId;
                                        try {
                                            itemId = Integer.parseInt(parameter);
                                        } catch (Exception e) {
                                            privateBus.post(getStackTrace(e));
                                            return;
                                        }


                                        new AsyncTask<Void, Void, Void>() {


                                            @Override
                                            protected Void doInBackground(Void... voids) {

                                                try {
                                                    ListClient<RecipeServer> client = clientFactory.getRecipeClient();
                                                    client.deleteListItem(listId, itemId);

                                                    privateBus.post("OK.");

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
                },
                NullCommand.StringInstance);

    }

    @OnClick(R.id.button_postBoughtItems)
    @SuppressWarnings("unused")
    void postBoughtItems() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Posting BoughtItems...");

                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();

                    List<BoughtItem> boughtItems = new ArrayList<>();

                    boughtItems.add(new BoughtItem("Test1", "place1", "Kwik-E-Mart Springfield"));
                    boughtItems.add(new BoughtItem("Test2", "place1", "Kwik-E-Mart Springfield"));
                    boughtItems.add(new BoughtItem("Test4", "place1", "Kwik-E-Mart Springfield"));
                    boughtItems.add(new BoughtItem("Test5", "place1", "Kwik-E-Mart Springfield"));

                    boughtItems.add(new BoughtItem("Test1", "place2", "Foobar Supermarket"));

                    boughtItems.add(new BoughtItem("Test1", "place1", "Kwik-E-Mart Springfield"));
                    boughtItems.add(new BoughtItem("Test3", "place1", "Kwik-E-Mart Springfield"));
                    boughtItems.add(new BoughtItem("Test4", "place1", "Kwik-E-Mart Springfield"));
                    boughtItems.add(new BoughtItem("Test5", "place1", "Kwik-E-Mart Springfield"));

                    ItemOrderWrapper itemOrder = new ItemOrderWrapper(boughtItems);
                    client.postItemOrder(itemOrder);

                } catch (Exception e) {

                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();

    }

    @OnClick(R.id.button_sortList)
    @SuppressWarnings("unused")
    void sortList() {

        viewLauncher.showTextInputDialog("Shopping List id", "",
                new Command<String>() {
                    @Override
                    public void execute(String parameter) {

                        privateBus.post("Sorting list...");

                        final int listId;

                        try {
                            listId = Integer.parseInt(parameter);
                        } catch (Exception e) {
                            privateBus.post(getStackTrace(e));
                            return;
                        }

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {

                                try {

                                    ListClient<ShoppingListServer> client = clientFactory.getShoppingListClient();

                                    SortingRequest sortingRequest = new SortingRequest();
                                    sortingRequest.setPlaceId("place1");
                                    sortingRequest.setSupermarketName("Kwik-E-Mart Springfield");

                                    client.sortList(listId, sortingRequest);
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

    @OnClick(R.id.button_getLease)
    @SuppressWarnings("unused")
    void getLease() {


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    privateBus.post("Aquiring lease...");

                    LeaseResource leaseClient = clientFactory.getLeaseClient();

                    SynchronizationLease lease = leaseClient.getSynchronizationLeaseSynchronously();

                    privateBus.post(mapper.writeValueAsString(lease));

                } catch (Exception e) {

                    privateBus.post(getStackTrace(e));
                }

                return null;
            }
        }.execute();

    }

    @OnClick(R.id.button_extendLease)
    @SuppressWarnings("unused")
    void extendLease() {


        viewLauncher.showTextInputDialog("Lease id", "",
                new Command<String>() {
                    @Override
                    public void execute(final String parameter) {

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {

                                    privateBus.post("Extending lease...");

                                    LeaseResource leaseClient = clientFactory.getLeaseClient();

                                    SynchronizationLease lease = leaseClient.extendSynchronizationLeaseSynchronously(Integer.parseInt(parameter));

                                    privateBus.post(mapper.writeValueAsString(lease));

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


    @OnClick(R.id.button_removeLease)
    @SuppressWarnings("unused")
    void removeLease() {


        viewLauncher.showTextInputDialog("Lease id", "",
                new Command<String>() {
                    @Override
                    public void execute(final String parameter) {

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {

                                    privateBus.post(String.format("Removing lease %s ...", parameter));

                                    LeaseResource leaseClient = clientFactory.getLeaseClient();


                                    leaseClient.removeSynchronizationLeaseSynchronously(Integer.parseInt(parameter));

                                    privateBus.post("OK");

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
