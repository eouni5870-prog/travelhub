package tn.epi.travelhub.dto;

import java.util.List;

/**
 * Meteo renvoyee au front (donnees simplifiees a partir de l'API Open-Meteo).
 */
public record WeatherDto(
        String city,
        double latitude,
        double longitude,
        double currentTemperature,
        double windSpeed,
        String currentDescription,
        String currentIcon,
        List<DailyForecast> forecast
) {
    public record DailyForecast(String date, double min, double max, String description, String icon) {
    }
}
