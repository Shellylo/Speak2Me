package speaktome.client;

import android.media.Image;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemDetails {
    private ImageView contactImage;
    private String contactName;
    private String contactPhone;
    private String message;

    public ItemDetails(ImageView contactImage, String contactName, String contactPhone, String message) {
        this.contactImage = contactImage;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.message = message;
    }

    public ImageView getContactImage() {
        return contactImage;
    }

    public void setContactImage(ImageView contactImage) {
        this.contactImage = contactImage;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
