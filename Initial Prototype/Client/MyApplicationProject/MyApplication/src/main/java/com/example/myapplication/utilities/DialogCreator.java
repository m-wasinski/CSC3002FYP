package com.example.myapplication.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.example.myapplication.activities.activities.ProfileViewerActivity;
import com.example.myapplication.activities.activities.SendFriendRequestDialogActivity;
import com.example.myapplication.constants.IntentConstants;
import com.example.myapplication.domain_objects.User;
import com.google.gson.Gson;

/**
 * Created by Michal on 19/02/14.
 */
public class DialogCreator {

    public static void ShowProfileOptionsDialog(final Context context, final User user)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(user.getUserName());
        CharSequence userOptions[] = new CharSequence[] {"Show profile", "Send friend request"};
        builder.setItems(userOptions, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case 0:
                        context.startActivity(new Intent(context, ProfileViewerActivity.class).putExtra(IntentConstants.USER, new Gson().toJson(user)));
                        break;
                    case 1:
                        context.startActivity(new Intent(context, SendFriendRequestDialogActivity.class).putExtra(IntentConstants.USER, new Gson().toJson(user)));
                        break;
                }
            }
        });

        builder.show();
    }
}
