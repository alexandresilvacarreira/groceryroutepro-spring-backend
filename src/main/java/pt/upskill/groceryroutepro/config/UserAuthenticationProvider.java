package pt.upskill.groceryroutepro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.Login;
import pt.upskill.groceryroutepro.services.AuthService;
import pt.upskill.groceryroutepro.services.UserService;


import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        Login login = new Login(email, password);
        User user = authService.validateLogin(login);
        if(user != null) {
            /// logica verificar se já tem email registado???
            if (user.isVerifiedEmail()){
                List<GrantedAuthority> roleList = new ArrayList<>();
                roleList.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
                return new UsernamePasswordAuthenticationToken(email, password, roleList);
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
