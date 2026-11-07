package ua.duikt.restaurant.dao;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import ua.duikt.restaurant.model.Dish;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Запити-звіти: JOIN, фільтрація, агрегатні функції.
 *
 * Тут показано другий стиль Jdbi — Fluent API (у CRUD-DAO був SqlObject):
 *   jdbi.withHandle(handle -> handle.createQuery(sql)   // відкрити з'єднання, підготувати запит
 *           .bind("name", value)                        // іменовані параметри :name
 *           .map(ConstructorMapper.of(Row.class))       // рядок результату → record
 *           .list());                                   // виконати, зібрати List, закрити все
 * Fluent API зручний там, де текст SQL залежить від того, які фільтри вказав користувач.
 * Значення користувача й надалі йдуть тільки в параметри, а не в текст SQL, тож SQL-ін'єкція неможлива.
 */
public class ReportDao {

    private final Jdbi jdbi;

    public ReportDao(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /** Виконати SELECT з параметрами й перетворити кожен рядок на record типу type. */
    private <T> List<T> list(String sql, Map<String, Object> params, Class<T> type) {
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bindMap(params)
                .map(ConstructorMapper.of(type))
                .list());
    }

    // ---------- 1. JOIN: склад замовлень ----------

    public record OrderLine(int orderId, int tableNumber, String visitor, String waiter, String dish,
                            int quantity, BigDecimal price, BigDecimal lineSum, String paymentStatus) {
    }

    /**
     * Позиції замовлень разом з офіціантом, стравою і клієнтом.
     * INNER JOIN для обов'язкових зв'язків, LEFT JOIN для бронювання,
     * бо id_reservation може бути NULL (замовлення без бронювання не має відвідувача).
     * Псевдоніми AS збігаються з іменами полів record-а OrderLine — так Jdbi знає, що куди класти.
     *
     * @param orderId номер замовлення або null — усі замовлення
     */
    public List<OrderLine> orderLines(Integer orderId) {
        String sql = """
                SELECT o.id_order AS orderId, o.table_number AS tableNumber,
                       COALESCE(v.full_name, N'(без бронювання)') AS visitor,
                       w.full_name AS waiter, d.title AS dish, od.quantity, d.price,
                       od.quantity * d.price AS lineSum, o.payment_status AS paymentStatus
                FROM [Order] o
                JOIN Waiter w       ON w.id_waiter = o.id_waiter
                JOIN OrderDetail od ON od.id_order = o.id_order
                JOIN Dish d         ON d.id_dish = od.id_dish
                LEFT JOIN Reservation r ON r.id_reservation = o.id_reservation
                LEFT JOIN Visitor v     ON v.id_visitor = r.id_visitor
                """;
        Map<String, Object> params = new HashMap<>();
        if (orderId != null) {
            sql += "WHERE o.id_order = :orderId\n";
            params.put("orderId", orderId);
        }
        sql += "ORDER BY o.id_order, d.title";
        return list(sql, params, OrderLine.class);
    }

    // ---------- 2. Фільтрація: бронювання за статусом і періодом ----------

    public record ReservationRow(int id, LocalDateTime dateTime, int tableNumber, String visitor,
                                 String phone, int peopleCount, String status) {
    }

    /** Бронювання з умовами WHERE, які задає користувач. Будь-яку умову можна пропустити (null). */
    public List<ReservationRow> reservations(String status, LocalDateTime from, LocalDateTime to, Integer minPeople) {
        StringBuilder sql = new StringBuilder("""
                SELECT r.id_reservation AS id, r.date_time AS dateTime, r.table_number AS tableNumber,
                       v.full_name AS visitor, v.phone_number AS phone,
                       r.people_count AS peopleCount, r.reservation_status AS status
                FROM Reservation r
                JOIN Visitor v ON v.id_visitor = r.id_visitor
                WHERE 1 = 1
                """);
        Map<String, Object> params = new HashMap<>();
        if (status != null) {
            sql.append(" AND r.reservation_status = :status");
            params.put("status", status);
        }
        if (from != null) {
            sql.append(" AND r.date_time >= :from");
            params.put("from", from);
        }
        if (to != null) {
            sql.append(" AND r.date_time < :to");
            params.put("to", to);
        }
        if (minPeople != null) {
            sql.append(" AND r.people_count >= :minPeople");
            params.put("minPeople", minPeople);
        }
        sql.append(" ORDER BY r.date_time");
        return list(sql.toString(), params, ReservationRow.class);
    }

