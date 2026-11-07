package com.academy.presentation.config;

import com.academy.presentation.dto.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI academyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Academy API")
                        .version("1.0.0")
                        .description("CRUD de estudiantes con DTO, excepciones y seguridad JWT, documentado con OpenAPI. Para probar: **POST /api/v1/auth/login**, copiar el token y pulsar **Authorize**.")
                        .contact(new Contact().name("Equipo Academy").email("academy@mail.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(components());
    }

    private Components components() {
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addResponses("BadRequest", errorResponse("La petición no es válida",
                        example(400, "Bad Request", "Los datos enviados no son válidos", "/api/v1/students",
                                List.of(Map.of("field", "email", "message", "no tiene formato de correo electrónico")))))
                .addResponses("NotFound", errorResponse("El recurso no existe",
                        example(404, "Not Found", "No existe un estudiante con id 999", "/api/v1/students/999", null)))
                .addResponses("Conflict", errorResponse("El correo ya está registrado",
                        example(409, "Conflict", "El correo ana@mail.com ya está registrado", "/api/v1/students", null)))
                .addResponses("Unauthorized", errorResponse("No autenticado: falta el token, no es válido o expiró",
                        example(401, "Unauthorized", "Se requiere autenticación para acceder a este recurso", "/api/v1/students", null)))
                .addResponses("Forbidden", errorResponse("Autenticado, pero su rol no permite la operación",
                        example(403, "Forbidden", "No tiene permisos para realizar esta operación", "/api/v1/students/1", null)));
        // El esquema ErrorResponse se agrega a mano porque ninguna operación lo declara como tipo de retorno.
        ModelConverters.getInstance().readAll(ErrorResponse.class).forEach(components::addSchemas);
        return components;
    }

    private Map<String, Object> example(int status, String error, String message, String path, List<?> details) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("timestamp", "2026-11-01T10:15:30.123Z");
        example.put("status", status);
        example.put("error", error);
        example.put("message", message);
        example.put("path", path);
        if (details != null) {
            example.put("details", details);
        }
        return example;
    }

    private ApiResponse errorResponse(String description, Map<String, Object> example) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                        .example(example)));
    }

    /**
     * Agrega 401 a toda operación protegida y 403 a las que exigen el rol ADMIN (ver SecurityConfig).
     * Se agrega a cada grupo: con GroupedOpenApi, los personalizadores globales no se aplican.
     */
    private OpenApiCustomizer securityResponses() {
        return openApi -> openApi.getPaths().forEach((path, item) ->
                item.readOperationsMap().forEach((method, operation) -> {
                    boolean isPublic = operation.getSecurity() != null && operation.getSecurity().isEmpty();
                    if (isPublic) {
                        return;
                    }
                    operation.getResponses().addApiResponse("401",
                            new ApiResponse().$ref("#/components/responses/Unauthorized"));
                    if (method == PathItem.HttpMethod.DELETE || path.startsWith("/internal/")) {
                        operation.getResponses().addApiResponse("403",
                                new ApiResponse().$ref("#/components/responses/Forbidden"));
                    }
                }));
    }

    @Bean
    public GroupedOpenApi studentsGroup() {
        return GroupedOpenApi.builder()
                .group("students")
                .addOpenApiCustomizer(securityResponses())
                .displayName("Students")
                .pathsToMatch("/api/v1/students/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("auth")
                .addOpenApiCustomizer(securityResponses())
                .displayName("Auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalGroup() {
        return GroupedOpenApi.builder()
                .group("internal")
                .addOpenApiCustomizer(securityResponses())
                .displayName("Internal")
                .pathsToMatch("/internal/**")
                .build();
    }
}
