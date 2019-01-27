package speaktome.client;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MySqliteDatabase extends SQLiteOpenHelper {
    private static final String MESSAGES_TABLE_NAME = "MESSAGES";
    private static final String MESSAGES_COLUMN_MESSAGE_ID = "MESSAGE_ID"; //Integer
    private static final String MESSAGES_COLUMN_PHONE_CHAT = "PHONE_CHAT"; //Text: the phone number of the other person in the chat
    private static final String MESSAGES_COLUMN_IS_MINE = "IS_MINE"; //Integer: 0 - the message is not mine, 1 - the message is mine
    private static final String MESSAGES_COLUMN_CONTENT = "CONTENT"; //Text: the message itself
    private static final String MESSAGES_COLUMN_IS_IN_CHAT = "IS_IN_CHAT"; //Integer: 0 - the message is not in chat, 1 - the message is in chat

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
                        MESSAGES_COLUMN_CONTENT + " TEXT NOT NULL, " +
                        MESSAGES_COLUMN_IS_IN_CHAT + " INTEGER NOT NULL" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE_NAME);
        onCreate(db);
    }

    /*
        Function inserts message into the sqlite database
        Input: the message
        Output: message id
     */
    public int insertMessage (Message msg) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Insert message into database
        contentValues.put(MESSAGES_COLUMN_PHONE_CHAT, msg.getPhone());
        contentValues.put(MESSAGES_COLUMN_IS_MINE, (msg.isMine() ? 1 : 0));
        contentValues.put(MESSAGES_COLUMN_CONTENT, msg.getContent());
        contentValues.put(MESSAGES_COLUMN_IS_IN_CHAT, (msg.isInChat() ? 1 : 0));
        db.insert(MESSAGES_TABLE_NAME, null, contentValues);

        // Receive and return message id
        Cursor messagesRows = db.rawQuery("SELECT " + MESSAGES_COLUMN_MESSAGE_ID + " FROM " + MESSAGES_TABLE_NAME + " ORDER BY " + MESSAGES_COLUMN_MESSAGE_ID + " DESC LIMIT 1", null);
        messagesRows.moveToFirst(); // Go to first row
        return messagesRows.getInt(messagesRows.getColumnIndex(MESSAGES_COLUMN_MESSAGE_ID));
    }

    /*
        Function returns every first message from each conversation
        Input: None
        Output: The message + some details
     */
    public ArrayList<ContactChatDetails> getTopMessages() {
        ArrayList<ContactChatDetails> ret = new ArrayList<ContactChatDetails>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor res =  db.rawQuery( "SELECT " + MESSAGES_COLUMN_PHONE_CHAT + ", " + MESSAGES_COLUMN_CONTENT +
                " FROM (SELECT * " +
                        " FROM " + MESSAGES_TABLE_NAME +
                        " WHERE " + MESSAGES_COLUMN_IS_IN_CHAT + " = 1" + ")" +
                " GROUP BY " + MESSAGES_COLUMN_PHONE_CHAT +
                " HAVING MAX(" + MESSAGES_COLUMN_MESSAGE_ID + ")", null);
        res.moveToFirst();

        ContactChatDetails id;
        while(res.isAfterLast() == false){
            id = new ContactChatDetails(null,
                    res.getString(res.getColumnIndex(MESSAGES_COLUMN_PHONE_CHAT)),
                    res.getString(res.getColumnIndex(MESSAGES_COLUMN_PHONE_CHAT)),
                    res.getString(res.getColumnIndex(MESSAGES_COLUMN_CONTENT)));
            ret.add(id);
            res.moveToNext();
        }
        return ret;
    }

    /*
        Function receives user messages from / to specified number
        Input: None
        Output: Messages array
     */
    public ArrayList<Message> getMessages (String phoneNum, boolean isInChat) {
        ArrayList<Message> messagesList = new ArrayList<Message>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor messagesRows = db.rawQuery("SELECT * FROM " + MESSAGES_TABLE_NAME + " WHERE " + MESSAGES_COLUMN_PHONE_CHAT + " = \"" + phoneNum + "\" AND " + MESSAGES_COLUMN_IS_IN_CHAT + " = " + (isInChat ? 1 : 0), null);
        messagesRows.moveToFirst(); // Go to first row

        Message msg = null;
        while (!messagesRows.isAfterLast())
        {
            msg = new Message( messagesRows.getInt(messagesRows.getColumnIndex(MESSAGES_COLUMN_MESSAGE_ID)),
                               messagesRows.getString(messagesRows.getColumnIndex(MESSAGES_COLUMN_PHONE_CHAT)),
                        messagesRows.getInt(messagesRows.getColumnIndex(MESSAGES_COLUMN_IS_MINE)) == 1,
                               messagesRows.getString(messagesRows.getColumnIndex(MESSAGES_COLUMN_CONTENT)),
                       messagesRows.getInt(messagesRows.getColumnIndex((MESSAGES_COLUMN_IS_IN_CHAT)))== 1);
            messagesList.add(msg);
            messagesRows.moveToNext();
        }
        return messagesList;
    }

    /*
        Function removes a message from database
        Input: message id (in database)
        Output: None
     */
    public void removeMessage(int messageId)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(MESSAGES_TABLE_NAME, MESSAGES_COLUMN_MESSAGE_ID + "=" + messageId, null);
    }
}
