package de.fau.cs.mad.kwikshop.android.model;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.AutoCompletionBrandData;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionData;
import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.greenrobot.event.EventBus;

public class AutoCompletionHelper{
    private SimpleStorage<AutoCompletionData> autoCompletionNameStorage;
    private SimpleStorage<AutoCompletionBrandData> autoCompletionBrandStorage;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> autocompleteNameSuggestions = null;
    private ArrayList<String> autocompleteBrandSuggestions = null;
    private HashMap<String, Group> autoGroup = null;

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

        initializeSuggestionCollections();
    }

    private void initializeSuggestionCollections() {
        List<AutoCompletionData> autoCompletionNameData = autoCompletionNameStorage.getItems();
        autocompleteNameSuggestions = new ArrayList<String>(autoCompletionNameData.size());
        autoGroup = new HashMap<>();
        for (AutoCompletionData data : autoCompletionNameData) {
            autocompleteNameSuggestions.add(data.getName());
            if(data.getGroup() != null){
                autoGroup.put(data.getName(), data.getGroup());
            }
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
     * Gets the Group that was stored for this item the last time, or null, if no such Group exists
     */
    public Group getGroup(String itemName) {
        if (itemName == null) {
            throw new IllegalArgumentException("itemName must not be null");
        }

        return autoGroup.get(itemName);
    }

    /**
     * Offers a text for further autocompletion queries of item names. If this text is already
     * stored due to previous calls, nothing happens. Otherwise the text is stored.
     */
    public void offerName(String name) {
        //offer(name, autocompleteNameSuggestions, autoCompletionNameStorage);
        name = removeSpacesAtEndOfWord(name);
        putNameAndGroupIntoStorageAndArray(name, null);
    }

    public void offerNameAndGroup(String name, Group group){
        name = removeSpacesAtEndOfWord(name);
        if (autoGroup.containsKey(name)){
            addGroupToItem(name, group);
            return;
        }
        putNameAndGroupIntoStorageAndArray(name, group);
    }

    private void addGroupToItem(String itemName, Group group) {
        //if (!autocompleteNameSuggestions.contains(itemName))
        //    return;
        autoGroup.put(itemName, group);
        autoCompletionNameStorage.getItems().get(autoCompletionNameStorage.getItems().indexOf(new AutoCompletionData(itemName))).setGroup(group);
    }

    private void putNameAndGroupIntoStorageAndArray(String name, Group group) {
        if(!autocompleteNameSuggestions.contains(name)){
            autocompleteNameSuggestions.add(name);
            AutoCompletionData data = (group == null ? new AutoCompletionData(name) : new AutoCompletionData(name, group));
            autoCompletionNameStorage.addItem(data);
        }
    }

    /**
     * Offers a text for further autocompletion queries of brands. If this text is already
     * stored due to previous calls, nothing happens. Otherwise the text is stored.
     */
    public void offerBrand(String brand) {
        //offer(brand, autocompleteBrandSuggestions, autoCompletionBrandStorage);
        brand = removeSpacesAtEndOfWord(brand);

        if(!autocompleteBrandSuggestions.contains(brand)){
            autocompleteBrandSuggestions.add(brand);
            autoCompletionBrandStorage.addItem(new AutoCompletionBrandData(brand));
        }
    }

    private String removeSpacesAtEndOfWord(String word) {
        if (null == word || word.isEmpty())
            return word;

        if (word.charAt(word.length() - 1) == ' ') {
            int i;
            for (i = 1; i < word.length(); i++) {
                if (word.charAt(word.length() - 1) != ' ') break;
            }
            word = word.substring(0, word.length() - i);
        }

        return word;
    }

    public void reloadFromDatabase(){
        if (instance == null)
            return;
        initializeSuggestionCollections();
        EventBus.getDefault().post(new AutoCompletionHistoryDeletedEvent());
    }

}
