package com.samagra.commons.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ListView;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;

import com.samagra.commons.R;

import timber.log.Timber;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public final class AlertDialogUtils {

    private AlertDialogUtils() {
    }

    public static int getDimen(Context context, @DimenRes int dimenResId) {
        return context.getResources().getDimensionPixelSize(dimenResId);
    }


    /**
     * List View used with actions
     *
     * @param context UI Context (Activity/Fragment)
     * @return ListView with white divider between items to prevent accidental taps
     */
    @NonNull
    public static ListView createActionListView(@NonNull Context context) {
        int dividerHeight = getDimen(context, R.dimen.divider_accidental_tap);
        ListView listView = new ListView(context);
        listView.setPadding(0, dividerHeight, 0, 0);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listView.setDividerHeight(dividerHeight);
        return listView;
    }

    /**
     * Ensures that a dialog is shown safely and doesn't causes a crash. Useful in the event
     * of a screen rotation, async operations or activity navigation.
     *
     * @param dialog   that needs to be shown
     * @param activity that has the dialog
     */
    public static void showDialog(Dialog dialog, Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (dialog == null || dialog.isShowing()) {
            return;
        }

        try {
            dialog.show();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Shows a confirm/cancel dialog for deleting the current repeat group.
     */

    /**
     * Ensures that a dialog is dismissed safely and doesn't causes a crash. Useful in the event
     * of a screen rotation, async operations or activity navigation.
     *
     * @param dialog   that needs to be shown
     * @param activity that has the dialog
     */
    public static void dismissDialog(Dialog dialog, Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (dialog == null || !dialog.isShowing()) {
            return;
        }

        try {
            dialog.dismiss();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Creates an error dialog on an activity
     *
     * @param errorMsg The message to show on the dialog box
     * @param shouldExit Finish the activity if Ok is clicked
     */
    public static Dialog createErrorDialog(@NonNull Activity activity, String errorMsg, final boolean shouldExit) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = (dialog, i) -> {
            switch (i) {
                case BUTTON_POSITIVE:
                    if (shouldExit) {
                        activity.finish();
                    }
                    break;
            }
        };
        alertDialog.setCancelable(false);
        alertDialog.setButton(activity.getString(R.string.ok), errorListener);

        return alertDialog;
    }
}
