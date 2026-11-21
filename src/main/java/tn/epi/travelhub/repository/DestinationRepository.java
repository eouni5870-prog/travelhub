package tn.epi.travelhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.epi.travelhub.model.Destination;
import tn.epi.travelhub.model.TravelStatus;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    List<Destination> findByStatus(TravelStatus status);

    List<Destination> findByCityContainingIgnoreCaseOrCountryContainingIgnoreCase(String city, String country);

    long countByStatus(TravelStatus status);
}
