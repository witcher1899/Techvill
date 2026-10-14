# training.uz2.tests

Автотесты для проверки интеграций training-проекта.

## Как настроить проект локально

После клонирования репозитория для запуска тестов нужен токен доступа к корпоративному Vault.

Файл `src/main/resources/vault.properties` уже лежит в репозитории — **значение `VAULT_TOKEN` нужно запросить у тестировщиков УЗ 2.0** и подставить локально. В Git коммить только с пустым токеном, как в примере:

```properties
VAULT_ADDR=https://vault-gt.svc.vkusvill.ru/
VAULT_TOKEN=
SECRET_PATH=sandbox-qa/data/uz2
```

Не коммитьте `vault.properties` с реальным токеном и не передавайте его в MR.

Секреты для тестов (хосты БД, Gatezilla, gRPC, Temporal и т.д.) подтягиваются из Vault автоматически через `TestsConfigReader`.

### Запуск из терминала

```powershell
.\gradlew.bat test
```

Для bash:

```bash
./gradlew test
```

## Обновление контракта Gatezilla

Клиент для REST API Gatezilla генерируется из OpenAPI-спецификации.

При обновлении контракта:

1. Положите новый файл спецификации в `src/test/resources/` (замените `src/test/resources/gatezilla.swagger.yaml` или положите файл с тем же именем).
2. Перегенерируйте Java-классы:

```powershell
.\gradlew.bat openApiGenerate
```

Или выполните полную сборку — задача `openApiGenerate` запускается автоматически перед компиляцией тестов:

```powershell
.\gradlew.bat compileTestJava
```

Сгенерированный код попадает в `build/generated/openapi/` и не коммитится в репозиторий.

## Обновление контракта gRPC

Клиенты для gRPC-сервисов (Genry, Hardcore и др.) генерируются из `.proto`-файлов.

При обновлении контракта gRPC-методов:

1. Положите новые или обновлённые `.proto`-файлы в папку `src/test/proto/` (например, замените `genry.proto`, `hardcore.proto` или добавьте файлы с сохранением структуры каталогов).
2. Перегенерируйте Java-классы:

```powershell
.\gradlew.bat generateTestProto
```

Или выполните сборку тестов — генерация запускается автоматически:

```powershell
.\gradlew.bat compileTestJava
```

Сгенерированный код попадает в `build/generated/sources/proto/` и не коммитится в репозиторий.
