package hello;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;


import java.util.*;


@RestController
@RequestMapping("/vnfs")
public class HelloController {

    Logger logger = LoggerFactory.getLogger(HelloController.class);
    
    @ResponseBody
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Map<String, Map<String, String>> getVnfDetails() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, String>> map = new HashMap<>();
        try {
            Jedis jedis = new Jedis("localhost");
            Set<String> names = jedis.keys("*");
            java.util.Iterator<String> it = names.iterator();
            while (it.hasNext()) {
                String s = it.next();
                Map<String, String> data = objectMapper.readValue(jedis.get(s), new TypeReference<Map<String, String>>() {
                });
                map.put(s, data);

            }
        }catch (Exception e){
            logger.error("Could not parse redis data");
        }

        return map;
    }

}
