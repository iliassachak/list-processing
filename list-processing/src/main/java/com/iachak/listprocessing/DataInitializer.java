package com.iachak.listprocessing;

import com.iachak.listprocessing.entity.Role;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        create("admin","admin@lp.com","admin123", true, Set.of(Role.ADMIN,Role.USER));
        create("user1","user1@lp.com","user123", true, Set.of(Role.USER));
        create("user2","user2@lp.com","user123", true, Set.of(Role.USER));
        create("user3","user3@lp.com","user123", true, Set.of(Role.USER));
    }

    private void create(String u, String e, String pwd, boolean enable, Set<Role> roles){
        if(!userRepo.existsByUsername(u)){
            User user=new User();
            user.setUsername(u); user.setEmail(e);
            user.setPassword(passwordEncoder.encode(pwd));
            user.setEnabled(enable);
            user.setRoles(roles);
            userRepo.save(user);
            System.out.println("Created: "+u);
        }
    }
}
