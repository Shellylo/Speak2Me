package speaktome.client;

public class Contact {
    private String name;
    private String phoneNum;

    public Contact(String name, String phoneNum) {
        this.name = name;
        this.phoneNum = phoneNum;
    }

    /*
        Function returns contact's name
        Input: None
        Output: name
     */
    public String getName() {
        return this.name;
    }

    /*
        Function edits contact's name
        Input: contact's name
        Output: None
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
        Function returns contact's phone number
        Input: None
        Output: phone number
     */
    public String getPhoneNum() {
        return this.phoneNum;
    }

    /*
        Function sets contact's phone number
        Input: phone number
        Output: None
     */
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

}
