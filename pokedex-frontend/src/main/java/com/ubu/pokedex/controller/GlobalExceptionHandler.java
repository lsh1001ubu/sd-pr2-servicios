package com.ubu.pokedex.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

// Controlador global de excepciones para interceptar y gestionar errores en todo el frontal
@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura excepciones HTTP (ej. errores 4xx o 5xx devueltos por la API de Python)
    @ExceptionHandler(HttpStatusCodeException.class)
    public String handleHttpClientErrorException(HttpStatusCodeException ex, Model model) {
        String responseBody = ex.getResponseBodyAsString();
        String errorMessage = "Ha ocurrido un error inesperado.";

        // Intenta analizar el JSON de error personalizado de la API de Flask si es posible
        if (responseBody.contains("FILE_NOT_FOUND")) {
            errorMessage = "Error: El archivo solicitado no existe en el servidor.";
        } else if (responseBody.contains("DB_SYNTAX_ERROR") || responseBody.contains("DB_ERROR")) {
            errorMessage = "Error de Base de Datos: Hubo un problema al acceder a la información de los Pokémon. Por favor, contacte a soporte.";
        } else if (responseBody.contains("THIRD_PARTY_API_ERROR") || responseBody.contains("THIRD_PARTY_NETWORK_ERROR") || responseBody.contains("API_ERROR")) {
            errorMessage = "Error de API Externa: No se pudo conectar con el servicio de PokeAPI. Puede que esté caído temporalmente.";
        } else {
            errorMessage = "Error del Servidor Externo: " + ex.getStatusCode() + " - " + responseBody;
        }

        model.addAttribute("translatedError", errorMessage);
        model.addAttribute("rawError", responseBody);
        return "simulate";
    }

    // Captura errores graves de red cuando el servidor backend ni siquiera es accesible
    @ExceptionHandler(RestClientException.class)
    public String handleRestClientException(RestClientException ex, Model model) {
        model.addAttribute("translatedError", "Error Crítico: El servicio de Python API no está disponible o no se puede alcanzar.");
        model.addAttribute("rawError", ex.getMessage());
        return "simulate";
    }

    // Captura cualquier otra excepción genérica en tiempo de ejecución
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("translatedError", "Ha ocurrido un error inesperado en la aplicación.");
        model.addAttribute("rawError", ex.getMessage());
        return "simulate"; // Por simplicidad, se redirige a la página de simulación para mostrar la alerta de error
    }
}
