package ma.WhiteLab.mvc.dto;

public class PrescriptionDetailDTO {
    private String medicamentNom;
    private int quantite;
    private String frequence;
    private int dureeJours;

    public PrescriptionDetailDTO(String medicamentNom, int quantite, String frequence, int dureeJours) {
        this.medicamentNom = medicamentNom;
        this.quantite = quantite;
        this.frequence = frequence;
        this.dureeJours = dureeJours;
    }

    public String getMedicamentNom() { return medicamentNom; }
    public int getQuantite() { return quantite; }
    public String getFrequence() { return frequence; }
    public int getDureeJours() { return dureeJours; }
}