package de.cs.fau.mad.kwikshop.android.model;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.cs.fau.mad.kwikshop.android.common.AutoCompletionBrandData;
import de.cs.fau.mad.kwikshop.android.common.AutoCompletionData;
import de.cs.fau.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.greenrobot.event.EventBus;

public class AutoCompletionHelper{
    private SimpleStorage<AutoCompletionData> autoCompletionNameStorage;
    private SimpleStorage<AutoCompletionBrandData> autoCompletionBrandStorage;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> autocompleteNameSuggestions = null;
    private ArrayList<String> autocompleteBrandSuggestions = null;

    private static AutoCompletionHelper instance = null; //singleton

    private AutoCompletionHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }
        databaseHelper = new DatabaseHelper(context);
        try {
            //create local autocompletion storage
            autoCompletionNameStorage = new SimpleStorage<>(databaseHelper.getAutoCompletionDao());
            autoCompletionBrandStorage = new SimpleStorage<>(databaseHelper.getAutoCompletionBrandDao());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initializeSuggestionArrays();
    }

    private void initializeSuggestionArrays() {
        List<AutoCompletionData> autoCompletionNameData = autoCompletionNameStorage.getItems();
        autocompleteNameSuggestions = new ArrayList<String>(autoCompletionNameData.size());
        for (AutoCompletionData data : autoCompletionNameData) {
            autocompleteNameSuggestions.add(data.getName());
        }

        List<AutoCompletionBrandData> autoCompletionBrandData = autoCompletionBrandStorage.getItems();
        autocompleteBrandSuggestions = new ArrayList<String>(autoCompletionBrandData.size());
        for (AutoCompletionBrandData data : autoCompletionBrandData) {
            autocompleteBrandSuggestions.add(data.getName());
        }
    }

    public static AutoCompletionHelper getAutoCompletionHelper(Context context) {
        if (instance == null) {
            instance = new AutoCompletionHelper(context);
        }
        return instance;
    }

    /**
     * Gets a ArrayAdapter which can be used in setAdapter-method of a AutoCompleteTextView,
     * where an item name is auto completed
     * @param activity the current Activity
     */
    public ArrayAdapter<String> getNameAdapter(Activity activity) {
        return getAdapter(activity, autocompleteNameSuggestions);
    }

    /**
     * Gets a ArrayAdapter which can be used in setAdapter-method of a AutoCompleteTextView,
     * where an brand is auto completed
     * @param activity the current Activity
     */
    public ArrayAdapter<String> getBrandAdapter(Activity activity) {
        return getAdapter(activity, autocompleteBrandSuggestions);
    }

    private ArrayAdapter<String> getAdapter(Activity activity, ArrayList<String> list) {
        return new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, list);
    }

    /**
     * Offers a text for further autocompletion queries of item names. If this text is already
     * stored due to previous calls, nothing happens. Otherwise the text is stored.
     */
    public void offerName(String name) {
        offer(name, autocompleteNameSuggestions, autoCompletionNameStorage);
    }

    /**
     * Offers a text for further autocompletion queries of brands. If this text is already
     * stored due to previous calls, nothing happens. Otherwise the text is stored.
     */
    public void offerBrand(String brand) {
        offer(brand, autocompleteBrandSuggestions, autoCompletionBrandStorage);
    }

    private void offer(String text, ArrayList<String> list, SimpleStorage storage) {
        if (null == text || text.isEmpty())
            return;

        if(!list.contains(text)){
            list.add(text);
            storage.addItem(text);
        }
    }

    public void reloadFromDatabase(){
        if (instance == null)
            return;
        initializeSuggestionArrays();
        EventBus.getDefault().post(new AutoCompletionHistoryDeletedEvent());
    }

}
