package hu.lacztam.token;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{

	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER = "Bearer ";
	@Autowired private JwtService jwtService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain) throws ServletException, IOException {
		
		String authHeader = request.getHeader(AUTHORIZATION);
		
		UsernamePasswordAuthenticationToken authentication = createUserDetailsFromAuthHeader(authHeader, jwtService);
		if(authentication != null) {
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	public static UsernamePasswordAuthenticationToken createUserDetailsFromAuthHeader(String authHeader, JwtService jwtService) {
		if(authHeader != null && authHeader.startsWith(BEARER)) {
			String jwtToken = authHeader.substring(BEARER.length());
			UserDetails principal = jwtService.parseJwt(jwtToken);
			
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
			return authentication;
		}
		return null;
	}
}
