package boyw165.com.my_mosaic_stickers;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class SomeDialogFragment extends DialogFragment {
    private static final String TAG = "SomeDialogFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("message")
               .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                       Log.d(TAG, "ok");
                   }
               })
               .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                       Log.d(TAG, "cancel");
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
