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
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.repositories.UserRepository;

import javax.servlet.http.HttpServletResponse;
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
        httpSecurity.
            formLogin()
//                .loginPage("/users/login") //TODO provavelmente isto não será preciso
                .loginProcessingUrl("/users/process-login")
                .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                    // TODO o authentication devolve logo o user no login
                    // Podemos devolver uma mensagem de erro, etc aqui
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                    httpServletResponse.setContentType("application/json");
                    Map<String,Object> userDetails = new HashMap<>();
                    User user = this.userRepository.getByEmail((String) authentication.getPrincipal());
                    userDetails.put("user_email",user.getEmail());
                    userDetails.put("user_role",user.getRole());
                    try {
                        // Create ObjectMapper
                        ObjectMapper objectMapper = new ObjectMapper();
                        // Convert Map to JSON string
                        String json = objectMapper.writeValueAsString(userDetails);
                        // Print the JSON string
                        System.out.println(json);
                        httpServletResponse.getWriter().write(json);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                })
//                .failureHandler((httpServletRequest, httpServletResponse, authentication)-> {
//                    httpServletResponse.sendRedirect("http://localhost:4200/error");
//                })
            .and()
                .csrf().disable()
                .authorizeRequests()
                    // TODO proteger endpoints shopping-lists, routes
                    .antMatchers("/users/login", "/users/signup").anonymous()
                    .antMatchers("/").authenticated()
                    .antMatchers("/shopping-list/**").hasAnyRole("USER_FREE", "USER_PREMIUM")
                    .antMatchers("/user-management/**").hasRole("ADMIN")
                    .antMatchers("/login", "/users/signup").permitAll()
                    .antMatchers("**").denyAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) {
        managerBuilder.authenticationProvider(userAuthenticationProvider);
    }
}

