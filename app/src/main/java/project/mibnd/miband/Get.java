package project.mibnd.miband;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class Get {
    OkHttpClient client = new OkHttpClient();

    public void run(String url, Callback callback) {

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }
}