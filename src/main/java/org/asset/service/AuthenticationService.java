package org.asset.service;


import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.asset.model.Users;
import org.asset.repository.UserRepository;
import org.eclipse.microprofile.jwt.Claims;

import java.util.Arrays;
import java.util.HashSet;


@ApplicationScoped
public class AuthenticationService {

    @Inject
    UserRepository userRepository;

    public String login(String email, String password) {

        Users user = userRepository.getUserByEmail(email);

        if (user != null && user.getPassword().equals(password)) {

            String token =
                    Jwt.issuer("https://asset-service.com/issuer")
                            .upn(user.getEmail())
                            .groups(new HashSet<>(Arrays.asList((user.isAdmin() ? "Admin" : "User"))))
                            .claim(Claims.birthdate.name(), "2023-07-15")
                            .sign();

            return token;
        } else {
            throw new SecurityException("Nepravilno uporabniško ime ali geslo");
        }
    }


    public void registerUser(String email, String password) throws Exception {
        if (userRepository.isUserExists(email)) {
            throw new Exception("Uporabnik s tem e-poštnim naslovom že obstaja.");
        }

        Users newUser = new Users();
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setAdmin(false);
        userRepository.persist(newUser);
    }
}
