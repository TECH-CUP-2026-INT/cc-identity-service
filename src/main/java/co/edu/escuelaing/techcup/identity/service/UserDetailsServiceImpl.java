package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security implementation for loading user data during authentication.
 * Retrieves the user from the database by email and maps it to a UserDetails object.
 * Required by Spring Security to perform login validation (SCRUM-14).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their email address.
     * Called automatically by Spring Security during authentication.
     * @param email the email used as username
     * @return UserDetails object with credentials and roles
     * @throws UsernameNotFoundException if no user exists with that email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
/**
 * Maps the UserDocument to a Spring Security UserDetails object.
 * trues for: accountNonExpired, credentialsNonExpired, accountNonLocked
 */
        return new User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true, 
                true, 
                true, 
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}