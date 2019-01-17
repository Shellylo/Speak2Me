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

     */
    protected void updateError(TextView newError)
    {
        setVisibility(View.INVISIBLE); // remove old error
        this.currentError = newError;
        setVisibility(View.VISIBLE); // display new error
    }

    /*

     */
    protected void setVisibility(int visibility)
    {
        if (this.currentError != null) {
            this.currentError.setVisibility(visibility);
        }
    }
}
