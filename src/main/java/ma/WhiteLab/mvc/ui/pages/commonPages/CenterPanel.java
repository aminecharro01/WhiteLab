package ma.WhiteLab.mvc.ui.pages.commonPages;

import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class CenterPanel extends JPanel {

    private final CardLayout cardLayout = new CardLayout();
    private final Map<ApplicationPages, JComponent> pages = new EnumMap<>(ApplicationPages.class);

    public CenterPanel() {
        setLayout(cardLayout);
        setOpaque(false);
    }

    public void registerPage(ApplicationPages page, JComponent view) {
        upsertPage(page, view);
    }

    /**
     * Ajoute ou remplace la vue d'une page (utile quand le controller retourne un nouveau panel).
     */
    public void upsertPage(ApplicationPages page, JComponent view) {
        if (page == null || view == null) return;

        JComponent old = pages.put(page, view);
        if (old != null) {
            remove(old);  // Bonnes pratiques : nettoyer l'ancien composant
        }

        add(view, page.name());

        // Important : forcer la mise à jour quand on ajoute/modifie
        revalidate();
        repaint();
    }

    /**
     * Affiche une page déjà enregistrée.
     * CRITICAL FIX : ajouter revalidate() + repaint() ici !
     */
    public void showPage(ApplicationPages page) {
        if (page == null || !pages.containsKey(page)) {
            return;
        }

        cardLayout.show(this, page.name());

        // === LA CLÉ DU FIX : Swing a besoin d'être notifié du changement de carte ===
        revalidate();
        repaint();
    }
}