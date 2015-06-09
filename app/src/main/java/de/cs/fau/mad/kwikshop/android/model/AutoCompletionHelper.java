package de.cs.fau.mad.kwikshop.android.model;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.cs.fau.mad.kwikshop.android.common.AutoCompletionData;

public class AutoCompletionHelper{
    private SimpleStorage<AutoCompletionData> autoCompletionStorage;
    private DatabaseHelper databaseHelper;

    private ArrayList<String> autocompleteSuggestions = null;

    private static AutoCompletionHelper instance = null; //singleton

    private AutoCompletionHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }
        databaseHelper = new DatabaseHelper(context);
        try {
            //create local autocompletion storage
            autoCompletionStorage = new SimpleStorage<>(databaseHelper.getAutoCompletionDao());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<AutoCompletionData> autoCompletionData = autoCompletionStorage.getItems();
        autocompleteSuggestions = new ArrayList<String>(autoCompletionData.size());
        for (AutoCompletionData data : autoCompletionData) {
            autocompleteSuggestions.add(data.getText());
        }
    }

    public static AutoCompletionHelper getAutoCompletionHelper(Context context) {
        if (instance == null) {
            instance = new AutoCompletionHelper(context);
        }
        return instance;
    }

    /**
     * Gets a ArrayAdapter which can be used in setAdapter-method of a AutoCompleteTextView
     * @param activity the current Activity
     */
    public ArrayAdapter<String> getAdapter(Activity activity) {
        return new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
    }

    /**
     * Offers a text for further autocompletion queries. If this text is already stored due to
     * previous calls, nothing happens. Otherwise the text is stored.
     */
    public void offer(String text) {
        if (!autocompleteSuggestions.contains(text)) {
            autocompleteSuggestions.add(text);
            autoCompletionStorage.addItem(new AutoCompletionData(text));
        }
    }
}
