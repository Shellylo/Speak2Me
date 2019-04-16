package speaktome.client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.util.ArrayList;

public class ConversationsScreenRecyclerViewAdapter extends RecyclerViewAdapter {

    public ConversationsScreenRecyclerViewAdapter(ArrayList<ContactChatDetails> layoutItems, Context context, String phone, MySqliteDatabase sqlDB) {
        super(layoutItems, context, phone, sqlDB);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        super.onBindViewHolder(viewHolder, i);

        viewHolder.chatsLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popup = new PopupMenu(viewHolder.chatsLayout.getContext(), viewHolder.chatsLayout);
                popup.inflate(R.menu.contacts_list_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.DeleteChat:
                                ConversationsScreenRecyclerViewAdapter.this.sqlDB.removeAllMessagesInChat(ConversationsScreenRecyclerViewAdapter.this.layoutItems.get(i).getContactPhone());
                                ((ConversationsScreen)ConversationsScreenRecyclerViewAdapter.this.context).initRecyclerDetails();
                                ConversationsScreenRecyclerViewAdapter.this.notifyDataSetChanged();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.setGravity(Gravity.RIGHT);
                popup.show();
                return true;
            }
        });
    }
}
