package home.spring.vertx.sync.verticle;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.netty.util.internal.ThreadLocalRandom;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by alex on 10/5/2015.
 */
public class WebServer extends AbstractVerticle implements Handler<HttpServerRequest> {

    static Logger logger = LoggerFactory.getLogger(WebServer.class.getName());

    private final Random random = ThreadLocalRandom.current();

    private static final String PATH_PLAINTEXT = "/plaintext";
    private static final String PATH_JSON = "/json";
    private static final String PATH_DB = "/db";
    private static final String PATH_QUERIES = "/queries";
    private static final String PATH_UPDATES = "/updates";
    private static final String PATH_FORTUNES = "/fortunes";

    private static final String RESPONSE_TYPE_PLAIN = "text/plain";
    private static final String RESPONSE_TYPE_HTML = "text/html";
    private static final String RESPONSE_TYPE_JSON = "application/json";

    private static final String UNDERSCORE_ID = "_id";
    private static final String TEXT_ID = "id";
    private static final String RANDOM_NUMBER = "randomNumber";
    private static final String TEXT_QUERIES = "queries";
    private static final String TEXT_MESSAGE = "message";
    private static final String TEXT_MESSAGES = "messages";
    private static final String ADD_FORTUNE_MESSAGE = "Additional fortune added at request time.";
    private static final String HELLO_WORLD = "Hello, world!";
    private static final Buffer HELLO_WORLD_BUFFER = Buffer.buffer(HELLO_WORLD);

    private static final String HEADER_SERVER = "SERVER";
    private static final String HEADER_DATE = "DATE";
    private static final String HEADER_CONTENT = "content-type";

    private static final String SERVER = "vertx3";

    private static final String DB_WORLD = "World";
    private static final String DB_FORTUNE = "Fortune";
    private static final String DB_NAME = "hello_world";
    private static final int DB_PORT = 27017;

    private static final String TEMPLATE_FORTUNE = "<!DOCTYPE html><html><head><title>Fortunes</title></head><body><table><tr><th>id</th><th>message</th></tr><#list messages as message><tr><td>${message.id?html}</td><td>${message.message?html}</td></tr></#list></table></body></html>";

    private Template ftlTemplate;

    private MongoClient mongoClient;

    private String dateString;

    private HttpServer server;

