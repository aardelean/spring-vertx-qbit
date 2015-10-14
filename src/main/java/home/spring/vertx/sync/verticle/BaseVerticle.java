package home.spring.vertx.sync.verticle;

import io.vertx.core.AbstractVerticle;

/**
 * Created by alex on 10/10/2015.
 */
public class BaseVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        super.start();
        System.out.println("NORMAL VERTICLE DEPLOYED");
    }
}
