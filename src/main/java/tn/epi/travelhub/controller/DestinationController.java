package tn.epi.travelhub.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.epi.travelhub.dto.StatsDto;
import tn.epi.travelhub.dto.WeatherDto;
import tn.epi.travelhub.model.Destination;
import tn.epi.travelhub.model.TravelStatus;
import tn.epi.travelhub.service.DestinationService;

import java.util.List;

/**
 * Service web REST : CRUD des destinations.
 */
@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService service;

    public DestinationController(DestinationService service) {
        this.service = service;
    }

    // GET /api/destinations?search=paris  ou  ?status=VISITED
    @GetMapping
    public List<Destination> getAll(@RequestParam(required = false) String search,
                                    @RequestParam(required = false) TravelStatus status) {
        return service.findAll(search, status);
    }

    @GetMapping("/{id}")
    public Destination getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Destination> create(@Valid @RequestBody Destination destination) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(destination));
    }

    @PutMapping("/{id}")
    public Destination update(@PathVariable Long id, @Valid @RequestBody Destination destination) {
        return service.update(id, destination);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Meteo de la destination (via l'API externe Open-Meteo)
    @GetMapping("/{id}/weather")
    public WeatherDto weather(@PathVariable Long id) {
        return service.weatherFor(id);
    }

    @GetMapping("/stats")
    public StatsDto stats() {
        return service.stats();
    }
}
