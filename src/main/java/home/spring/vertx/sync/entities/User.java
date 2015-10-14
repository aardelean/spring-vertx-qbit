package home.spring.vertx.sync.entities;

import lombok.Data;

/**
 * Created by alex on 9/30/2015.
 */
@Data
public class User {
    private String name;
    private String lastname;
    private String occupation;
    public User(String name, String lastName, String occupation){
        this.name = name;
        this.lastname= lastName;
        this.occupation = occupation;
    }

    public User() {}
}
