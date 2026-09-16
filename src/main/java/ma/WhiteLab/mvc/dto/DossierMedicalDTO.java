package ma.WhiteLab.mvc.dto;

import java.time.LocalDateTime;

public class DossierMedicalDTO {
    private Long id;
    private Long patientId;        // Nouveau : pour l'export PDF
    private String patientNom;
    private String patientPrenom;
    private String medecinNom;
    private LocalDateTime dateCreation;
    private String historique;
    private int nombreOrdonnances;

    // Constructeur complet utilisé dans le controller
    public DossierMedicalDTO(Long id, Long patientId, String patientNom, String patientPrenom,
                             String medecinNom, LocalDateTime dateCreation,
                             String historique, int nombreOrdonnances) {
        this.id = id;
        this.patientId = patientId;
        this.patientNom = patientNom;
        this.patientPrenom = patientPrenom;
        this.medecinNom = medecinNom;
        this.dateCreation = dateCreation;
        this.historique = historique;
        this.nombreOrdonnances = nombreOrdonnances;
    }

    public DossierMedicalDTO() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getPatientPrenom() { return patientPrenom; }
    public void setPatientPrenom(String patientPrenom) { this.patientPrenom = patientPrenom; }

    public String getMedecinNom() { return medecinNom; }
    public void setMedecinNom(String medecinNom) { this.medecinNom = medecinNom; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getHistorique() { return historique; }
    public void setHistorique(String historique) { this.historique = historique; }

    public int getNombreOrdonnances() { return nombreOrdonnances; }
    public void setNombreOrdonnances(int nombreOrdonnances) { this.nombreOrdonnances = nombreOrdonnances; }
}