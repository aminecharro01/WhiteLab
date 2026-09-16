package ma.WhiteLab.mvc.dto;

import java.time.LocalDate;

public class OrdonnanceDTO {
    private Long id;
    private LocalDate dateOrdonnance;
    private String patientNomComplet;
    private String medecinNom;
    private int nombrePrescriptions;
    private Long dossierId;
    private Long patientId;

    public OrdonnanceDTO() {}

    public OrdonnanceDTO(Long id, LocalDate dateOrdonnance, String patientNomComplet,
                         String medecinNom, int nombrePrescriptions, Long dossierId, Long patientId) {
        this.id = id;
        this.dateOrdonnance = dateOrdonnance;
        this.patientNomComplet = patientNomComplet;
        this.medecinNom = medecinNom;
        this.nombrePrescriptions = nombrePrescriptions;
        this.dossierId = dossierId;
        this.patientId = patientId;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateOrdonnance() { return dateOrdonnance; }
    public void setDateOrdonnance(LocalDate dateOrdonnance) { this.dateOrdonnance = dateOrdonnance; }

    public String getPatientNomComplet() { return patientNomComplet; }
    public void setPatientNomComplet(String patientNomComplet) { this.patientNomComplet = patientNomComplet; }

    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    public int getNombrePrescriptions() { return nombrePrescriptions; }
    public void setNombrePrescriptions(int nombrePrescriptions) { this.nombrePrescriptions = nombrePrescriptions; }

    public Long getDossierId() { return dossierId; }
    public void setDossierId(Long dossierId) { this.dossierId = dossierId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}