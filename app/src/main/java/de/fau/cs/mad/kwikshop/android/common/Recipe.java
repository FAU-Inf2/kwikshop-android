package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.RecipeStorage;

@DatabaseTable (tableName = "recipe")
public class Recipe {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Item> items;

    private RecipeStorage recipeStorage;


    public Recipe(int id) {
        this.id = id;
    }

    public Recipe() {
        // Default no-arg constructor for generating Items, required for ORMLite
        recipeStorage = ListStorageFragment.getRecipeStorage();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void save() {
        recipeStorage.saveRecipe(this);
    }

    public Boolean delete() {
        return recipeStorage.deleteRecipe(this.getId());
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    public boolean removeItem(int id) {
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            Item currentItem = iterator.next();
            if (currentItem.getId() == id) {
                items.remove(currentItem);
                recipeStorage.deleteItem(id);
                return true;
            }
        }
        return false;
    }

    public boolean removeItem(Item item) {
        return removeItem(item.getId());
    }

    public int size() {
        return items.size();
    }

    public Collection<Item> getItems() {
        return Collections.unmodifiableCollection(this.items);
    }

    public Item getItem(int id) {
        for (Item item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        //TODO: returning null is a bad idea
        return null;
    }

}
