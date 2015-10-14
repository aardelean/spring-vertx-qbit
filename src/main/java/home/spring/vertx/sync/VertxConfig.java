package home.spring.vertx.sync;

import home.spring.vertx.sync.endpoint.qbit.RestComplexEndpoint;
import home.spring.vertx.sync.endpoint.qbit.RestJsonEndpoint;
import home.spring.vertx.sync.endpoint.qbit.RestMongoEndpoint;
import home.spring.vertx.sync.endpoint.qbit.RestMysqlEndpoint;
import home.spring.vertx.sync.verticle.BaseVerticle;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.vertx.http.VertxHttpServerBuilder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Created by alex on 9/27/2015.
 */
@Configuration
public class VertxConfig {

    @Autowired
    private ServiceEndpointServer serviceEndpointServer;

    @Autowired
    private HttpServer server;

    @Autowired
    private Vertx vertx;

    @Autowired
    private ApplicationContext ctx;

    @Bean
    public Vertx vertx(){
        VertxOptions options = new VertxOptions();
        Vertx vertx = Vertx.vertx(options);
        return vertx;
    }

    @Bean
    public HttpServer server(Vertx vertx){
        HttpServer server = vertx.createHttpServer();
        return server;
    }

    @Bean
    public io.advantageous.qbit.http.server.HttpServer qbitServer(HttpServer server, Vertx vertx){
        return VertxHttpServerBuilder.vertxHttpServerBuilder()
                .setHttpServer(server)
                .setVertx(vertx)
                .build();
    }

    @Bean
    public ServiceEndpointServer serviceEndpointServer(io.advantageous.qbit.http.server.HttpServer qbitServer,
                                                       RestJsonEndpoint jsonService,
                                                       RestMysqlEndpoint employeeService,
                                                       RestMongoEndpoint personService,
                                                       RestComplexEndpoint complexService){
        return EndpointServerBuilder.endpointServerBuilder()
                       .setHttpServer(qbitServer)
                       .setUri("/qbit/")
                       .addServices(jsonService, employeeService, personService, complexService)
                       .build();
    }

    @PostConstruct
    public void gameOn(){
        vertx.deployVerticle(BaseVerticle.class.getName() , new DeploymentOptions().setInstances(16));
        serviceEndpointServer.start();
        server.listen(8080);
    }
}
