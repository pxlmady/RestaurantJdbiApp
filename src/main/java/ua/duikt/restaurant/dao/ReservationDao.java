package ua.duikt.restaurant.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.duikt.restaurant.model.Reservation;

import java.util.List;
import java.util.Optional;

/** DAO таблиці Reservation. date_time (datetime) ↔ LocalDateTime Jdbi перетворює сам. */
@RegisterConstructorMapper(Reservation.class)
public interface ReservationDao extends CrudDao<Reservation, Integer> {

    @Override
    @SqlQuery("SELECT id_reservation AS id, id_visitor AS visitorId, table_number AS tableNumber, "
            + "date_time AS dateTime, people_count AS peopleCount, reservation_status AS status "
            + "FROM Reservation ORDER BY date_time, id_reservation")
    List<Reservation> findAll();

    @Override
    @SqlQuery("SELECT id_reservation AS id, id_visitor AS visitorId, table_number AS tableNumber, "
            + "date_time AS dateTime, people_count AS peopleCount, reservation_status AS status "
            + "FROM Reservation WHERE id_reservation = :id")
    Optional<Reservation> findById(@Bind("id") Integer id);

    @Override
    @SqlUpdate("INSERT INTO Reservation (id_visitor, table_number, date_time, people_count, reservation_status) "
            + "VALUES (:visitorId, :tableNumber, :dateTime, :peopleCount, :status)")
    @GetGeneratedKeys
    Integer create(@BindMethods Reservation r);

    @Override
    @SqlUpdate("UPDATE Reservation SET id_visitor = :visitorId, table_number = :tableNumber, date_time = :dateTime, "
            + "people_count = :peopleCount, reservation_status = :status WHERE id_reservation = :id")
    boolean update(@BindMethods Reservation r);

    @Override
    @SqlUpdate("DELETE FROM Reservation WHERE id_reservation = :id")
    boolean delete(@Bind("id") Integer id);
}
