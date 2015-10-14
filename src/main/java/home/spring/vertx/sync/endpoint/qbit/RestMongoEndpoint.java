package home.spring.vertx.sync.endpoint.qbit;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.advantageous.qbit.annotation.RequestMapping;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * Created by alex on 9/27/2015.
 */
@RequestMapping("/mongo")
public class RestMongoEndpoint {

    private final static String id="eefa89c4-ec21-11e4-b08b-b75697636679-8e488775";

    @Autowired
    private MongoDatabase mongoDatabase;

    @RequestMapping("/check")
    public String processRequest() throws Exception {
        CompletableFuture<String> mongoResult = new CompletableFuture();
        MongoCollection<Document> collection = mongoDatabase.getCollection("Person");
        collection.find().filter(Filters.eq("_id", id)).first((p, throwable) ->mongoResult.complete(((Document) p).toJson()));
        return mongoResult.get();
    }
}
