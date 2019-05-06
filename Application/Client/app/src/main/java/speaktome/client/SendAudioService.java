package speaktome.client;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import static speaktome.client.App.CHANNEL_ID;

public class SendAudioService extends Service {
    private Client client;

    @Override
    public void onCreate() {
        this.client = ClientHandler.getClient();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Sending Audio")
                .setContentText("Sending audio to server for speech to text")
                .setSmallIcon(android.R.drawable.presence_audio_online)
                .build();

        startForeground(1, notification);

        sendRecord(intent);

        stopSelf();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void sendRecord(Intent intent) {
        try {

            File file = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + ChatScreen.RecordTask.AUDIO_PATH);
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
            String content = Base64.encodeToString(bytes, Base64.DEFAULT);

            // Prepare audio message request
            JSONObject sendRecordReq = new JSONObject();
            sendRecordReq.put("code", Codes.SPEECH_TO_TEXT_CODE);
            sendRecordReq.put("src_phone", intent.getStringExtra("src_phone"));
            sendRecordReq.put("dst_phone", intent.getStringExtra("dst_phone"));
            sendRecordReq.put("content", content);

            // Send message request
            this.client.send(sendRecordReq);
        }
        catch (Exception e) {
            System.out.println(e);
        }


    }
}
