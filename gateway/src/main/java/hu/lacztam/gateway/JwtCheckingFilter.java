package hu.lacztam.gateway;

import hu.lacztam.token.JwtAuthFilter;
import hu.lacztam.token.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class JwtCheckingFilter implements GlobalFilter {

	@Autowired
	private JwtService jwtService;

	private PathPattern userLoginPathPattern = PathPatternParser.defaultInstance.parse("/user/jwt-login");
	private PathPattern userRegisterPathPattern = PathPatternParser.defaultInstance.parse("/user/account");
	private PathPattern userServicePathPattern = PathPatternParser.defaultInstance.parse("/user/**");
	private PathPattern keepassServicePathPattern = PathPatternParser.defaultInstance.parse("/keepass/**");

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		Set<URI> origUrls = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
		URI originalUri = origUrls.iterator().next();

		if (userLoginPathPattern.matches(PathContainer.parsePath(originalUri.toString()).subPath(4)))
			return chain.filter(exchange);
		if (userRegisterPathPattern.matches(PathContainer.parsePath(originalUri.toString()).subPath(4)))
			return chain.filter(exchange);

		if (userServicePathPattern.matches(PathContainer.parsePath(originalUri.toString()).subPath(4)))
			return isJwtValid(exchange, chain) ? chain.filter(exchange) : send401Response(exchange);

		if (keepassServicePathPattern.matches(PathContainer.parsePath(originalUri.toString()).subPath(4)))
			return isJwtValid(exchange, chain) ? chain.filter(exchange) : send401Response(exchange);

		return send401Response(exchange);
	}

	private boolean isJwtValid(ServerWebExchange exchange, GatewayFilterChain chain) {
		List<String> authHeaders = exchange.getRequest().getHeaders().get("Authorization");

		if (ObjectUtils.isEmpty(authHeaders)) {
			return false;
		} else {
			String authHeader = authHeaders.get(0);
			UsernamePasswordAuthenticationToken userDetails = null;

			try {
				userDetails = JwtAuthFilter.createUserDetailsFromAuthHeader(authHeader, jwtService);
				if(userDetails.getPrincipal() != null)
					return true;
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return false;
			}
		}

		return false;
	}

	private boolean isCatalogRequestFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
		List<String> authHeaders = exchange.getRequest().getHeaders().get("Authorization");
		String methodValue = exchange.getRequest().getMethodValue();
		if (methodValue.equals("GET")) {
			return true;
		}
		
		if (ObjectUtils.isEmpty(authHeaders)) {
			return false;
		} else {
			String authHeader = authHeaders.get(0);
			UsernamePasswordAuthenticationToken userDetails = null;
			try {
				userDetails = JwtAuthFilter.createUserDetailsFromAuthHeader(authHeader, jwtService);
				if(userDetails != null) {
					Optional<Boolean> isAdmin = userDetails.getAuthorities().stream()
							.map(r -> r.getAuthority().equals("ADMIN")).findFirst();
					if (isAdmin.isPresent() && isAdmin.get()) {
						return true;
					} else {
						if (methodValue.equals("POST") || methodValue.equals("PUT") || methodValue.equals("DELETE"))
							return false;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	private Mono<Void> send401Response(ServerWebExchange exchange) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		return response.setComplete();
	}

}
