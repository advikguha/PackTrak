package com.algorhythms.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Makes alerts and toasts.
 * @author Algorhythms
 */
public class AlertBuilder {
    /**
     * Displays a simple Toast to the screen quickly.
     * @param message The message to display.
     */
    public static void toaster(String message) {
        Toast.makeText(null, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays an alert message to the screen.
     * @param ctx The context for the alert.
     * @param title The title of the alert.
     * @param message The message body.
     */
    public static void showAlert(Context ctx, String title, String message) {
        new AlertDialog.Builder(ctx)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                }).show();
    }
}