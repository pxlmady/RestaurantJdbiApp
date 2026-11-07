package ua.duikt.restaurant.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.duikt.restaurant.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * DAO таблиці [Order]. id_reservation може бути NULL: у record-і це Integer,
 * і Jdbi сам передає NULL у запит та читає NULL з результату (у JDBC для цього
 * були setNull і getNullableInt). Видалення замовлення каскадно видаляє його позиції
 * (FK_OrderDetail_Order ON DELETE CASCADE) — це робить SQL Server.
 */
@RegisterConstructorMapper(Order.class)
public interface OrderDao extends CrudDao<Order, Integer> {

    @Override
    @SqlQuery("SELECT id_order AS id, id_reservation AS reservationId, id_waiter AS waiterId, "
            + "payment_status AS paymentStatus, table_number AS tableNumber, tips "
            + "FROM [Order] ORDER BY id_order")
    List<Order> findAll();

    @Override
    @SqlQuery("SELECT id_order AS id, id_reservation AS reservationId, id_waiter AS waiterId, "
            + "payment_status AS paymentStatus, table_number AS tableNumber, tips "
            + "FROM [Order] WHERE id_order = :id")
    Optional<Order> findById(@Bind("id") Integer id);

    @Override
    @SqlUpdate("INSERT INTO [Order] (id_reservation, id_waiter, payment_status, table_number, tips) "
            + "VALUES (:reservationId, :waiterId, :paymentStatus, :tableNumber, :tips)")
    @GetGeneratedKeys
    Integer create(@BindMethods Order o);

    @Override
    @SqlUpdate("UPDATE [Order] SET id_reservation = :reservationId, id_waiter = :waiterId, "
            + "payment_status = :paymentStatus, table_number = :tableNumber, tips = :tips WHERE id_order = :id")
    boolean update(@BindMethods Order o);

    @Override
    @SqlUpdate("DELETE FROM [Order] WHERE id_order = :id")
    boolean delete(@Bind("id") Integer id);
}
