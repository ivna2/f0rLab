package fitnessclub.security;

import fitnessclub.service.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .httpBasic(basic -> {})
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/post-login", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2/**"))
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth", "/register", "/login", "/api/auth/register", "/api/auth/csrf").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/h2/**").permitAll()
                        .requestMatchers("/post-login", "/member-photos/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/trainers/**", "/lessons/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.POST, "/api/operations/book-lesson").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.POST, "/api/operations/renew-subscription").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.POST, "/api/operations/bookings/*/reschedule").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers(HttpMethod.GET, "/api/operations/members/*/schedule").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers("/admin/**", "/members-page", "/trainers-page", "/lessons-page", "/subscriptions-page", "/bookings-page").hasRole("ADMIN")
                        .requestMatchers("/members/**", "/trainers/add", "/trainers/delete/**", "/lessons/add", "/lessons/delete/**",
                                "/subscriptions/add", "/subscriptions/delete/**", "/bookings/add", "/bookings/delete/**").hasRole("ADMIN")
                        .requestMatchers("/api/members/**", "/subscriptions/**", "/bookings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/trainers/**", "/lessons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/trainers/**", "/lessons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/trainers/**", "/lessons/**").hasRole("ADMIN")
                        .requestMatchers("/api/operations/enroll-member").hasRole("ADMIN")
                        .requestMatchers("/api/operations/trainers/*/workload").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers("/member/**").hasAnyRole("ADMIN", "MEMBER")
                        .requestMatchers("/api/auth/me").authenticated()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
