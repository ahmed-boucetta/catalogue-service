package com.adeo.sec;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JWTAuthorisationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LogManager.getLogger(JWTAuthorisationFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        httpServletResponse.addHeader("Access-Control-Allow-Origin","*");
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept,Authorization");
        httpServletResponse.addHeader("Access-Control-Expose-Headers","Allow-Control-Allow-Origin,Allow-Control-Allow-Credentials,Authorization");
        httpServletResponse.addHeader("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,Patch");
        if(httpServletRequest.getRequestURI().equals("/login")){
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }
        String jwt = httpServletRequest.getHeader(SecurityParams.JWT_HEADER_NAME);
        if (jwt == null || !jwt.startsWith(SecurityParams.HEADER_PREFIX)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            if(LOGGER.isDebugEnabled()) LOGGER.debug("Token is Null or header not start with "+SecurityParams.HEADER_PREFIX);
            return;
        }
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(SecurityParams.SECRET)).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(jwt.substring(SecurityParams.HEADER_PREFIX.length()));
        String userName = decodedJWT.getSubject();
        List<String> roles = decodedJWT.getClaims().get("roles").asList(String.class);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        roles.forEach(s -> grantedAuthorities.add(new SimpleGrantedAuthority(s)));
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userName, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
