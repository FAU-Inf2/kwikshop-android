package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.common.Recipe;

public class DialogFinishedEvent {

    private final double scaledValue;
    private final Recipe recipe;

    public DialogFinishedEvent(double scaledValue, Recipe recipe){
        this.scaledValue = scaledValue;
        this.recipe = recipe;
    }

    public double getScaledValue(){
        return scaledValue;
    }

    public Recipe getRecipe(){
        return recipe;
    }

}