    @Override
    public void start() {

        int port = 8080;

        server = vertx.createHttpServer();

        JsonObject config = new JsonObject();
        String host = System.getenv("DBHOST");
        if ( host != null && host.length() > 0 ) {
            config.put("host", host);
        } else {
            config.put("host", "localhost");
        }

        config.put("port", DB_PORT).put("db_name", DB_NAME);
        //config.put("maxPoolSize", 250).put("minPoolSize", 50);

        mongoClient = MongoClient.createShared(vertx, config);

        try {
            ftlTemplate = new Template("Fortune", new StringReader(TEMPLATE_FORTUNE),
                    new Configuration());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        server.requestHandler(WebServer.this).listen(port);

        dateString = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now());

        vertx.setPeriodic(1000, handler -> {
            dateString = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now());
        });
    }

    @Override
    public void handle(HttpServerRequest request) {
        switch (request.path()) {
            case PATH_PLAINTEXT:
                handlePlainText(request);
                break;
            case PATH_JSON:
                handleJson(request);
                break;
            case PATH_DB:
                handleDbMongo(request);
                break;
            case PATH_QUERIES:
                handleDBMongoQueries(request);
                break;
            case PATH_UPDATES:
                hanldeDBMongoUpdates(request);
                break;
            case PATH_FORTUNES:
                handleFortunes(request);
                break;
            default:
                request.response().setStatusCode(404);
                request.response().end();
        }
    }

    @Override
    public void stop(){
        if ( mongoClient != null ) mongoClient.close();
        if ( server != null ) server.close();
    }

    private void handleFortunes(HttpServerRequest request) {
        mongoClient.find(DB_FORTUNE, new JsonObject(), handler -> {

            List<JsonObject> fortunes = handler.result();
            fortunes.add(new JsonObject().put(TEXT_ID,0).put(TEXT_MESSAGE, ADD_FORTUNE_MESSAGE));

            //Convert JsonObjects to Map for template to work correctly
            //TODO: why is id coming back as a double
            List<Map<String, Object>> fortunes2 = fortunes.stream()
                    .sorted((f1, f2) -> f1.getString(TEXT_MESSAGE).compareTo(f2.getString(TEXT_MESSAGE)))
                    .map( m -> {  m.remove(UNDERSCORE_ID); m.put(TEXT_ID, m.getInteger(TEXT_ID)); return m.getMap(); })
                    .collect(Collectors.toList());

            Map model = new HashMap();
            model.put(TEXT_MESSAGES, fortunes2);
            Writer writer = new StringWriter();
            try { ftlTemplate.process(model, writer); } catch (Exception ex) { ex.printStackTrace(); }

            Buffer buffer = Buffer.buffer(writer.toString());
            request.response().putHeader(HEADER_CONTENT, RESPONSE_TYPE_HTML)
                    .putHeader(HEADER_SERVER,  SERVER)
                    .putHeader(HEADER_DATE, dateString)
                    .end(buffer);
        });
    }

    private void handlePlainText(HttpServerRequest request) {
        request.response()
                .putHeader(HEADER_CONTENT, RESPONSE_TYPE_PLAIN).putHeader(HEADER_SERVER,  SERVER)
                .putHeader(HEADER_DATE, dateString).end(HELLO_WORLD_BUFFER);
    }

    private void handleJson(HttpServerRequest request) {
        request.response().putHeader(HEADER_CONTENT, RESPONSE_TYPE_JSON).putHeader(HEADER_SERVER,  SERVER)
                .putHeader(HEADER_DATE, dateString).end(new JsonObject().put(TEXT_MESSAGE, HELLO_WORLD).encode());
    }

    private void handleDbMongo(HttpServerRequest request) {
        mongoClient.findOne(DB_WORLD, new JsonObject().put(UNDERSCORE_ID, (random.nextInt(10000) + 1)), new JsonObject(), handler -> {
            if ( handler.succeeded() ) {
                JsonObject world = getResultFromReply(handler);
                String result = world.encode();
                sendResponse(request, result);
            } else {
                handler.cause().printStackTrace();
                sendResponse(request, new JsonObject().put("error", "error").encode());
            }
        });
    }

    private JsonObject getResultFromReply(AsyncResult<JsonObject> reply) {
        JsonObject body = reply.result();
        //Move _id to id
        if ( body.containsKey(UNDERSCORE_ID)) {
            String id = body.remove(UNDERSCORE_ID).toString();
            body.put(TEXT_ID, Double.valueOf(id).intValue());
        }
        return body;
    }

    private void hanldeDBMongoUpdates(HttpServerRequest request) {
        int queriesParam = 1;

        try {
            queriesParam = Integer.parseInt(request.params().get(TEXT_QUERIES));
        } catch (NumberFormatException e) {
            queriesParam = 1;
        }

        //Queries must be between 1 and 500
        queriesParam = Math.max(1, Math.min(queriesParam, 500));

        final MongoHandler dbh = new MongoHandler(request, queriesParam, true);

        IntStream.range(0, queriesParam).parallel().forEach(nbr -> {
            findRandom(dbh);
        });
    }

    private void handleDBMongoQueries(HttpServerRequest request) {
        int queriesParam = 1;

        try {
            queriesParam = Integer.parseInt(request.params().get(TEXT_QUERIES));
        } catch (NumberFormatException e) {
            queriesParam = 1;
        }

        //Queries must be between 1 and 500
        queriesParam = Math.max(1, Math.min(queriesParam, 500));
        final Handler<AsyncResult<JsonObject>> dbh = new MongoHandler(request, queriesParam, false);
        IntStream.range(0, queriesParam).parallel().forEach(
                nbr -> { findRandom(dbh); }
        );
    }

    private void findRandom(Handler<AsyncResult<JsonObject>> handler) {
        mongoClient.findOne(DB_WORLD, new JsonObject().put(UNDERSCORE_ID, (random.nextInt(10000) + 1)), new JsonObject(), handler);
    }

    private void updateRandom(JsonObject json) {
        JsonObject update = new JsonObject().put("$set", json);
        mongoClient.update(DB_WORLD, new JsonObject().put(UNDERSCORE_ID, json.getInteger(TEXT_ID)), update, handler-> {
            if (  handler.succeeded()) {

            } else if ( handler.failed() ) {
                handler.cause().printStackTrace();
            }
        });
    }

    private void sendResponse(HttpServerRequest request, String result) {
        request.response().putHeader(HEADER_CONTENT, RESPONSE_TYPE_JSON)
                .putHeader(HEADER_SERVER,  SERVER)
                .putHeader(HEADER_DATE, dateString)
                .end(result);
    }

    private final class MongoHandler implements Handler<AsyncResult<JsonObject>> {
        private final HttpServerRequest request;
        private final int queries;
        private final JsonArray worlds;
        private final Random random;
        private final boolean randomUpdates;

        public MongoHandler(HttpServerRequest rq, int queriesParam, boolean performRandomUpdates) {
            request = rq;
            queries = queriesParam;
            randomUpdates = performRandomUpdates;
            random = ThreadLocalRandom.current();
            worlds = new JsonArray();
        }
        @Override
        public void handle(AsyncResult<JsonObject> reply) {
            JsonObject world = getResultFromReply(reply);
            if (randomUpdates) {
                world.put(RANDOM_NUMBER, (random.nextInt(10000) + 1));
                updateRandom(world);
            }
            worlds.add(world);
            if (worlds.size() == this.queries) {
                // All queries have completed; send the response.
                String result = worlds.encode();
                sendResponse(request, result);
            }
        }
    }

    public static void main(String[] args) {

        int procs = Runtime.getRuntime().availableProcessors();
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(WebServer.class.getName(),
                new DeploymentOptions().setInstances(procs*2), event -> {
                    if (event.succeeded()) {
                        logger.debug("Your Vert.x application is started!");
                    } else {
                        logger.error("Unable to start your application", event.cause());
                    }
                });
    }
}