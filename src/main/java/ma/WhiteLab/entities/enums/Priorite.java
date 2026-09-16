package ma.WhiteLab.entities.enums;

public enum Priorite {
    BASSE("Basse"),
    MOYENNE("Moyenne"),
    HAUTE("Haute"),
    CRITIQUE("Critique");

    private final String libelle;

    Priorite(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}