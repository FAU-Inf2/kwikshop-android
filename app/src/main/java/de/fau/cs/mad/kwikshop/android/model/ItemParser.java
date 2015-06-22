package de.fau.cs.mad.kwikshop.android.model;

import android.app.Activity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;

public class ItemParser {

    private final SimpleStorage<Unit> unitStorage;
    private final DisplayHelper displayHelper;
    private final Activity context;

    @Inject
    public ItemParser(SimpleStorage<Unit> unitStorage, DisplayHelper displayHelper, Activity context) {

        if(unitStorage == null) {
            throw new IllegalArgumentException("'unitStorage' must not be null");
        }

        if(displayHelper == null) {
            throw new IllegalArgumentException("'displayHelper' must not be null");
        }

        if(context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        this.unitStorage = unitStorage;
        this.displayHelper = displayHelper;
        this.context = context;
    }


    public Item parseAmountAndUnit(Item item) {

        String input = item.getName();
        String output = "";
        String amount = "";
        String thisCanBeUnitOrName = "";
        boolean lastCharWasANumber = false;
        boolean charWasReadAfterAmount = false;
        boolean emptyStringOrWhiteSpace = true;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            //only parses the first number found to amount
            if (c > 47 && c < 58 && (lastCharWasANumber == true || amount == "") && emptyStringOrWhiteSpace) {
                amount = amount + c;
                lastCharWasANumber = true;
            } else if (lastCharWasANumber && c == ' ') {
                //ignore all white spaces between the amount and the next char
                emptyStringOrWhiteSpace = true;
            } else if (lastCharWasANumber || charWasReadAfterAmount && c != ' ') {
                //String from amount to next whitespace, this should be unit or name
                thisCanBeUnitOrName = thisCanBeUnitOrName + c;
                lastCharWasANumber = false;
                charWasReadAfterAmount = true;
                emptyStringOrWhiteSpace = false;
            } else if (charWasReadAfterAmount && c == ' ') {
                //whitespace after possible unit
                charWasReadAfterAmount = false;
                emptyStringOrWhiteSpace = true;
            } else if(c == ' '){
                emptyStringOrWhiteSpace = true;
                output = output + c;
            } else {
                output = output + c;
                lastCharWasANumber = false;
                emptyStringOrWhiteSpace = false;
            }
        }


        boolean unitMatchFound = false;

        for (Unit unit : unitStorage.getItems()) {
            if (displayHelper.getDisplayName(unit).equalsIgnoreCase(thisCanBeUnitOrName) ||
                    displayHelper.getShortDisplayName(unit).equalsIgnoreCase(thisCanBeUnitOrName)) {
                item.setUnit(unit);
                unitMatchFound = true;
                break;
            }
        }


        if (unitMatchFound == false && thisCanBeUnitOrName != "") {
            //if no unit was found complete string has to be restored
            if (output != "") {
                output = thisCanBeUnitOrName + " " + output;
            } else {
                output = thisCanBeUnitOrName;
            }
        }

        if (!StringHelper.isNullOrWhiteSpace(output)) {
            if (amount != "") item.setAmount(Integer.parseInt(amount));
            item.setName(output);
        }
        return item;


    }

    public ArrayList<String> parseSeveralItems(String input){

        ArrayList<String> output = new ArrayList<>();
        int positionWordBegin = 0;
        int positionLastWhiteSpace = 0;

        for(int i = 0; i < input.length(); i++){
            if(i == input.length() -1){
                String word = input.substring(positionWordBegin, input.length());
                output.add(word);
                break;
            }
            if(input.charAt(i) == ' ' && i != positionLastWhiteSpace){
                String word = input.substring(positionLastWhiteSpace, i);
                if(word.equalsIgnoreCase(context.getString(R.string.item_divider))){
                    output.add(input.substring(positionWordBegin, positionLastWhiteSpace - 1));
                    positionWordBegin = i + 1;
                }
                positionLastWhiteSpace = i + 1;
            }
        }

        return output;
    }


}
