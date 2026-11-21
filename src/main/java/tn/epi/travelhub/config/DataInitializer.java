package tn.epi.travelhub.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.epi.travelhub.model.Destination;
import tn.epi.travelhub.model.TravelStatus;
import tn.epi.travelhub.service.DestinationService;

import java.time.LocalDate;

/**
 * Ajoute quelques destinations au premier demarrage.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final DestinationService service;

    public DataInitializer(DestinationService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (!service.findAll(null, null).isEmpty()) {
            return;
        }
        service.create(new Destination("Rome", "Italie", "Colisee, Vatican et vraies pizzas.",
                1800.0, LocalDate.now().plusMonths(3), TravelStatus.PLANNED));
        service.create(new Destination("Paris", "France", "Tour Eiffel et musee du Louvre.",
                2200.0, LocalDate.now().plusMonths(6), TravelStatus.PLANNED));
        service.create(new Destination("Djerba", "Tunisie", "Plages et Houmt Souk.",
                600.0, LocalDate.now().minusMonths(4), TravelStatus.VISITED));
        service.create(new Destination("Istanbul", "Turquie", "Sainte-Sophie et le Bosphore.",
                1500.0, LocalDate.now().minusYears(1), TravelStatus.VISITED));
    }
}
