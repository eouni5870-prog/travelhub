package tn.epi.travelhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tn.epi.travelhub.dto.WeatherDto;
import tn.epi.travelhub.exception.ExternalServiceException;

import java.util.ArrayList;
import java.util.List;

/**
 * Consommation du service web externe gratuit Open-Meteo :
 *  - API de geocodage : ville -> latitude / longitude
 *  - API meteo        : latitude / longitude -> meteo actuelle + previsions 5 jours
 */
@Service
public class WeatherService {

    private final RestClient restClient = RestClient.create();

    @Value("${external.geocoding.url}")
    private String geocodingUrl;

    @Value("${external.weather.url}")
    private String weatherUrl;

    /** Renvoie {latitude, longitude} d'une ville. */
    public double[] geocode(String city) {
        String url = UriComponentsBuilder.fromHttpUrl(geocodingUrl)
                .queryParam("name", city)
                .queryParam("count", 1)
                .queryParam("language", "fr")
                .encode().toUriString();
        try {
            JsonNode root = restClient.get().uri(url).retrieve().body(JsonNode.class);
            JsonNode results = root == null ? null : root.path("results");
            if (results == null || !results.isArray() || results.isEmpty()) {
                throw new ExternalServiceException("Ville introuvable : " + city);
            }
            JsonNode first = results.get(0);
            return new double[]{first.path("latitude").asDouble(), first.path("longitude").asDouble()};
        } catch (RestClientException e) {
            throw new ExternalServiceException("Service de geocodage indisponible");
        }
    }

    public WeatherDto getWeatherByCity(String city) {
        double[] coords = geocode(city);
        return getWeather(city, coords[0], coords[1]);
    }

    public WeatherDto getWeather(String city, double lat, double lon) {
        String url = UriComponentsBuilder.fromHttpUrl(weatherUrl)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("current", "temperature_2m,wind_speed_10m,weather_code")
                .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min")
                .queryParam("timezone", "auto")
                .queryParam("forecast_days", 5)
                .encode().toUriString();
        try {
            JsonNode root = restClient.get().uri(url).retrieve().body(JsonNode.class);
            if (root == null) {
                throw new ExternalServiceException("Reponse meteo vide");
            }
            JsonNode current = root.path("current");
            int code = current.path("weather_code").asInt();

            JsonNode daily = root.path("daily");
            List<WeatherDto.DailyForecast> forecast = new ArrayList<>();
            for (int i = 0; i < daily.path("time").size(); i++) {
                int dCode = daily.path("weather_code").get(i).asInt();
                forecast.add(new WeatherDto.DailyForecast(
                        daily.path("time").get(i).asText(),
                        daily.path("temperature_2m_min").get(i).asDouble(),
                        daily.path("temperature_2m_max").get(i).asDouble(),
                        describe(dCode), icon(dCode)));
            }
            return new WeatherDto(city, lat, lon,
                    current.path("temperature_2m").asDouble(),
                    current.path("wind_speed_10m").asDouble(),
                    describe(code), icon(code), forecast);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Service meteo indisponible");
        }
    }

    /** Codes meteo WMO utilises par Open-Meteo. */
    private String describe(int code) {
        if (code == 0) return "Ciel degage";
        if (code <= 3) return "Partiellement nuageux";
        if (code <= 48) return "Brouillard";
        if (code <= 67) return "Pluie";
        if (code <= 77) return "Neige";
        if (code <= 82) return "Averses";
        return "Orage";
    }

    private String icon(int code) {
        if (code == 0) return "☀️";
        if (code <= 3) return "⛅";
        if (code <= 48) return "🌫️";
        if (code <= 67) return "🌧️";
        if (code <= 77) return "❄️";
        if (code <= 82) return "🌦️";
        return "⛈️";
    }
}
