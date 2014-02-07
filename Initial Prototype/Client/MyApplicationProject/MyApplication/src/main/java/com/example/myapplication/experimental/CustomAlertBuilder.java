package com.example.myapplication.experimental;

import android.app.Dialog;
import android.content.Context;

import com.example.myapplication.R;

/**
 * Created by Michal on 07/02/14.
 */
public class CustomAlertBuilder {
    public static Dialog getSendFriendRequestDialog(Context context)
    {
        // Show the journey requests dialog.
        final Dialog sendFriendRequestDialog = new Dialog(context);
        sendFriendRequestDialog.setContentView(R.layout.alert_dialog_send_friend_request);
        sendFriendRequestDialog.setTitle("New friend request");

        return sendFriendRequestDialog;
    }
}
