package ma.WhiteLab.entities.enums;

public enum Jour {
    LUNDI("Lundi"),
    MARDI("Mardi"),
    MERCREDI("Mercredi"),
    JEUDI("Jeudi"),
    VENDREDI("Vendredi"),
    SAMEDI("Samedi"),
    DIMANCHE("Dimanche");

    private final String displayName;

    Jour(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Jour fromString(String text) {
        if (text != null) {
            for (Jour j : Jour.values()) {
                if (text.equalsIgnoreCase(j.name()) || text.equalsIgnoreCase(j.displayName)) {
                    return j;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text '" + text + "' found in enum Jour");
    }
}
