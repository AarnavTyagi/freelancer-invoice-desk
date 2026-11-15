package com.freelancedesk.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Generic data-access contract shared by every DAO. */
public interface Repository<T, ID> {
    T save(T entity) throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;
}
