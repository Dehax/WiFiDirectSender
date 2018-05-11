package org.donntu.itt.dehax.wifidirectsender;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class ClientTask extends AsyncTask<InetAddress, Void, String> {

    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    ClientTask(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(InetAddress... addresses) {
        int timeout = 10000;
        int port = 8666;

        InetSocketAddress socketAddress = new InetSocketAddress(addresses[0], port);
        String currentDateTimeString = "error";

        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(socketAddress, timeout);
            OutputStream outputStream = socket.getOutputStream();
            currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            outputStream.write(currentDateTimeString.getBytes("UTF-8"));
            outputStream.close();
        } catch (IOException e) {
            Log.e("DEHAX", "IO Exception.", e);
        } finally {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e("DEHAX", "IO Exception.", e);
                }
            }
        }
        return currentDateTimeString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Toast.makeText(mContext, "Sent: " + s, Toast.LENGTH_LONG).show();
    }
}
