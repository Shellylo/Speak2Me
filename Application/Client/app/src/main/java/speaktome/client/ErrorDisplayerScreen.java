package speaktome.client;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ErrorDisplayerScreen extends GeneralScreen {
    protected TextView currentError;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.currentError = null;
    }


    /*
        Updates current error (changes previous error if existing to be invisible,
        and received error to be visible)
        Input: Error to display
        Output: None
     */
    protected void updateError(TextView newError)
    {
        setVisibility(View.INVISIBLE); // remove old error
        this.currentError = newError;
        setVisibility(View.VISIBLE); // display new error
    }

    /*
        Sets current error visibility according to input
        Input: Visible / Invisible
        Output: None
     */
    protected void setVisibility(int visibility)
    {
        if (this.currentError != null) {
            this.currentError.setVisibility(visibility);
        }
    }
}
