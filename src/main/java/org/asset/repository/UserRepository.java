package org.asset.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.asset.model.Users;

@ApplicationScoped
public class UserRepository implements PanacheRepository<Users> {

    public Users getUserByEmail(String email) {
        return find("email", email).firstResult();
    }

    public boolean isUserExists(String email) {
        Long count = getEntityManager().createQuery("SELECT COUNT(u) FROM Users u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

}
