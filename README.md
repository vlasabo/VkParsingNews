Реализация телеграм-бота на Java, использующая API телеграм и ВКонтакте для отслеживания новостей по заданным пользователем ключевым словам 
и передающая новость + ссылку на неё в телеграм.
Для хранения пользователей, слов и уже отправленных новостей спользован PostgreSQL. 
Есть ветка add-docker, позволяющая развернуть проект в докере.
Бот был запущен на мощностях heroku, из-за сложностей с оплатой перенесён на сервер yandex cloud.

файл application.properties исключен из репозитория. Для локального запуска он должен содержать следующие строки (? - ваше значение):

#telegram settings

bot.name=?

bot.token=?

#db settings

spring.sql.init.mode=always

spring.datasource.url=jdbc:postgresql://localhost:5432/?

spring.datasource.driverClassName=org.postgresql.Driver

spring.jpa.database=postgresql

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

hibernate.hbm2ddl.auto=update

spring.datasource.username=?

spring.datasource.password=?

spring.jpa.defer-datasource-initialization=true

#VK settings

app.code=?

app.id=?