    // ---------- 3. Фільтрація: страви за назвою і ціною ----------

    /** LIKE з параметром: символи % додаються в Java, тому введений текст не може змінити запит. */
    public List<Dish> dishes(String titlePart, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder(
                "SELECT id_dish AS id, title, price, weight_grams AS weightGrams FROM Dish WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (titlePart != null) {
            sql.append(" AND title LIKE :title");
            params.put("title", "%" + titlePart + "%");
        }
        if (minPrice != null) {
            sql.append(" AND price >= :minPrice");
            params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= :maxPrice");
            params.put("maxPrice", maxPrice);
        }
        sql.append(" ORDER BY price, title");
        return list(sql.toString(), params, Dish.class);
    }

    // ---------- 4. Агрегація: показники офіціантів ----------

    public record WaiterStats(int waiterId, String waiter, int ordersCount, BigDecimal revenue,
                              BigDecimal avgCheck, BigDecimal tips) {
    }

    /**
     * COUNT / SUM / AVG по кожному офіціанту для замовлень з обраним статусом оплати.
     * Сума кожного замовлення спершу рахується у підзапиті t (SUM по його позиціях),
     * інакше чайові o.tips додалися б стільки разів, скільки страв у замовленні.
     * LEFT JOIN залишає в звіті офіціантів без замовлень (з нулями).
     */
    public List<WaiterStats> waiterStats(String paymentStatus) {
        String sql = """
                SELECT w.id_waiter AS waiterId, w.full_name AS waiter,
                       COUNT(o.id_order)              AS ordersCount,
                       COALESCE(SUM(t.order_sum), 0)  AS revenue,
                       COALESCE(AVG(t.order_sum), 0)  AS avgCheck,
                       COALESCE(SUM(o.tips), 0)       AS tips
                FROM Waiter w
                LEFT JOIN [Order] o ON o.id_waiter = w.id_waiter AND o.payment_status = :status
                LEFT JOIN (SELECT od.id_order, SUM(od.quantity * d.price) AS order_sum
                           FROM OrderDetail od
                           JOIN Dish d ON d.id_dish = od.id_dish
                           GROUP BY od.id_order) t ON t.id_order = o.id_order
                GROUP BY w.id_waiter, w.full_name
                ORDER BY revenue DESC, w.full_name
                """;
        return list(sql, Map.of("status", paymentStatus), WaiterStats.class);
    }

    // ---------- 5. Агрегація: популярність страв ----------

    public record DishPopularity(int dishId, String title, int ordersCount, int totalQuantity, BigDecimal revenue) {
    }

    /** Скільки разів і в якій кількості замовляли кожну страву; HAVING відсікає страви з меншою кількістю. */
    public List<DishPopularity> dishPopularity(int minQuantity) {
        String sql = """
                SELECT d.id_dish AS dishId, d.title,
                       COUNT(od.id_order)                      AS ordersCount,
                       COALESCE(SUM(od.quantity), 0)           AS totalQuantity,
                       COALESCE(SUM(od.quantity * d.price), 0) AS revenue
                FROM Dish d
                LEFT JOIN OrderDetail od ON od.id_dish = d.id_dish
                GROUP BY d.id_dish, d.title
                HAVING COALESCE(SUM(od.quantity), 0) >= :minQty
                ORDER BY totalQuantity DESC, d.title
                """;
        return list(sql, Map.of("minQty", minQuantity), DishPopularity.class);
    }
}
