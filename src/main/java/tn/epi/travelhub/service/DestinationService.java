package tn.epi.travelhub.service;

import org.springframework.stereotype.Service;
import tn.epi.travelhub.dto.StatsDto;
import tn.epi.travelhub.dto.WeatherDto;
import tn.epi.travelhub.exception.ExternalServiceException;
import tn.epi.travelhub.exception.ResourceNotFoundException;
import tn.epi.travelhub.model.Destination;
import tn.epi.travelhub.model.TravelStatus;
import tn.epi.travelhub.repository.DestinationRepository;

import java.util.List;

@Service
public class DestinationService {

    private final DestinationRepository repository;
    private final WeatherService weatherService;

    public DestinationService(DestinationRepository repository, WeatherService weatherService) {
        this.repository = repository;
        this.weatherService = weatherService;
    }

    public List<Destination> findAll(String search, TravelStatus status) {
        if (search != null && !search.isBlank()) {
            return repository.findByCityContainingIgnoreCaseOrCountryContainingIgnoreCase(search, search);
        }
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    public Destination findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination introuvable : id=" + id));
    }

    public Destination create(Destination d) {
        d.setId(null);
        fillCoordinates(d);
        return repository.save(d);
    }

    public Destination update(Long id, Destination data) {
        Destination existing = findById(id);
        boolean cityChanged = !existing.getCity().equalsIgnoreCase(data.getCity());
        existing.setCity(data.getCity());
        existing.setCountry(data.getCountry());
        existing.setDescription(data.getDescription());
        existing.setBudget(data.getBudget());
        existing.setPlannedDate(data.getPlannedDate());
        existing.setStatus(data.getStatus() == null ? TravelStatus.PLANNED : data.getStatus());
        if (cityChanged || existing.getLatitude() == null) {
            fillCoordinates(existing);
        }
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    public WeatherDto weatherFor(Long id) {
        Destination d = findById(id);
        if (d.getLatitude() == null) {
            fillCoordinates(d);
            repository.save(d);
        }
        if (d.getLatitude() == null) {
            throw new ExternalServiceException("Coordonnees introuvables pour " + d.getCity());
        }
        return weatherService.getWeather(d.getCity(), d.getLatitude(), d.getLongitude());
    }

    public StatsDto stats() {
        double budget = repository.findAll().stream()
                .mapToDouble(d -> d.getBudget() == null ? 0 : d.getBudget())
                .sum();
        return new StatsDto(repository.count(),
                repository.countByStatus(TravelStatus.PLANNED),
                repository.countByStatus(TravelStatus.VISITED),
                budget);
    }

    /** Appelle l'API externe ; si elle echoue, la destination est quand meme enregistree. */
    private void fillCoordinates(Destination d) {
        try {
            double[] c = weatherService.geocode(d.getCity());
            d.setLatitude(c[0]);
            d.setLongitude(c[1]);
        } catch (ExternalServiceException e) {
            d.setLatitude(null);
            d.setLongitude(null);
        }
    }
}
