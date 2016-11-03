package hello;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by surya.kumar on 15/07/16.
 */
public class VaradhiData {

    private final ExecutorService threadPool = Executors.newFixedThreadPool(20);
    public  String WriteDataToRedis(String Mh) throws Exception  {
        try {
             HelperService hs = new HelperService();
            String url = "http://10.85.50.10/queues/vendor_marketplace_production/messages?sidelined=true&offset=0&limit=1000000";

            String Data = hs.getData(url);
            hs.flushall();
            final String StatusUrl = "http://10.85.50.55:9000/query/execute/";
            JSONArray newJArray = new JSONArray(Data);
            Map<String, JSONObject> requestMap = new HashMap<>();
            Map<String, Future<String>> responseMap = new HashMap<>();
            for (int i=0;i<newJArray.length();i++) {

                String s = newJArray.getString(i);
                JSONObject reader = new JSONObject(s);
                String message_id = reader.getString("message_id");
                final String merchantReferenceId = reader.getString("group_id");
                String http_response_code = reader.getString("http_response_code");
                String Message = reader.getString("message");
                JSONObject MessageReader = new JSONObject(Message);
                String shipments = MessageReader.getString("shipments");
                JSONArray shipmentArray = new JSONArray(shipments);
                requestMap.put(merchantReferenceId, reader);



//                for (int j = 0; j < shipmentArray.length(); j++) {
//                    String shipment_param = shipmentArray.getString(j);
//                    JSONObject shipmentReader = new JSONObject(shipment_param);
                responseMap.put(merchantReferenceId, threadPool.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        if (merchantReferenceId.startsWith("R-") || merchantReferenceId.startsWith("R1-") ) {
                            merchantReferenceId.replace("R-", "");
                        }
                        return hs.PostData(StatusUrl, merchantReferenceId);
                    }
                }));

//                }
            }



//            Map<String , Map<String, String>> dataMap = new HashMap<>();
            Map<String ,JSONObject> dataMap = new HashMap<>();
            for(Map.Entry<String, JSONObject> entry : requestMap.entrySet()) {
                JSONObject reader = entry.getValue();
                String message_id = reader.getString("message_id");
                String merchantReferenceId = reader.getString("group_id");
                String http_response_code = reader.getString("http_response_code");
                String Message = reader.getString("message");

                JSONObject MessageReader = new JSONObject(Message);
                String shipments = MessageReader.getString("shipments");
                JSONArray shipmentArray = new JSONArray(shipments);

                for (int j = 0; j < shipmentArray.length(); j++) {
                    String shipment_param = shipmentArray.getString(j);
                    JSONObject shipmentReader = new JSONObject(shipment_param);
                    String origin_pincode = shipmentReader.getString("origin_pincode");
                    JSONObject mhReader = new JSONObject(Mh);
                    String mhMap=mhReader.getString("mappings");
                    JSONObject mhMapReader = new JSONObject(mhMap);
                    String mhPincode = "";
                    if(mhMapReader.has(origin_pincode)){
                        mhPincode =  mhMapReader.getString(origin_pincode);
                    }


                    String destination_pincode = shipmentReader.getString("destination_pincode");
                    Future<String> futureResponse = responseMap.get(entry.getKey());
                    String orderId = "NA";
                    String orderStatus = "NA";

                    if(futureResponse.get() !="") {
                        JSONObject Readerstatus = new JSONObject(futureResponse.get());
                        String hit1 = Readerstatus.getString("hits");
                        JSONObject Readerhits = new JSONObject(hit1);
                        String hit2 = Readerhits.getString("hits");

                        JSONArray hitArray = new JSONArray(hit2);
                        if (hitArray.length() != 0) {
                            String hit3 = hitArray.getString(0);
                            JSONObject req_fields = new JSONObject(hit3);
                            String fields = req_fields.getString("fields");
                            JSONObject req_fields1 = new JSONObject(fields);
                            String orderOb = req_fields1.getString("order_id");
                            JSONArray orderArray = new JSONArray(orderOb);
                            orderId = orderArray.getString(0);
                            String statusOb = req_fields1.getString("units.state");
                            JSONArray statusArray = new JSONArray(statusOb);
                            orderStatus = statusArray.getString(0);

                        }
                    }
                    //  System.out.println(fields);

                    String categories="C";
                    String seller_id = shipmentReader.getString("seller_id");
                    String size = shipmentReader.getString("size");
                    String hand_to_hand_pickup = shipmentReader.getString("hand_to_hand_pickup");
                    String eklBag = shipmentReader.getString("ekl_databag");
                    JSONObject dataBag = new JSONObject(eklBag);
                    String tier = dataBag.getString("lpe_tier");
                    String handlingAttributes="";
                    List<String> handle = new ArrayList<String>();


                    if (dataBag.has("attributes")){
                        handlingAttributes = dataBag.getString("attributes").replace("[","").replace("]","");

                        handle = new ArrayList<String>(Arrays.asList(handlingAttributes.split(",")));

                    }


                    String is_fragile="";
                    String amount_to_collect = shipmentReader.getString("amount_to_collect");
                    String shipmentItems = shipmentReader.getString("shipment_items");
                    JSONArray shipmentItemArray = new JSONArray(shipmentItems);
                    for (int k = 0; k < shipmentItemArray.length(); k++) {
                        String verticalsLoops = shipmentItemArray.getString(k);
                        JSONObject verticalsReader = new JSONObject(verticalsLoops);
                        String vertical=verticalsReader.getString("vertical");
                        String categoryUrl="http://10.47.1.55/postal_codes/category?vertical="+vertical;
                        String category= hs.getData(categoryUrl);
                        categories = categories+"-"+category;
                        is_fragile=verticalsReader.getString("is_fragile");

                    }
                    String newTier="";
                    if (tier.equals("REGULAR")){
                        newTier="REGULAR";
                        if ( (handle.contains("dangerous") || is_fragile.equals("true"))){
                            newTier="SLOW";
                        }

                    }
                    if( tier.equals("ECONOMY")){
                        newTier="ECONOMY";
                    }
                    if(tier.equals("EXPRESS")){
                        newTier="NDD";
                        if ( (handle.contains("dangerous") || is_fragile.equals("true"))){
                            newTier="NDD_DG";
                        }

                    }

                    Map<String, String> redisMap = new HashMap<>();
                    redisMap.put("message_id", message_id);
                    redisMap.put("http_response_code", http_response_code);
                    redisMap.put("origin_pincode", origin_pincode);
                    redisMap.put("destination_pincode", destination_pincode);
                    redisMap.put("seller_id", seller_id);
                    redisMap.put("size", size);
                    redisMap.put("hand_to_hand_pickup", hand_to_hand_pickup);
                    redisMap.put("amount_to_collect", amount_to_collect);
                    redisMap.put("order", orderId);
                    redisMap.put("order_status", orderStatus);
                    redisMap.put("category", categories);
                    redisMap.put("Tier", newTier);
                    redisMap.put("Mh", mhPincode);



                    JSONObject redisjson= new JSONObject(redisMap);
                    dataMap.put(merchantReferenceId, redisjson);



                }

            }


            System.out.println(dataMap);
            String writeResponse1=hs.writeData(dataMap);
            System.out.println(writeResponse1);

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "success";
    }

}
