package com.example.backend.security;

import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Recherche utilisateur avec email: '" + email + "'");
        com.example.backend.entity.User utilisateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found avec email: " + email));
        System.out.println("Utilisateur trouvé: " + utilisateur.getEmail());
        return new User(utilisateur.getEmail(), utilisateur.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(utilisateur.getRole().name())));
    }
}
