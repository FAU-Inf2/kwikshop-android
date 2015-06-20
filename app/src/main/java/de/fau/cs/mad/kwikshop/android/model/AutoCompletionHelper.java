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

    private static volatile AutoCompletionHelper instance = null; //singleton

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
            synchronized (AutoCompletionHelper.class) { // double checked looking
                if (instance == null) {
                    instance = new AutoCompletionHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * Gets the instance of AutoCompletionHelper, if it was already created (via getAutoCompletionHelper)
     * @return the instance of AutoCompletionHelper, or null if it doesn't exist
     */
    public static AutoCompletionHelper getInstance() {
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
            updateGroupForName(name, group);
            return;
        }
        putNameAndGroupIntoStorageAndArray(name, group);
    }

    private void putNameAndGroupIntoStorageAndArray(String name, Group group) {
        if (!autocompleteNameSuggestions.contains(name)){
            autocompleteNameSuggestions.add(name);
            if (group == null) {
                autoCompletionNameStorage.addItem(new AutoCompletionData(name));
                return;
            } else {
                autoGroup.put(name, group);
                autoCompletionNameStorage.addItem(new AutoCompletionData(name, group));
                return;
            }
        }

        if (group == null) {
            return;
        }

        if (autoGroup.containsKey(name)) {
            updateGroupForName(name, group);
        } else {
            addGroupForName(name, group);
        }
    }

    private void addGroupForName(String name, Group group) {
        autoGroup.put(name, group);
        for (AutoCompletionData data : autoCompletionNameStorage.getItems()) {
            if (data.getName().equals(name)) {
                data.setGroup(group);
                autoCompletionNameStorage.updateItem(data);
                return;
            }
        }
    }

    private void updateGroupForName(String name, Group group) {
        if (autoGroup.get(name).equals(group)) {
            return; // current group for that item name already stored
        }
        addGroupForName(name, group);
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

    public static String removeSpacesAtEndOfWord(String word) {
        if (null == word || word.isEmpty())
            return word;

        while (word.charAt(word.length() - 1) == ' ') {
            word = word.substring(0,word.length() - 1);
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
