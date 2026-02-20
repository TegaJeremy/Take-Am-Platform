////package com.takeam.gateway.config;
////
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.web.cors.CorsConfiguration;
////import org.springframework.web.cors.reactive.CorsWebFilter;
////import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
////
////import java.util.Arrays;
////
////@Configuration
////public class CorsConfig {
////
////    @Bean
////    public CorsWebFilter corsWebFilter() {
////        CorsConfiguration corsConfig = new CorsConfiguration();
////        corsConfig.setAllowedOrigins(Arrays.asList(
////                "http://localhost:3000",
////                "http://localhost:5173",
////                "https://takeam.com",
////                "https://tegajeremy.github.io"
////        ));
////        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
////        corsConfig.setAllowedHeaders(Arrays.asList("*"));
////        corsConfig.setAllowCredentials(true);
////        corsConfig.setMaxAge(3600L);
////
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        source.registerCorsConfiguration("/**", corsConfig);
////
////        return new CorsWebFilter(source);
////    }
////}
//
//
//package com.takeam.gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//
//        // IMPORTANT: Use setAllowedOriginPatterns instead of setAllowedOrigins
//        // This prevents duplicate header issues
//        corsConfig.setAllowedOriginPatterns(Arrays.asList(
//                "http://localhost:3000",
//                "http://localhost:5173",
//                "https://takeam.com",
//                "https://www.takeam.com",
//                "https://tegajeremy.github.io",
//                "https://take-am.netlify.app"
//        ));
//
//        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        corsConfig.setAllowedHeaders(Arrays.asList("*"));
//        corsConfig.setAllowCredentials(true);
//        corsConfig.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//
//        return new CorsWebFilter(source);
//    }
//}