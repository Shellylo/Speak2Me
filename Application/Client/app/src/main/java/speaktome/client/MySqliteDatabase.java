package speaktome.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MySqliteDatabase extends SQLiteOpenHelper {
    private static final String MESSAGES_TABLE_NAME = "MESSAGES";
    private static final String MESSAGES_COLUMN_MESSAGE_ID = "MESSAGE_ID"; //Integer
    private static final String MESSAGES_COLUMN_PHONE_CHAT = "PHONE_CHAT"; //Text: the phone number of the other person in the chat
    private static final String MESSAGES_COLUMN_IS_MINE = "IS_MINE"; //Integer: 0 - the message is not mine, 1 - the message is mine
    private static final String MESSAGES_COLUMN_CONTENT = "CONTENT"; //Text: the message itself

    /*
    Creates a MySqliteDatabase object
    Input:
        context
        name - the phone number of the logged in user
     */
    public MySqliteDatabase(Context context, String name) {
        super(context, name + ".db", null, 1);
    }

    /*
        Function activated only for the first time the database created
        Input: database
        Output: None
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + MESSAGES_TABLE_NAME + "(" +
                        MESSAGES_COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        MESSAGES_COLUMN_PHONE_CHAT + " TEXT NOT NULL, " +
                        MESSAGES_COLUMN_IS_MINE + " INTEGER NOT NULL, " +
                        MESSAGES_COLUMN_CONTENT + " TEXT NOT NULL" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertMessage (Message msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.insert("contacts", null, contentValues);
        return true;
    }

    /*
        Function receives user messages from / to specified number
        Input: None
        Output: Messages array
     */
    public ArrayList<Message> getMessages (String phoneNum) {
        ArrayList<Message> messagesList = new ArrayList<Message>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor messagesRows = db.rawQuery("SELECT * FROM " + MESSAGES_TABLE_NAME + " WHERE " + MESSAGES_COLUMN_PHONE_CHAT + " = \"" + phoneNum + "\"", null);
        messagesRows.moveToFirst(); // Go to first row

        Message msg = null;
        while (!messagesRows.isAfterLast())
        {
            msg = new Message( messagesRows.getString(messagesRows.getColumnIndex(MESSAGES_COLUMN_PHONE_CHAT)),
                        messagesRows.getInt(messagesRows.getColumnIndex(MESSAGES_COLUMN_IS_MINE)) == 1,
                               messagesRows.getString(messagesRows.getColumnIndex(MESSAGES_COLUMN_CONTENT)));
            messagesList.add(msg);
        }
        return messagesList;
    }
}
