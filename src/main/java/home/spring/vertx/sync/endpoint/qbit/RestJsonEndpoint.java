package home.spring.vertx.sync.endpoint.qbit;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by alex on 10/2/2015.
 */
@RequestMapping("/json")
public class RestJsonEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(RestJsonEndpoint.class);

    private static final  String responseString = "{name: 'Max, lastname: 'Mustermann', occupation: 'developer'}";

    @RequestMapping(value = "/check",  method = RequestMethod.GET)
    public String check(){
//        logger.info("jsony");
//        System.out.println("json!");
        return responseString;
    }
}
