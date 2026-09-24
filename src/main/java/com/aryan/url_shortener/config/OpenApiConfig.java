package com.aryan.url_shortener.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "URL Shortener",
        version = "1.0.0",
        description = "A simple and scalable RESTful API to shorten long URLs and redirect users."
    ),
    servers = {
        @Server (url = "/")
    }
)
public class OpenApiConfig {

}
