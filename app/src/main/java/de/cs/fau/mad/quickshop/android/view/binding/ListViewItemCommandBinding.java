package de.cs.fau.mad.quickshop.android.view.binding;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;
import de.cs.fau.mad.quickshop.android.viewmodel.common.CommandListener;

public class ListViewItemCommandBinding extends Binding implements CommandListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    public enum ListViewItemCommandType {
        Click,
        LongClick
    }

    private final ListViewItemCommandType type;
    private final ListView listView;
    private final Command<Integer> command;

    public ListViewItemCommandBinding(ListViewItemCommandType type, ListView listView, Command<Integer> command) {

        if (listView == null) {
            throw new IllegalArgumentException("'listView' must not be null");
        }

        if (command == null) {
            throw new IllegalArgumentException("'command' must not be null");
        }

        this.type = type;
        this.listView = listView;
        this.command = command;

        this.command.setListener(this);
        switch (type) {

            case Click:
                listView.setOnItemClickListener(this);
                break;
            case LongClick:
                listView.setOnItemLongClickListener(this);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }


    @Override
    public void onIsAvailableChanged(boolean newValue) {

    }

    @Override
    public void onCanExecuteChanged(boolean newValue) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (command.getCanExecute()) {
            command.execute((int) id);
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (command.getCanExecute()) {
            command.execute((int) id);
            return true;
        } else {
            return false;
        }
    }

}
