## Grabber

Проект представляет собой автоматический парсер вакансий Java Developer с сайта career.habr.com, созданный для личного пользования.<br>
Сохранение полученных вакансий производится в локальную базу данных.<br>
Проект был создан на ранних стадиях обучения.

### Требования к окружению:
* Java 17,
* PostgreSQL 14.0,
* Apache Maven 3.8.4

### Используемые технологии:
* Java 17
* Maven 3.8
* PostgreSQL 14 (driver v.42.5.0)
* Quartz 2.3.2
* Jsoup 1.15.3
* Checkstyle 3.1.2
* Log4J 1.2.17
* Slf4j 1.7.36

### Запуск проекта:
```
1. Для запуска проекта, Вам необходимо клонировать проект из этого репозитория;
2. Затем необходимо создать локальную базу данных и таблицу согласно скриптам из папки sql;
3. После - пропишите, пожалуйста, логин и пароль к созданной вами базе данных в ресурсный файл 
habrCareerGrabber.properties;
4. Запустите приложение через класс HabrCareerParse, находящийся в папке src\main\java\ru\job4j\grabber;
5. Вакансии по специальности Java Developer, размещенные на Habr Career, с первых пяти страниц с вакансиями, будут записаны в вашу базу данных. 
```

### Контакты для связи:
> <a href="https://github.com/Niaktes/">Захаренко Сергей</a> <br>
> Телефон: +7 995 299 07 34 <br>
<a href="https://t.me/SZakharenko"><img src="https://seeklogo.com/images/T/telegram-logo-AD3D08A014-seeklogo.com.png" alt="Telegram" height="30"></a>
<a href="https://wa.me/89265900734"><img src="https://seeklogo.com/images/W/whatsapp-icon-logo-6E793ACECD-seeklogo.com.png" alt="Whatsapp" height="30"></a>
<a href="mailto:Sergei.Zakharenko.it@gmail.com"><img src="https://seeklogo.com/images/G/gmail-logo-0B5D69FF48-seeklogo.com.png" alt="Mail" height="30"></a>
