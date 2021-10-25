package Handlers;

import Events.LongEvent;
import com.lmax.disruptor.EventHandler;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class OnlineLogHandler implements EventHandler<LongEvent> {
    private final String url;
    private final HttpClient httpclient;

    public OnlineLogHandler(String localLogFile){
        super();
        this.httpclient = HttpClients.createDefault();
        this.url = localLogFile;
    }


    @Override
    public void onEvent(LongEvent longEvent, long l, boolean b) throws Exception {
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair("value", Long.toString(longEvent.get())));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        this.httpclient.execute(httppost);
    }
}
