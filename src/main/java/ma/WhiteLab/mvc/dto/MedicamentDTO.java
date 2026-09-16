package ma.WhiteLab.mvc.dto;

import ma.WhiteLab.entities.enums.Forme;

public class MedicamentDTO {

    private Long id;
    private String nom;
    private String type;
    private String laboratoire;
    private Forme forme;
    private double prixUnitaire;
    private boolean remboursable;

    public MedicamentDTO() {}

    public MedicamentDTO(Long id, String nom, String type,
                         String laboratoire, Forme forme,
                         double prixUnitaire, boolean remboursable) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.laboratoire = laboratoire;
        this.forme = forme;
        this.prixUnitaire = prixUnitaire;
        this.remboursable = remboursable;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public String getLaboratoire() { return laboratoire; }
    public Forme getForme() { return forme; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public boolean isRemboursable() { return remboursable; }
}
