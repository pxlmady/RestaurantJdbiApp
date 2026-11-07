package ua.duikt.restaurant.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.duikt.restaurant.model.RestaurantTable;

import java.util.List;
import java.util.Optional;

/**
 * DAO таблиці [Table]. Назва збігається з ключовим словом SQL, тому в квадратних дужках.
 * Номер столика вводить користувач (не IDENTITY), тому create — це default-метод:
 * він викликає згенерований Jdbi insert(...) і повертає вже відомий номер.
 */
@RegisterConstructorMapper(RestaurantTable.class)
public interface TableDao extends CrudDao<RestaurantTable, Integer> {

    @Override
    @SqlQuery("SELECT table_number AS tableNumber, capacity FROM [Table] ORDER BY table_number")
    List<RestaurantTable> findAll();

    @Override
    @SqlQuery("SELECT table_number AS tableNumber, capacity FROM [Table] WHERE table_number = :number")
    Optional<RestaurantTable> findById(@Bind("number") Integer number);

    @SqlUpdate("INSERT INTO [Table] (table_number, capacity) VALUES (:tableNumber, :capacity)")
    void insert(@BindMethods RestaurantTable t);

    @Override
    default Integer create(RestaurantTable t) {
        insert(t);
        return t.tableNumber();
    }

    @Override
    @SqlUpdate("UPDATE [Table] SET capacity = :capacity WHERE table_number = :tableNumber")
    boolean update(@BindMethods RestaurantTable t);

    @Override
    @SqlUpdate("DELETE FROM [Table] WHERE table_number = :number")
    boolean delete(@Bind("number") Integer number);
}
