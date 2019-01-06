package speaktome.client;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactsScreen extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private RecyclerView rv;

    private String phone;

    private ArrayList<ItemDetails> contactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_screen);

        Intent intent = this.getIntent();
        this.phone = intent.getStringExtra("phone");

        getContacts(); // Init contacts list
    }

    /*
        Initializes recyclerview object
        Input: None
        Output: None
     */
    public void initRecyclerView(){
        this.rv = findViewById(R.id.ChatsList);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this.contactsList, this, this.phone);
        this.rv.setAdapter(adapter);
        this.rv.setLayoutManager(new LinearLayoutManager(this));
    }

    /*
        Function retrieves contacts
        Input: Current screen context
        Output: List of contacts
     */
    public void getContacts() {
        ContentResolver cr = this.getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        this.contactsList = new ArrayList<ItemDetails>();
        ItemDetails contactDetails = null;

        if(cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (pCur.moveToNext()) { // TODO: check if number has the application
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        contactDetails = new ItemDetails(null, contactName, contactNumber, "");
                        this.contactsList.add(contactDetails);
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext());
        }
    }
}
