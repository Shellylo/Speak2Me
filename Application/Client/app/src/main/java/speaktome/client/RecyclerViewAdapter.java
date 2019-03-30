package speaktome.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<ContactChatDetails> layoutItems;
    private Context context;
    private String phone;

    private MySqliteDatabase sqlDB;
    private ContactsListScreen conversationsScreen;

    public RecyclerViewAdapter(ArrayList<ContactChatDetails> layoutItems, Context context, String phone, MySqliteDatabase sqlDB, ContactsListScreen conversationsScreen) {
        this.layoutItems = layoutItems;
        this.context = context;
        this.phone = phone;
        this.sqlDB = sqlDB;
        this.conversationsScreen = conversationsScreen;
    }

    @NonNull
    @Override
    /*
        Function recycles each item
        Input: view group, i
        Output: holder contains the view
     */
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_chat, viewGroup, false );
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        ContactChatDetails currentDetails = this.layoutItems.get(i);
        viewHolder.contactImage.setImageResource(R.drawable.profile_picture); //TODO: get image from contact
        viewHolder.contactName.setText(currentDetails.getContactName());
        viewHolder.contactPhone.setText(currentDetails.getContactPhone());
        viewHolder.message.setText(currentDetails.getMessage());

        // Set item listener - wait for click
        viewHolder.chatsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecyclerViewAdapter.this.context, ChatScreen.class);
                intent.putExtra("src_phone", RecyclerViewAdapter.this.phone);
                intent.putExtra("dst_phone", RecyclerViewAdapter.this.layoutItems.get(i).getContactPhone());
                intent.putExtra("dst_name", RecyclerViewAdapter.this.layoutItems.get(i).getContactName());
                RecyclerViewAdapter.this.context.startActivity(intent);
                //Toast.makeText(RecyclerViewAdapter.this.context, RecyclerViewAdapter.this.layoutItems.get(i).getContactPhone(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
        Function returns the number of conversations
        Input: None
        Output: Number of conversations in Recyclerview
     */
    @Override
    public int getItemCount() {
        return this.layoutItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        ImageView contactImage;
        TextView contactName;
        TextView contactPhone;
        TextView message;
        RelativeLayout chatsLayout;
        public ViewHolder(final View itemView) {
            super(itemView);
            this.contactImage = itemView.findViewById(R.id.LayoutContactImage);
            this.contactName = itemView.findViewById(R.id.LayoutContactName);
            this.contactPhone = itemView.findViewById(R.id.LayoutPhoneNum);
            this.message = itemView.findViewById(R.id.LayoutMessageText);
            this.chatsLayout = itemView.findViewById(R.id.parent_layout);

            itemView.setOnCreateContextMenuListener(this);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.inflate(R.menu.contacts_list_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.DeleteChat:
                                    RecyclerViewAdapter.this.sqlDB.removeAllMessagesInChat(ViewHolder.this.contactPhone.getText().toString());
                                    ((ConversationsScreen)RecyclerViewAdapter.this.conversationsScreen).initRecyclerDetails();
                                    RecyclerViewAdapter.this.notifyDataSetChanged();
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

         /*   Function creates pop-up context menu when item in recycler view is held
            Input: menu, current view, menu info
            Output: None
        */

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }

    }
}
