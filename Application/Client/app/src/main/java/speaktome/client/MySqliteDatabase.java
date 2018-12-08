package speaktome.client;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
}
