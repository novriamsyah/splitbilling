
package com.novri.splitbill.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI splitBillOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Split Bill API")
                        .description("""
                                REST API 
                                - Manage bill groups
                                - Manage group participants
                                - Manage expenses
                                - Calculate expense shares
                                - View settlement summary
                                """)
                        .version("1.0.0"));
    }
}
