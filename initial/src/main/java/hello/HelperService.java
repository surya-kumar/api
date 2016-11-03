package hello;
import java.io.*;
import java.net.*;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.*;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.net.ssl.HttpsURLConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;



import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;



import java.util.*;



/**
 * Created by surya.kumar on 15/07/16.
 */
public class HelperService {
    public String getData(String urlToRead) throws Exception {
        String sResponse="";
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urlToRead);

            HttpResponse response = httpClient.execute(httpGet);
            System.out.println(urlToRead);
            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            response.getEntity().writeTo(outstream);
            byte[] responseBody = outstream.toByteArray();
            sResponse = new String(responseBody);
            System.out.println("RESPONSE=" + sResponse.trim());
            System.out.println(response.getStatusLine().getStatusCode());
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
        return sResponse;
    }






//    public  String writeData(Map<String, JSONObject> map)  {
//        Jedis jedis = new Jedis("localhost");
//        Pipeline p = jedis.pipelined();
//        for(Map.Entry<String, JSONObject> entry : map.entrySet())
//            p.set(entry.getKey(), entry.getValue().toString());
//
//        p.sync();
//        return "Done writing to redis";
//    }




//    public  void flushall()  {
//        Jedis jedis = new Jedis("localhost");
//        jedis.flushAll();
//
//    }



    public static String PostData(String urlToRead,String id) throws Exception {
        String output="";
        try {

            URL url = new URL(urlToRead);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("username", "surya.kumar");
            conn.setRequestProperty("password", "surya@123");

            String input = "{\"query_name\": \"es_shipment_to_order_item\",\"parameters\": {\"VAR_SHIPMENT_ID\": \"" + id + "\"}}";
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
//System.out.println(new BufferedReader(new InputStreamReader((conn.getInputStream()))));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            output = br.readLine();


            conn.disconnect();
        }
        catch (Exception ex2){
            ex2.getStackTrace();
        }
        return output;
    }





}
