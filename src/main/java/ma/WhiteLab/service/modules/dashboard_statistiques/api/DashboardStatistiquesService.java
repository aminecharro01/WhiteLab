package ma.WhiteLab.service.modules.dashboard_statistiques.api;

import ma.WhiteLab.service.modules.dashboard_statistiques.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardStatistiquesService {

    // 🔹 Statistiques globales (cartes du dashboard)
    DashboardGlobalDTO getStatistiquesGlobales(
            Long cabinetId,
            LocalDate dateDebut,
            LocalDate dateFin
    );

    // 🔹 Chiffre d'affaires par mois (graphique)
    List<ChiffreAffairesParMoisDTO> getChiffreAffairesParMois(
            Long cabinetId,
            int annee
    );

    // 🔹 Top actes dentaires
    List<TopActeDTO> getTopActes(
            Long cabinetId,
            LocalDate dateDebut,
            LocalDate dateFin,
            int limit
    );
}
