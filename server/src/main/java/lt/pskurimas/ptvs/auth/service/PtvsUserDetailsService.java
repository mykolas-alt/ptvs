package lt.pskurimas.ptvs.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lt.pskurimas.ptvs.auth.repository.AppUserRepository;
import lt.pskurimas.ptvs.auth.security.PtvsUserDetails;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PtvsUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByUsername(username)
                .map(PtvsUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
