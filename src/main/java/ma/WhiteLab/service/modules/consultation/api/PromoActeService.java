package ma.WhiteLab.service.modules.consultation.api; // or acte module

import ma.WhiteLab.service.modules.consultation.dto.ActeDTO;

public interface PromoActeService {
    double calculateDiscount(ActeDTO acte, String promoCode);
}
