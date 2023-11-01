package yonseigolf.server.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class CorsConfigTest {

    private CorsConfig corsConfig;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    public void setUp() {
        corsConfig = new CorsConfig();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @Test
    public void whenOriginIsYonseiGolf_thenSetAllowOriginToYonseiGolfSite() throws IOException, ServletException {
        request.addHeader("Origin", "https://yonseigolf.site");

        corsConfig.doFilter(request, response, chain);

        assertEquals("https://yonseigolf.site", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void whenOriginIsWWWYonseiGolf_thenSetAllowOriginToYonseiGolfSite() throws IOException, ServletException {
        request.addHeader("Origin", "https://www.yonseigolf.site");

        corsConfig.doFilter(request, response, chain);

        assertEquals("https://www.yonseigolf.site", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void whenOriginIsNotYonseiGolf_thenSetAllowOriginToLocalhost() throws IOException, ServletException {
        request.addHeader("Origin", "https://other.site");

        corsConfig.doFilter(request, response, chain);

        assertEquals("http://localhost:3000", response.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void whenNoOriginIsSet_thenDefaultToAllowOriginLocalhost() throws IOException, ServletException {
        corsConfig.doFilter(request, response, chain);

        assertEquals("http://localhost:3000", response.getHeader("Access-Control-Allow-Origin"));
    }
}
