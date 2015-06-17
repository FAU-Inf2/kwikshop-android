package de.fau.cs.mad.kwikshop.android.model;

import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;

public class ItemParser {

    private final SimpleStorage<Unit> unitStorage;
    private final DisplayHelper displayHelper;

    @Inject
    public ItemParser(SimpleStorage<Unit> unitStorage, DisplayHelper displayHelper) {

        if(unitStorage == null) {
            throw new IllegalArgumentException("'unitStorage' must not be null");
        }

        if(displayHelper == null) {
            throw new IllegalArgumentException("'displayHelper' must not be null");
        }

        this.unitStorage = unitStorage;
        this.displayHelper = displayHelper;
    }


    public Item parseAmountAndUnit(Item item) {

        String input = item.getName();
        String output = "";
        String amount = "";
        String thisCanBeUnitOrName = "";
        boolean lastCharWasANumber = false;
        boolean charWasReadAfterAmount = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            //only parses the first number found to amount
            if (c > 47 && c < 58 && (lastCharWasANumber == true || amount == "")) {
                amount = amount + c;
                lastCharWasANumber = true;
            } else if (lastCharWasANumber && c == ' ') {
                //ignore all white spaces between the amount and the next char
            } else if (lastCharWasANumber || charWasReadAfterAmount && c != ' ') {
                //String from amount to next whitespace, this should be unit or name
                thisCanBeUnitOrName = thisCanBeUnitOrName + c;
                lastCharWasANumber = false;
                charWasReadAfterAmount = true;
            } else if (charWasReadAfterAmount && c == ' ') {
                //whitespace after possible unit
                charWasReadAfterAmount = false;
            } else {
                output = output + c;
                lastCharWasANumber = false;
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


}
