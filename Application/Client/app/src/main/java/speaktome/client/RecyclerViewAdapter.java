package speaktome.client;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<ItemDetails> layoutItems;
    private Context context;

    public RecyclerViewAdapter(ArrayList<ItemDetails> layoutItems, Context context) {
        this.layoutItems = layoutItems;
        this.context = context;
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
        ItemDetails currentDetails = this.layoutItems.get(i);
        viewHolder.contactImage = currentDetails.getContactImage();
        viewHolder.contactName.setText(currentDetails.getContactName());
        viewHolder.contactPhone.setText(currentDetails.getContactPhone());
        viewHolder.message.setText(currentDetails.getMessage());

        // Set item listener - wait for click
        viewHolder.chatsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RecyclerViewAdapter.this.context, RecyclerViewAdapter.this.layoutItems.get(i).getContactPhone(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.layoutItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView contactImage;
        TextView contactName;
        TextView contactPhone;
        TextView message;
        RelativeLayout chatsLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.contactImage = itemView.findViewById(R.id.LayoutContactImage);
            this.contactName = itemView.findViewById(R.id.LayoutContactName);
            this.contactPhone = itemView.findViewById(R.id.LayoutPhoneNum);
            this.message = itemView.findViewById(R.id.LayoutMessageText);
            this.chatsLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}