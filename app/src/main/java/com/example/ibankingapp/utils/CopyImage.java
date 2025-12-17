package com.example.ibankingapp.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyImage {
    public static Uri copyUriToCache(Context context, Uri uri) throws Exception {
        InputStream input = context.getContentResolver().openInputStream(uri);



        File file = new File(context.getCacheDir(),
                "avatar_" + System.currentTimeMillis() + ".jpg");

        OutputStream output = new FileOutputStream(file);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }

        input.close();
        output.close();

        return Uri.fromFile(file);
    }

}
