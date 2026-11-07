package ua.duikt.restaurant.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.duikt.restaurant.model.Waiter;

import java.util.List;
import java.util.Optional;

/** DAO таблиці Waiter (Jdbi SqlObject). */
@RegisterConstructorMapper(Waiter.class)
public interface WaiterDao extends CrudDao<Waiter, Integer> {

    @Override
    @SqlQuery("SELECT id_waiter AS id, full_name AS fullName FROM Waiter ORDER BY id_waiter")
    List<Waiter> findAll();

    @Override
    @SqlQuery("SELECT id_waiter AS id, full_name AS fullName FROM Waiter WHERE id_waiter = :id")
    Optional<Waiter> findById(@Bind("id") Integer id);

    @Override
    @SqlUpdate("INSERT INTO Waiter (full_name) VALUES (:fullName)")
    @GetGeneratedKeys
    Integer create(@BindMethods Waiter w);

    @Override
    @SqlUpdate("UPDATE Waiter SET full_name = :fullName WHERE id_waiter = :id")
    boolean update(@BindMethods Waiter w);

    @Override
    @SqlUpdate("DELETE FROM Waiter WHERE id_waiter = :id")
    boolean delete(@Bind("id") Integer id);
}
