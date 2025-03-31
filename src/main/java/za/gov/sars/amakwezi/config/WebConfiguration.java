package za.gov.sars.amakhwezi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author S2024726
 */
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home.xhtml");
        registry.addViewController("/home").setViewName("home.xhtml");
        registry.addViewController("/users").setViewName("users.xhtml");
        registry.addViewController("/userroles").setViewName("userroles.xhtml");
    }
}
