package lab.booking.config;

import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Booking System API")
                        .version("1.0.0"))
                .tags(List.of(
                        new Tag().name("Users").description("Operations with users"),
                        new Tag().name("Rooms").description("Operations with rooms"),
                        new Tag().name("Reservations").description("Operations with reservations")
                ));
    }
}
