package speaktome.client;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ErrorDisplayerScreen extends GeneralScreen {

    protected TextView currentError;

    public ErrorDisplayerScreen()
    {
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
