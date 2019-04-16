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

    public ConversationsScreenRecyclerViewAdapter(ArrayList<ContactChatDetails> layoutItems, Context context, String phone) {
        super(layoutItems, context, phone);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        super.onBindViewHolder(viewHolder, i);

        // When item in recycler is long clicked, popup menu will appear on screen
        viewHolder.chatsLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // Create popup menu and inflate (load) chats menu
                PopupMenu popup = new PopupMenu(viewHolder.chatsLayout.getContext(), viewHolder.chatsLayout);
                popup.inflate(R.menu.contacts_list_menu);

                // Listen to menu options buttons
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.DeleteChat:
                                String contactPhoneNum = ConversationsScreenRecyclerViewAdapter.this.layoutItems.get(i).getContactPhone();
                                ((ConversationsScreen)ConversationsScreenRecyclerViewAdapter.this.context).deleteConversation(contactPhoneNum);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.setGravity(Gravity.LEFT);
                popup.show();
                return true;
            }
        });
    }
}
