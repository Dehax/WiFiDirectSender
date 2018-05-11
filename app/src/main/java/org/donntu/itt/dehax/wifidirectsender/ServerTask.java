package org.donntu.itt.dehax.wifidirectsender;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTask extends AsyncTask<Void, Void, String> {

    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    ServerTask(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            ServerSocket serverSocket = new ServerSocket(8666);
            Socket serverClient = serverSocket.accept();
            InputStream inputStream = serverClient.getInputStream();
            byte[] buffer = new byte[1024];
            int count = inputStream.read(buffer);
            byte[] result = new byte[count];

            System.arraycopy(buffer, 0, result, 0, result.length);
            serverSocket.close();

            return new String(result, "UTF-8");
        } catch (IOException e) {
            Log.e("DEHAX", "IO Exception.", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        Toast.makeText(mContext, "Received: " + result, Toast.LENGTH_LONG).show();
    }
}