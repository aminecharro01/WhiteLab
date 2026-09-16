package ma.WhiteLab.mvc.dto;

import java.time.LocalDate;

public class PrescriptionDTO {
    private Long id;
    private String medicamentNom;
    private int quantite;
    private String frequence;
    private int dureeJours;
    private LocalDate dateOrdonnance;
    private String patientNomComplet;
    private Long ordonnanceId;

    public PrescriptionDTO() {}

    public PrescriptionDTO(Long id, String medicamentNom, int quantite, String frequence,
                           int dureeJours, LocalDate dateOrdonnance, String patientNomComplet, Long ordonnanceId) {
        this.id = id;
        this.medicamentNom = medicamentNom;
        this.quantite = quantite;
        this.frequence = frequence;
        this.dureeJours = dureeJours;
        this.dateOrdonnance = dateOrdonnance;
        this.patientNomComplet = patientNomComplet;
        this.ordonnanceId = ordonnanceId;
    }

    // Getters
    public Long getId() { return id; }
    public String getMedicamentNom() { return medicamentNom; }
    public int getQuantite() { return quantite; }
    public String getFrequence() { return frequence; }
    public int getDureeJours() { return dureeJours; }
    public LocalDate getDateOrdonnance() { return dateOrdonnance; }
    public String getPatientNomComplet() { return patientNomComplet; }
    public Long getOrdonnanceId() { return ordonnanceId; }
}