package ua.duikt.restaurant.db;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;

/**
 * Єдине місце, яке знає, ДЕ лежить база і ЯК до неї підключитися.
 * Параметри читаються з db.properties (url, user, password), пароль не зашитий у код.
 *
 * Різниця з ПР3.1(5): замість DriverManager.getConnection() тут створюється
 * об'єкт Jdbi — "фабрика" з'єднань MicroORM. Сам Jdbi з'єднання не тримає:
 * на кожну операцію він відкриває Handle (обгортку над java.sql.Connection)
 * і сам закриває його після виконання.
 */
public final class Database {

    private final Jdbi jdbi;

    private Database(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /** Читає db.properties з поточної папки або зі шляху -Ddb.config=... */
    public static Database fromPropertiesFile() {
        Path path = Path.of(System.getProperty("db.config", "db.properties"));
        if (!Files.exists(path)) {
            throw new DataAccessException("Не знайдено файл налаштувань " + path.toAbsolutePath()
                    + ". Скопіюйте db.properties.example у db.properties і вкажіть пароль.");
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException e) {
            throw new DataAccessException("Не вдалося прочитати " + path + ": " + e.getMessage(), e);
        }
        String url = p.getProperty("db.url");
        if (url == null || url.isBlank()) {
            throw new DataAccessException("У " + path + " не задано db.url");
        }
        Jdbi jdbi = Jdbi.create(url.trim(), p.getProperty("db.user", "").trim(), p.getProperty("db.password", ""));
        // Плагін SqlObject дозволяє оголошувати DAO як інтерфейси з анотаціями @SqlQuery / @SqlUpdate
        jdbi.installPlugin(new SqlObjectPlugin());
        return new Database(jdbi);
    }

    public Jdbi jdbi() {
        return jdbi;
    }

    /**
     * Готова реалізація DAO-інтерфейсу. Jdbi генерує її сам (динамічний проксі);
     * onDemand означає: на кожен виклик методу береться нове з'єднання і одразу закривається.
     */
    public <T> T dao(Class<T> daoInterface) {
        return jdbi.onDemand(daoInterface);
    }

    /** Перевірка підключення: повертає назву бази та версію сервера. */
    public String describeConnection() {
        try {
            return jdbi.withHandle(handle -> {
                Connection c = handle.getConnection();
                DatabaseMetaData md = c.getMetaData();
                return "Підключено: база " + c.getCatalog() + ", " + md.getDatabaseProductName() + " "
                        + md.getDatabaseProductVersion() + ", користувач " + md.getUserName();
            });
        } catch (JdbiException e) {
            throw DataAccessException.from("Не вдалося підключитися до SQL Server", e);
        } catch (java.sql.SQLException e) {
            throw DataAccessException.from("Не вдалося підключитися до SQL Server", e);
        }
    }
}
