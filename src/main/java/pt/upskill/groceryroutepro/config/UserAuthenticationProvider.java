package pt.upskill.groceryroutepro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {


//    @Autowired
//    AuthService authService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        String username = authentication.getName();
//        String password = authentication.getCredentials().toString();
//        Login login = new Login(username, password);
//        User user = authService.validateLogin(login);
//        if(user != null) {
//            List<GrantedAuthority> roleList = new ArrayList<>();
//            roleList.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
//            return new UsernamePasswordAuthenticationToken(username, password, roleList);
//        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
