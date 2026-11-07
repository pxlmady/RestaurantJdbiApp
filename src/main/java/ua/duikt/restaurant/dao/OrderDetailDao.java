package ua.duikt.restaurant.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.duikt.restaurant.model.OrderDetail;

import java.util.List;
import java.util.Optional;

/**
 * DAO таблиці OrderDetail зі складеним ключем (id_order, id_dish).
 * Ключ — record OrderDetail.Key; @BindMethods бере :orderId і :dishId з його методів.
 */
@RegisterConstructorMapper(OrderDetail.class)
public interface OrderDetailDao extends CrudDao<OrderDetail, OrderDetail.Key> {

    @Override
    @SqlQuery("SELECT id_order AS orderId, id_dish AS dishId, quantity "
            + "FROM OrderDetail ORDER BY id_order, id_dish")
    List<OrderDetail> findAll();

    @Override
    @SqlQuery("SELECT id_order AS orderId, id_dish AS dishId, quantity "
            + "FROM OrderDetail WHERE id_order = :orderId AND id_dish = :dishId")
    Optional<OrderDetail> findById(@BindMethods OrderDetail.Key key);

    @SqlUpdate("INSERT INTO OrderDetail (id_order, id_dish, quantity) VALUES (:orderId, :dishId, :quantity)")
    void insert(@BindMethods OrderDetail d);

    /** Ключ складається з уведених значень, тому просто повертаємо його після INSERT. */
    @Override
    default OrderDetail.Key create(OrderDetail d) {
        insert(d);
        return d.key();
    }

    /** Ключ (замовлення + страва) не змінюється, змінюється лише кількість. */
    @Override
    @SqlUpdate("UPDATE OrderDetail SET quantity = :quantity WHERE id_order = :orderId AND id_dish = :dishId")
    boolean update(@BindMethods OrderDetail d);

    @Override
    @SqlUpdate("DELETE FROM OrderDetail WHERE id_order = :orderId AND id_dish = :dishId")
    boolean delete(@BindMethods OrderDetail.Key key);
}
