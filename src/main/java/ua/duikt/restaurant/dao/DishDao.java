package ua.duikt.restaurant.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.duikt.restaurant.model.Dish;

import java.util.List;
import java.util.Optional;

/** DAO таблиці Dish (Jdbi SqlObject). price DECIMAL(10,2) ↔ BigDecimal Jdbi перетворює сам. */
@RegisterConstructorMapper(Dish.class)
public interface DishDao extends CrudDao<Dish, Integer> {

    @Override
    @SqlQuery("SELECT id_dish AS id, title, price, weight_grams AS weightGrams FROM Dish ORDER BY id_dish")
    List<Dish> findAll();

    @Override
    @SqlQuery("SELECT id_dish AS id, title, price, weight_grams AS weightGrams FROM Dish WHERE id_dish = :id")
    Optional<Dish> findById(@Bind("id") Integer id);

    @Override
    @SqlUpdate("INSERT INTO Dish (title, price, weight_grams) VALUES (:title, :price, :weightGrams)")
    @GetGeneratedKeys
    Integer create(@BindMethods Dish d);

    @Override
    @SqlUpdate("UPDATE Dish SET title = :title, price = :price, weight_grams = :weightGrams WHERE id_dish = :id")
    boolean update(@BindMethods Dish d);

    @Override
    @SqlUpdate("DELETE FROM Dish WHERE id_dish = :id")
    boolean delete(@Bind("id") Integer id);
}
