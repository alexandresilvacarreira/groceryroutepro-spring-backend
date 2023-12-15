package pt.upskill.groceryroutepro.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.repositories.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityWebConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserAuthenticationProvider userAuthenticationProvider;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        //TODO confirmar email, alterar antMatchers,
        httpSecurity
                .cors()
                .and()
                .logout().permitAll()
                .and()
                .formLogin()
//                .loginPage("/login") // Por defeito, se um utilizador pedir um recurso ao qual nao tem acesso, o Spring Security manda para uma página de login deles.
                .loginProcessingUrl("/process-login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    handleSuccess(request, response, authentication);
                })
                .failureHandler((request, response, exception) -> {
                    handleFailure(request, response, exception);
                })
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/signup","/login").permitAll()
                .antMatchers("/").authenticated()
                .antMatchers("/shopping-list/**").hasAnyRole("USER_FREE", "USER_PREMIUM")
                .antMatchers("/user-management/**").hasRole("ADMIN")
                .antMatchers("**").denyAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) {
        managerBuilder.authenticationProvider(userAuthenticationProvider);
    }


    private void handleSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // Podemos personalizar esta lógica
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        User user = userRepository.getByEmail(authentication.getName());

        Map<String, Object> serverMessage = new HashMap<>();
        serverMessage.put("success",true);
        serverMessage.put("message","Autenticado com sucesso");
        serverMessage.put("userRole",user.getRole().getName());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(serverMessage);
        response.getWriter().write(json);
    }

    private void handleFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // Podemos personalizar esta lógica
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> serverMessage = new HashMap<>();
        serverMessage.put("success",false);
        serverMessage.put("message","Erro ao autenticar");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(serverMessage);
        response.getWriter().write(json);
    }

}

