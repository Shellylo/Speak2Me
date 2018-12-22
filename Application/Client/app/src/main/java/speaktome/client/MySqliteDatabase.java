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

    /*
        Function inserts message into the sqlite database
        Input: the message
        Output: None
     */
    public void insertMessage (Message msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGES_COLUMN_PHONE_CHAT, msg.getPhone());
        contentValues.put(MESSAGES_COLUMN_IS_MINE, (msg.isMine() ? 1 : 0));
        contentValues.put(MESSAGES_COLUMN_CONTENT, msg.getContent());
        db.insert(MESSAGES_TABLE_NAME, null, contentValues);
    }

    /*
        Function returns every first message from each conversation
        Input: None
        Output: The message + some details
     */
    public ArrayList<ItemDetails> getTopMessages() {
        ArrayList<ItemDetails> ret = new ArrayList<ItemDetails>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "SELECT " + MESSAGES_COLUMN_PHONE_CHAT + ", " + MESSAGES_COLUMN_CONTENT +
                " FROM (SELECT * " +
                        " FROM " + MESSAGES_TABLE_NAME + ")" +
                " GROUP BY " + MESSAGES_COLUMN_PHONE_CHAT +
                " HAVING MAX(" + MESSAGES_COLUMN_MESSAGE_ID + ")", null);
        res.moveToFirst();

        ItemDetails id;
        while(res.isAfterLast() == false){
            id = new ItemDetails(null,
                    res.getString(res.getColumnIndex(MESSAGES_COLUMN_PHONE_CHAT)),
                    res.getString(res.getColumnIndex(MESSAGES_COLUMN_PHONE_CHAT)),
                    res.getString(res.getColumnIndex(MESSAGES_COLUMN_CONTENT)));
            ret.add(id);
            res.moveToNext();
        }
        return ret;
    }
}
