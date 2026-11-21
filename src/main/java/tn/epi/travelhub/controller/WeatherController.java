package tn.epi.travelhub.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.epi.travelhub.dto.WeatherDto;
import tn.epi.travelhub.service.WeatherService;

/**
 * Meteo de n'importe quelle ville : GET /api/weather?city=Tunis
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherDto byCity(@RequestParam String city) {
        return weatherService.getWeatherByCity(city);
    }
}
