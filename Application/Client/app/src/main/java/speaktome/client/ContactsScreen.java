package speaktome.client;

import android.os.Bundle;

public class ContactsScreen extends ContactsListScreen {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_screen);

        this.contactsDetails = getContacts(); // Init contacts list

        this.rv = findViewById(R.id.ContactsList);
        initRecyclerView(); //Init recycler view
    }

    public void onPause() {
        super.onPause();
        finish();
    }
}
