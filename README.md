# Лабораторні роботи з курсу «Розробка програмного забезпечення на платформі Java»

## Інформація про студента
- **Студент:** Андронов Данило
- **Група:** ІС-43
- **Порядковий номер у списку групи:** 1
- **Розрахунок варіанта:**
  $$V = N \bmod 3 = 1 \bmod 3 = 1$$
- **Загальний варіант для всіх робіт:** **Варіант 1**

---

## Структура репозиторію

Проєкт організовано як єдиний модуль із розподілом завдань за офіційними пакетами кафедри:

```
src/
├── main/java/ua/kpi/comsys/
│   ├── dop/                     # Лабораторна робота №1: Data-Oriented Programming
│   │   ├── Transaction.java     # Sealed interface
│   │   ├── Deposit.java         # Record
│   │   ├── Withdrawal.java      # Record
│   │   ├── Transfer.java        # Record
│   │   └── TransactionProcessor.java # Switch pattern matching + Text Blocks
│   ├── collections/             # Лабораторна робота №2: Collections Framework
│   │   └── ActionHistoryManager.java # LIFO Deque з обмеженням ємності maxCapacity
│   └── streams/                 # Лабораторна робота №3: Stream API & Gatherers
│       ├── SensorMetric.java    # IoT Metric Record
│       └── TelemetryStreamAnalyzer.java # Short-circuiting + Custom Stream Gatherer
└── test/java/ua/kpi/comsys/
    ├── dop/
    │   └── TransactionProcessorTest.java       # 39 юніт-тестів
    ├── collections/
    │   └── ActionHistoryManagerTest.java       # 19 юніт-тестів
    └── streams/
        └── TelemetryStreamAnalyzerTest.java    # 23 юніт-тести з @Timeout(2)
```

---

## Опис лабораторних робіт

### Лабораторна робота №1: Сучасні можливості Java та підхід Data-Oriented Programming (DOP)
- **Варіант V = 1:** «Фінансова система транзакцій»
- **Реалізація:**
  - `sealed interface Transaction permits Deposit, Withdrawal, Transfer`
  - Незмінні записи `Deposit`, `Withdrawal`, `Transfer` із валідацією полів та додатних сум у компактних конструкторах.
  - Клас `TransactionProcessor` з вичерпним `switch expression` (pattern matching без `default`) та форматуванням результату за шаблоном через Text Blocks (`"""`).
  - Покриття тестами: 39 тестів (валідація, граничні випадки, коректність форматування, обробка `null`).

### Лабораторна робота №2: Збірка проєкту за допомогою Gradle та робота з Java Collections Framework
- **Варіант V = 1:** «Менеджер історії дій з обмеженням ємності»
- **Реалізація:**
  - Клас `ActionHistoryManager(int maxCapacity)` на базі двонаправленої черги `java.util.Deque` (`ArrayDeque`).
  - Дисципліна LIFO: `performAction()`, `undo()`, `peekRecentAction()`.
  - Автоматичне витіснення найстарішої дії при перевищенні `maxCapacity`.
  - Метод `getHistory()` повертає захисну копію списку без мутації внутрішнього стану.
  - Покриття тестами: 19 тестів (валідація ємності, LIFO-порядок, витіснення за лімітом, захисне копіювання, очищення).

### Лабораторна робота №3: Stream API, Optionals та кастомні Stream Gatherers
- **Варіант V = 1:** «Телеметрія IoT-пристроїв у реальному часі»
- **Реалізація:**
  - Робота з нескінченними потоками (`Stream.generate`) без блокування програми.
  - `findFirstAnomaly(Stream, targetDeviceId, criticalThreshold)`: ефективне коротке замикання через `findFirst()` з поверненням безпечного `Optional`.
  - `streamActiveMetrics(Stream, normalThreshold, maxCount)`: ледачий пропуск через `dropWhile()` та обмеження вибірки через `limit()`.
  - `calculateMovingAverages(Stream, metricType, windowSize, outputLimit)`: реалізація **кастомного послідовного Stream Gatherer** (`Gatherer.ofSequential`) зі збереженням внутрішнього вікна (`windowSize`) для динамічного розрахунку ковзного середнього.
  - Тести захищені таймаутами `@Timeout(2)` проти нескінченного зависання.

---

## Інструкція із запуску та перевірки

Проєкт використовує систему збірки **Gradle** з каталогом версій **Version Catalog** (`gradle/libs.versions.toml`) та можливостями **Java 23** (`--enable-preview` для Stream Gatherers).

### Збірка всього проєкту:
```bash
./gradlew build
```

### Запуск усіх модульних тестів (81 тест):
```bash
./gradlew test
```

### Запуск тестів окремої лабораторної роботи:
- **Тільки ЛР 1 (DOP):**
  ```bash
  ./gradlew test --tests "*dop*"
  ```
- **Тільки ЛР 2 (Collections):**
  ```bash
  ./gradlew test --tests "*collections*"
  ```
- **Тільки ЛР 3 (Streams):**
  ```bash
  ./gradlew test --tests "*streams*"
  ```

### Перегляд звіту:
Детальний звіт проходження всіх тестів:
`build/reports/tests/test/index.html`
