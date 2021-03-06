package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;

import java.util.ArrayList;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.TutorialChapter;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;

public class TutorialViewModel {

    private Context context;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;

    @Inject
    public TutorialViewModel(ResourceProvider resourceProvider, ViewLauncher viewLauncher) {

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(viewLauncher == null) {
            throw new ArgumentNullException("viewLauncher");
        }

        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;
    }

    public void setContext(Context context){this.context = context;}

    public ArrayList<TutorialChapter> getChapters(){
        ArrayList<TutorialChapter> chapters = new ArrayList<>();

        // Navigation Chapter
        chapters.add(new TutorialChapter(R.drawable.tutorial_nav_drawer, R.string.tutorial_navigation_title, R.string.tutorial_navigation_description));

        // Create Shopping List Chapter
        chapters.add(new TutorialChapter(R.drawable.tutorial_create_shopping_list, R.string.tutorial_create_shopping_list_title, R.string.tutorial_create_shopping_list_description));

        // Create new Item
        chapters.add(new TutorialChapter(R.drawable.tutorial_create_item, R.string.tutorial_create_item_title, R.string.tutorial_create_item_description));

        // put item in shopping cart
        chapters.add(new TutorialChapter(R.drawable.tutorial_swipe_item, R.string.tutorial_swipe_item_title, R.string.tutorial_swipe_item_description));

        // add recipe to shopping list
        chapters.add(new TutorialChapter(R.drawable.tutorial_add_recipe, R.string.tutorial_add_recipe_to_shopping_list_title, R.string.tutorial_add_recipe_to_shopping_list_description));

        return chapters;
    }

}
