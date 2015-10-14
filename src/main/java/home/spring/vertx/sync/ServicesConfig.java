package home.spring.vertx.sync;

import home.spring.vertx.sync.endpoint.qbit.RestComplexEndpoint;
import home.spring.vertx.sync.endpoint.qbit.RestMysqlEndpoint;
import home.spring.vertx.sync.endpoint.qbit.RestJsonEndpoint;
import home.spring.vertx.sync.endpoint.qbit.RestMongoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by alex on 10/2/2015.
 */
@Configuration
public class ServicesConfig {
    @Bean
    public RestJsonEndpoint simpleService(){
        return new RestJsonEndpoint();
    }
    @Bean
    public RestMongoEndpoint personService(){
        return new RestMongoEndpoint();
    }
    @Bean
    public RestMysqlEndpoint employeeService(){
        return new RestMysqlEndpoint();
    }

    @Bean
    public RestComplexEndpoint complexService(){
        return new RestComplexEndpoint();
    }

}
