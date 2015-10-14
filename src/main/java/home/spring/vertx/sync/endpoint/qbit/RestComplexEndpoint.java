package home.spring.vertx.sync.endpoint.qbit;

import co.paralleluniverse.fibers.Suspendable;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import home.spring.vertx.sync.dao.EmployeeDao;
import home.spring.vertx.sync.entities.Employee;
import home.spring.vertx.sync.entities.Person;
import home.spring.vertx.sync.rest.RestClient;
import io.advantageous.boon.json.ObjectMapper;
import io.advantageous.boon.json.implementation.ObjectMapperImpl;
import io.advantageous.qbit.annotation.RequestMapping;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by alex on 10/3/2015.
 */
@RequestMapping("/complex")
public class RestComplexEndpoint {

    private final static String id="eefa89c4-ec21-11e4-b08b-b75697636679-8e488775";

    @Autowired
    private EmployeeDao employeeDao;

    @Autowired
    private MongoDatabase mongoDatabase;

    @Autowired
    private RestClient restClient;

    @Value("${externalUrl}")
    private String externalUrl;

    private ObjectMapper objectMapper = new ObjectMapperImpl();

    @RequestMapping("/check")
    public String complexShit() throws Exception {
        CompletableFuture<Person> futurePerson = person();
        Employee employee = employee();
        String response = response();
        double calculated = calculate();
        return objectMapper.toJson(employee)
                +response
                +objectMapper.toJson(futurePerson.get())
                +calculated;
    }

    @Suspendable
    public Employee employee(){
        return employeeDao.findOne(1l);
    }

    public CompletableFuture<Person> person() throws Exception {
        CompletableFuture<Person> mongoResult = new CompletableFuture();
        MongoCollection<Document> collection = mongoDatabase.getCollection("Person");
        collection.find().filter(Filters.eq("_id", id)).first((p, throwable) ->mongoResult.complete(objectMapper.readValue(((Document) p).toJson(), Person.class)));
        return mongoResult;
    }

    @Suspendable
    public String response() throws UnsupportedEncodingException {
        return restClient.get(externalUrl, String.class);
    }

    @Suspendable
    public double calculate(){
        double[] resultVal = new double[100_000];
        for(int i=0; i<100_000;i++){
            resultVal[i] = Math.sqrt(i*i+123.4);
        }
        return resultVal[75_000];
    }

}
