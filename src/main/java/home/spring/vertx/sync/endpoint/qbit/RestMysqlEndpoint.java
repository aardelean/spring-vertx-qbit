package home.spring.vertx.sync.endpoint.qbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import home.spring.vertx.sync.dao.EmployeeDao;
import io.advantageous.qbit.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by alex on 9/27/2015.
 */
@RequestMapping("/mysql")
public class RestMysqlEndpoint {

    @Autowired
    private EmployeeDao employeeDao;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping("/check")
    public String processRequest() throws Exception{
       return objectMapper.writeValueAsString(employeeDao.findOne(1l));
    }
}
