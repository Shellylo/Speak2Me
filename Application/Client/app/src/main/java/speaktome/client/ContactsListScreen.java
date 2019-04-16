package speaktome.client;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class ContactsListScreen extends CommunicationScreen{
    protected RecyclerView rv;

    protected ArrayList<ContactChatDetails> contactsDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
        Initializes recyclerview object
        Input: None
        Output: None
     */
    protected void initRecyclerView(){
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this.contactsDetails, this, this.srcPhone, this.sqlDB);
        this.rv.setAdapter(adapter);
        this.rv.setLayoutManager(new LinearLayoutManager(this));
    }

    /*
        Function retrieves contacts
        Input: Current screen context
        Output: List of contacts
     */
    protected ArrayList<ContactChatDetails> getContacts() {
        ContentResolver cr = this.getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        ArrayList<ContactChatDetails> contacts = new ArrayList<ContactChatDetails>();
        ContactChatDetails contactDetails = null;
        if(cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null); //TODO: CHECK IF WE NEED SECOND QUERY
                    if (pCur.moveToNext()) { // TODO: check if number has the application
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactNumber = contactNumber.replaceAll("\\D+","");
                        if (contactNumber.startsWith("972")) {
                            contactNumber = contactNumber.replaceFirst("972", "0");
                        }
                        String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        contactDetails = new ContactChatDetails(null, contactName, contactNumber, "");
                        contacts.add(contactDetails);
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext());
        }
        return contacts;
    }
}
