package ma.WhiteLab.common.utils;

import java.sql.Connection;

@FunctionalInterface
public interface RepoFactory<T> {
    T create(Connection c);
}
