package hello;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;

import com.mongodb.*;
import com.mongodb.client.*;

import org.bson.Document;
import com.mongodb.util.JSON;


import java.util.*;


@RestController
@RequestMapping("/vnfs")
public class HelloController {

    Logger logger = LoggerFactory.getLogger(HelloController.class);
    
//    @ResponseBody
//    @RequestMapping(value = "/all", method = RequestMethod.GET)
//    public Map<String, Map<String, String>> getVnfDetails() {
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, Map<String, String>> map = new HashMap<>();
//        try {
//            Jedis jedis = new Jedis("localhost");
//            Set<String> names = jedis.keys("*");
//            java.util.Iterator<String> it = names.iterator();
//            while (it.hasNext()) {
//                String s = it.next();
//                Map<String, String> data = objectMapper.readValue(jedis.get(s), new TypeReference<Map<String, String>>() {
//                });
//                map.put(s, data);
//
//            }
//        }catch (Exception e){
//            logger.error("Could not parse redis data");
//        }
//
//        return map;
//    }
//
//    @ResponseBody
//    @RequestMapping(value = "/rediswrite", method = RequestMethod.GET)
//    public String redisWrite(){
//        String res="h";
//
//        try {
//            String mhUrl = "http://10.47.1.55/debug/sellerToMhMappings";
//            HelperService helper = new HelperService();
//            String sellerMh=helper.getData(mhUrl);
//            Jedis jedis = new Jedis("localhost");
//            System.out.println("Connection to server sucessfully");
//            //check whether server is running or not
//            System.out.println("Server is running: "+jedis.ping());
//            VaradhiData vd = new VaradhiData();
//            res=vd.WriteDataToRedis(sellerMh);
//            System.out.println("response"+res);
//        }
//        catch (Exception ec){
//            logger.error("Could not parse redis data");
//        }
//
//    return  res;
//    }


    @ResponseBody
    @RequestMapping(value = "/getallVnf", method = RequestMethod.GET)
    public BasicDBList VnfResponse(){
        BasicDBList list = new BasicDBList();

        try {
            MongoClient client = new MongoClient(new ServerAddress("10.33.149.58", 27017));

            MongoDatabase db = client.getDatabase("debug");

            MongoCollection<Document> collection = db.getCollection("vnfInfo");

            MongoCursor<Document> iterator = collection.find().iterator();


            while (iterator.hasNext()) {
                Document doc = iterator.next();
                list.add(doc);
            }

        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return list;
    }


    @ResponseBody
    @RequestMapping(value = "/deleteAllProcessed", method = RequestMethod.GET)
    public String removeAll(){
         HelperService hs = new HelperService();

        try {
            MongoClient client = new MongoClient(new ServerAddress("10.33.149.58", 27017));

            MongoDatabase db = client.getDatabase("debug");

            MongoCollection<Document> collection = db.getCollection("vnfInfo");

            MongoCursor<Document> iterator = collection.find().iterator();




            while (iterator.hasNext()) {
                Document doc = iterator.next();
                String shipment= doc.get("shipmentId").toString();
                JSON json =new JSON();
//                String serialize = json.serialize(doc.get("request"));

                JSONObject requestParserObject = new JSONObject(json.serialize(doc.get("request")));
                String profile = requestParserObject.getString("serviceProfile");
                System.out.println(shipment+","+profile);
                String stats1url="http://10.85.50.117/shipments/get_all_shipment_details?merchant_reference_id=S211742649";
                String shipmentStatusResponse = hs.getData(stats1url);
                System.out.println(shipmentStatusResponse);

                if(shipmentStatusResponse != null && !shipmentStatusResponse.isEmpty()){
                    JSONArray responseArray = new JSONArray(shipmentStatusResponse);
                    for (int i=0;i<responseArray.length();i++) {
                        String s = responseArray.getString(i);
                        JSONObject reader = new JSONObject(s);
                        String message_id = reader.getString("shipment");


                    }

                }




//                System.out.println(serialize);

//                System.out.println(request);


//                JSONObject requestParserObject = new JSONObject(doc.get("request").toString());
////                System.out.println(requestParserObject);
//                String profile = requestParserObject.getString("serviceProfile");
//                System.out.println(shipment+","+profile);

            }

        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return "hi";
    }



}


