package de.fau.cs.mad.kwikshop.android.viewmodel;

import java.util.LinkedList;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;


public abstract class ListDetailsViewModel<TList extends DomainListObject> extends ListViewModelBase {

    //listener interface
    public interface Listener<TList> extends ListViewModelBase.Listener {

    }

    protected class CompositeListener implements Listener {

        @Override
        public void onNameChanged(String value) {
            for (Listener l : listeners) {
                l.onNameChanged(value);
            }
        }

        @Override
        public void onFinish() {
            for (Listener l : listeners) {
                l.onFinish();
            }
        }
    }


    protected final List<Listener> listeners = new LinkedList<>();
    protected final Listener compositeListener = new CompositeListener();

    protected final ViewLauncher viewLauncher;
    protected final ListManager<TList> listManager;
    protected final ResourceProvider resourceProvider;

    protected int listId;
    protected boolean isNewList;

    protected Command saveCommand;
    protected Command cancelCommand;
    protected Command deleteCommand;


    public ListDetailsViewModel(final ViewLauncher viewLauncher,
                                final ResourceProvider resourceProvider,
                                final ListManager<TList> listManager) {

        if (viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }
        if (resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }

        if (listManager == null) {
            throw new IllegalArgumentException("'recipeStorage' must not be null");
        }

        this.viewLauncher = viewLauncher;
        this.resourceProvider = resourceProvider;
        this.listManager = listManager;

    }


    public void addListener(Listener value) {
        this.listeners.add(value);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }


    public void initialize() {
        initialize(-1);
    }

    public void initialize(int shoppingListId) {
        this.listId = shoppingListId;
        this.isNewList = shoppingListId == -1;

        initializeCommands();


        setUp();
    }


    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }

    public Command getDeleteCommand() {
        return this.deleteCommand;
    }

    @Override
    public void setName(String value) {
        super.setName(value);
        getSaveCommand().setCanExecute(!StringHelper.isNullOrWhiteSpace(value));
    }

    public boolean getIsNewList() {
        return isNewList;
    }


    @Override
    protected ListViewModelBase.Listener getListener() {
        return compositeListener;
    }

    protected void initializeCommands() {
        this.saveCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                saveCommandExecute();
            }
        };
        this.cancelCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                cancelCommandExecute();
            }
        };
        this.deleteCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                deleteCommandExecute();
            }
        };
    }

    protected void setUp(){

        this.saveCommand.setIsAvailable(true);
        this.cancelCommand.setIsAvailable(true);
    }


    protected abstract void saveCommandExecute();

    protected abstract void deleteCommandExecute();

    protected void cancelCommandExecute() {
        finish();
    }

}
