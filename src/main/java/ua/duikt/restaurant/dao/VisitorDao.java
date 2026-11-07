package ua.duikt.restaurant.dao;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import ua.duikt.restaurant.model.Visitor;

import java.util.List;
import java.util.Optional;

/**
 * DAO таблиці Visitor у стилі Jdbi SqlObject.
 * Це лише ІНТЕРФЕЙС: реалізацію (відкрити з'єднання, PreparedStatement, підставити
 * параметри, пройти ResultSet, закрити все) Jdbi генерує сам під час виконання.
 *
 * - @SqlQuery     — SELECT; результат перетворюється на List / Optional моделі.
 * - @SqlUpdate    — INSERT / UPDATE / DELETE; boolean = "змінено хоч один рядок".
 * - :fullName     — іменований параметр (у JDBC це був знак ?).
 * - @BindMethods  — значення параметрів беруться з методів record-а: :fullName ← v.fullName().
 * - @RegisterConstructorMapper — рядок результату → new Visitor(id, fullName, phoneNumber):
 *   стовпці зіставляються з параметрами конструктора ЗА ІМЕНЕМ, тому в SELECT є псевдоніми AS.
 */
@RegisterConstructorMapper(Visitor.class)
public interface VisitorDao extends CrudDao<Visitor, Integer> {

    @Override
    @SqlQuery("SELECT id_visitor AS id, full_name AS fullName, phone_number AS phoneNumber "
            + "FROM Visitor ORDER BY id_visitor")
    List<Visitor> findAll();

    @Override
    @SqlQuery("SELECT id_visitor AS id, full_name AS fullName, phone_number AS phoneNumber "
            + "FROM Visitor WHERE id_visitor = :id")
    Optional<Visitor> findById(@Bind("id") Integer id);

    /** @GetGeneratedKeys: повернути значення IDENTITY, яке видав сервер. */
    @Override
    @SqlUpdate("INSERT INTO Visitor (full_name, phone_number) VALUES (:fullName, :phoneNumber)")
    @GetGeneratedKeys
    Integer create(@BindMethods Visitor v);

    @Override
    @SqlUpdate("UPDATE Visitor SET full_name = :fullName, phone_number = :phoneNumber WHERE id_visitor = :id")
    boolean update(@BindMethods Visitor v);

    @Override
    @SqlUpdate("DELETE FROM Visitor WHERE id_visitor = :id")
    boolean delete(@Bind("id") Integer id);
}
