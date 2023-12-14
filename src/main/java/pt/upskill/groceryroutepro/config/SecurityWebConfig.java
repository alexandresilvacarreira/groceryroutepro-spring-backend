package pt.upskill.groceryroutepro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityWebConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserAuthenticationProvider userAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.
            formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
            .and()
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/tarefas").hasAnyRole("USER", "ADMIN")
                    .antMatchers("/countries", "/admin").hasRole("ADMIN")
                    .antMatchers("/login", "/signup", "/assets/**", "/styles/**").permitAll()
                    .antMatchers("/").authenticated()
                    .antMatchers("**").denyAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder managerBuilder) {
        managerBuilder.authenticationProvider(userAuthenticationProvider);
    }
}

