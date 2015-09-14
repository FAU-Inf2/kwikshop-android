package de.fau.cs.mad.kwikshop.android.view.interfaces;
import android.widget.ImageButton;
import android.widget.TextView;

public interface EditModeActivity {

    ImageButton getSaveButton();

    ImageButton getDeleteButton();

    ImageButton getAddToShoppingCartButton();

    ImageButton getRemoveFromShoppingCartButton();

    TextView getMarkedItemsCountTextView();


}
