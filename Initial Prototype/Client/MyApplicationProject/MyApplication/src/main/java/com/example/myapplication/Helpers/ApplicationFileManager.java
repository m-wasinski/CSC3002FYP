package com.example.myapplication.Helpers;

import android.os.Environment;
import android.util.Log;

import com.example.myapplication.Constants.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Created by Michal on 15/11/13.
 */
public  class ApplicationFileManager {

    public boolean FolderExists()
    {
        return folderExists;
    }

    public boolean CookieExists()
    {
        return cookieExists;
    }

    private File applicationFolder;
    private File externalStorage;
    private File sessionId;
    private String state;
    private Boolean folderExists;
    private Boolean cookieExists;
    public ApplicationFileManager()
    {
        externalStorage = Environment.getExternalStorageDirectory();
        applicationFolder = new File(externalStorage.getAbsolutePath() + "/"+ Constants.APP_FOLDER_NAME);
        sessionId = new File(applicationFolder.getAbsolutePath()+"/"+Constants.SESSION_ID_FILE_NAME);
        String state = Environment.getExternalStorageState();

        folderExists = applicationFolder.exists();
        cookieExists = sessionId.exists();

        Log.e("External Storage", externalStorage.getAbsolutePath());
        Log.e("Application Folder", applicationFolder.getAbsolutePath());
        Log.e("Session cookie file", sessionId.getAbsolutePath());
    }

    public boolean CheckIfCookieExists()
    {
        return true;
    }

    public boolean MakeApplicationDirectory()
    {
        return applicationFolder.mkdirs();
    }

    public boolean CreateEmptyCookie()
    {
        try {
            sessionId.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(sessionId, false);
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e("Error while creating file", e.toString());
        }

        return true;
    }

    public boolean CreateSessionCookie(String value)
    {

        try {
            if (cookieExists)
            {
                sessionId.delete();
                sessionId.createNewFile();
            }
            //FileOutputStream fileOutputStream = new FileOutputStream(sessionId, false);

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(sessionId), "UTF8"));
            out.append(value);
            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, value);
            //outputStreamWriter.write(value);
            //outputStreamWriter.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e("Error while creating file", e.toString());
        }


        return true;
    }

    public String GetTokenValue()
    {
        String str = "";

        StringBuilder sb = new StringBuilder();

        try {

            BufferedReader in = new BufferedReader(new FileReader(sessionId));
            String line = in.readLine();

            while (line != null) {
                sb.append(line);
                line = in.readLine();
            }

            in.close();
        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println(e.getMessage());
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.println("COOKIE VALUE: " + sb.toString());
        return sb.toString().replace("\n", "");
    }

    public boolean WriteToCookie()
    {
        return true;
    }
}
