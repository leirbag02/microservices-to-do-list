import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*") // Permite requisições de qualquer origem
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Limita os métodos permitidos
                        .allowedHeaders("*") // Permite todos os cabeçalhos
                        .allowCredentials(false) // Define se cookies ou credenciais devem ser enviados
                        .maxAge(3600); // Define o tempo máximo em segundos que o navegador deve armazenar as permissões (cache)
            }
        };
    }
}
