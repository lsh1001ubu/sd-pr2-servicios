package com.ubu.pokedex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import com.ubu.pokedex.repository.UserRepository;
import com.ubu.pokedex.model.User;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

// Controlador principal de Spring Boot para manejar las vistas y rutas de la Pokedex
@Controller
public class PokedexController {

    @Value("${pokedex.api.url}")
    private String apiUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    // Método auxiliar para comprobar si el usuario ha iniciado sesión
    private boolean checkSession(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }

    // Método auxiliar para inyectar el nombre de usuario en las vistas
    private void addUsername(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("loggedInUser"));
    }

    // Ruta principal (Home)
    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        if (checkSession(session)) {
            model.addAttribute("loggedIn", true);
        }
        return "index";
    }

    // Muestra la pantalla de inicio de sesión
    @GetMapping("/login")
    public String login(HttpSession session) {
        if (checkSession(session)) {
            return "redirect:/simulate";
        }
        return "login";
    }

    // Procesa el formulario de login validando contra la base de datos
    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("loggedInUser", username);
            return "redirect:/pokedex"; // Redirige a la Pokedex tras iniciar sesión
        }
        model.addAttribute("error", true);
        return "login";
    }

    // Cierra la sesión activa
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // Muestra el panel del simulador de API (protegido por sesión)
    @GetMapping("/simulate")
    public String simulate(HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        return "simulate";
    }

    // Obtiene y muestra la lista de Pokémon (protegido por sesión)
    @GetMapping("/pokedex")
    public String pokedexList(HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        
        try {
            String url = apiUrl + "/api/pokemon";
            // La API de Python devuelve un array JSON que Jackson parsea automáticamente a una lista (List)
            java.util.List pokemonList = restTemplate.getForObject(url, java.util.List.class);
            model.addAttribute("pokemonList", pokemonList);
        } catch (Exception e) {
            model.addAttribute("error", "Error obteniendo la lista de la Pokedex: " + e.getMessage());
        }
        
        return "pokedex-list";
    }

    // Muestra los detalles completos de un Pokémon concreto (protegido por sesión)
    @GetMapping("/pokedex/{name}")
    public String pokedexDetail(@org.springframework.web.bind.annotation.PathVariable String name, HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        
        try {
            String url = apiUrl + "/api/pokemon/" + name;
            // La API de Python devuelve un objeto JSON que Jackson parsea a un Map
            java.util.Map pokemonDetail = restTemplate.getForObject(url, java.util.Map.class);
            model.addAttribute("pokemon", pokemonDetail);
        } catch (Exception e) {
            model.addAttribute("error", "Error obteniendo los detalles del Pokémon: " + e.getMessage());
        }
        
        return "pokedex-detail";
    }

    // Prueba de conexión correcta con la base de datos a través de la API
    @GetMapping("/api/call/db")
    public String callApiDb(HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        String url = apiUrl + "/api/db/pokemon";
        String response = restTemplate.getForObject(url, String.class);
        model.addAttribute("successData", response);
        return "simulate";
    }

    // Prueba de conexión correcta con una API de terceros a través de nuestra API
    @GetMapping("/api/call/pokeapi")
    public String callApiPokeapi(@RequestParam(defaultValue = "pikachu") String name, HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        String url = apiUrl + "/api/pokemon/" + name;
        String response = restTemplate.getForObject(url, String.class);
        model.addAttribute("successData", response);
        return "simulate";
    }

    // Simula un error forzado de lectura de fichero
    @GetMapping("/api/error/file")
    public String triggerFileError(HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        String url = apiUrl + "/api/test/error/file";
        restTemplate.getForObject(url, String.class);
        return "simulate";
    }

    // Simula un error forzado de la base de datos
    @GetMapping("/api/error/db")
    public String triggerDbError(HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        String url = apiUrl + "/api/test/error/db";
        restTemplate.getForObject(url, String.class);
        return "simulate";
    }

    // Simula un error forzado de una API externa
    @GetMapping("/api/error/api")
    public String triggerApiError(HttpSession session, Model model) {
        if (!checkSession(session)) return "redirect:/login";
        addUsername(session, model);
        String url = apiUrl + "/api/test/error/api";
        restTemplate.getForObject(url, String.class);
        return "simulate";
    }
}
