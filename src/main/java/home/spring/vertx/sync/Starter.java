package home.spring.vertx.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by alex on 10/1/2015.
 */
@SpringBootApplication
@Import({VertxConfig.class, ServicesConfig.class})
public class Starter {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Starter.class, args);
    }
}
