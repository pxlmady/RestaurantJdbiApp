# RestaurantDB: консольний застосунок на Java з MicroORM Jdbi 3, ПР 3.2(6)

Той самий застосунок, що й у ПР 3.1(5) (CRUD для 7 таблиць RestaurantDB і 5 запитів:
JOIN, фільтрація, агрегатні функції), але шар доступу до даних переписано з «чистого» JDBC
на MicroORM **Jdbi 3**: SQL пишемо самі, а підставляння параметрів, читання `ResultSet`
і створення Java-об'єктів робить Jdbi.

## Запуск

1. Вимоги: JDK 17+, IntelliJ IDEA (Maven вбудований), запущений SQL Server з базою RestaurantDB
   (`docker start restaurantdb-mssql`).
2. Скопіюйте `db.properties.example` → `db.properties` і вкажіть `db.password` (той самий, що в ПР 3.1(5)).
3. IntelliJ IDEA: **File → Open** → папка з `pom.xml` → дочекатися імпорту Maven (завантажить Jdbi) →
   `src/main/java/ua/duikt/restaurant/Main.java` → зелений ▶ біля `main`.
4. Або з терміналу в корені проєкту: `chcp 65001`, потім `mvn -q compile exec:java`.

## Структура

```
src/main/java/ua/duikt/restaurant/
  Main.java            головне меню, обробка помилок (у т.ч. виключень Jdbi)
  db/Database.java     читає db.properties, створює об'єкт Jdbi і видає DAO (jdbi.onDemand)
  db/DataAccessException.java  зрозумілі повідомлення з кодів помилок SQL Server
  model/               record-и: рядки таблиць (без змін)
  dao/CrudDao.java     інтерфейс CRUD (без змін)
  dao/*Dao.java        ІНТЕРФЕЙСИ з анотаціями @SqlQuery / @SqlUpdate (Jdbi SqlObject)
  dao/ReportDao.java   5 запитів через Jdbi Fluent API (handle.createQuery(...).bind(...).map(...))
  ui/                  консольні меню й форми (без змін)
sql/demo_data.sql      НЕОБОВ'ЯЗКОВІ демо-дані
```

Пояснення та підготовка до захисту: `ПР3.2(6)_Пояснення_і_захист.md`.
