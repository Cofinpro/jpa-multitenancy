package de.example.multitenant;

import javax.persistence.*;

/**
 * @author Gregor Tudan, Cofinpro AG
 */
@NamedQueries({
        @NamedQuery(name = "User.findAll", query = "select u from User u")
})
@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Basic(optional = false)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
