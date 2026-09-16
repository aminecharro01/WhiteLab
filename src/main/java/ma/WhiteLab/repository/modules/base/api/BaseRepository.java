package ma.WhiteLab.repository.modules.base.api;

import java.util.List;
import java.util.Optional;
//auteur : Aymane Akarbich

public interface BaseRepository<T> {

    Optional<T> findById(Long id);

    List<T> findAll();

    boolean deleteById(Long id);

    long count();

    boolean existsById(Long id);
}
