package tn.epi.travelhub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "destinations")
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 80)
    private String city;

    @NotBlank(message = "Le pays est obligatoire")
    @Size(max = 80)
    private String country;

    @Size(max = 500)
    private String description;

    @PositiveOrZero(message = "Le budget doit etre positif")
    private Double budget;

    private LocalDate plannedDate;

    @Enumerated(EnumType.STRING)
    private TravelStatus status = TravelStatus.PLANNED;

    // Remplies automatiquement grace a l'API de geocodage Open-Meteo
    private Double latitude;
    private Double longitude;

    public Destination() {
    }

    public Destination(String city, String country, String description,
                       Double budget, LocalDate plannedDate, TravelStatus status) {
        this.city = city;
        this.country = country;
        this.description = description;
        this.budget = budget;
        this.plannedDate = plannedDate;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }

    public TravelStatus getStatus() { return status; }
    public void setStatus(TravelStatus status) { this.status = status; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
