package de.cofinpro.multitenant;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Gregor Tudan, Cofinpro AG
 */
@ApplicationScoped
public class UserService {

    @PersistenceContext
    private EntityManager em;

    public List<User> getAllUsers() {
        return em.createNamedQuery("User.findAll", User.class).getResultList();
    }
}
