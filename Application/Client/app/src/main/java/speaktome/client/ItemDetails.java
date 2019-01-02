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

    /*
        Function returns contact image
        Input: None
        Output: contact image
     */
    public ImageView getContactImage() {
        return contactImage;
    }

    /*
        Function sets new contact image
        Input: Contact image
        Output: None
     */
    public void setContactImage(ImageView contactImage) {
        this.contactImage = contactImage;
    }

    /*
        Function returns contact name
        Input: None
        Output: Contact name
     */
    public String getContactName() {
        return contactName;
    }

    /*
        Function sets new contact name
        Input: Contact name
        Output: None
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /*
        Function returns contact phone number
        Input: None
        Output: Contact phone number
     */
    public String getContactPhone() {
        return contactPhone;
    }

    /*
        Function sets contact phone number
        Input: Contact phone number
        Output: None
     */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    /*
        Function returns last message from contact
        Input: None
        Output: The message
     */
    public String getMessage() {
        return message;
    }

    /*
        Function sets last message from contact
        Input: The message
        Output: None
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
