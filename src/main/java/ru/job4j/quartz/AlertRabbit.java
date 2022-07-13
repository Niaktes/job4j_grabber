package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.*;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
        Properties cfg = readPropertiesFromFile(("rabbit.properties"));
        Class.forName(cfg.getProperty("rabbit.driver"));
        try (Connection connection = DriverManager.getConnection(
                cfg.getProperty("rabbit.url"),
                cfg.getProperty("rabbit.login"),
                cfg.getProperty("rabbit.password")
        )){
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    private static Properties readPropertiesFromFile(String fileName) throws FileNotFoundException {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream(fileName)) {
            properties.load(in);
            propertiesValidation(properties);
        } catch (IOException e) {
            throw new FileNotFoundException("Where is \"rabbit.properties\"?");
        }
        return properties;
    }

    private static void propertiesValidation(Properties properties) {
        if (properties.size() != 5) {
            throw new IllegalArgumentException("Something wrong with \"rabbit.properties\" file.");
        }
        if (Integer.parseInt(properties.getProperty("rabbit.interval")) < 1) {
            throw new IllegalArgumentException("Wrong timer value.");
        }
        if (properties.getProperty("rabbit.driver").isBlank()){
            throw new IllegalArgumentException("No driver in property file.");
        }
        if (properties.getProperty("rabbit.url").isBlank()){
            throw new IllegalArgumentException("Wrong database URL.");
        }
        if (properties.getProperty("rabbit.login").isBlank() || properties.getProperty("rabbit.password").isBlank()){
            throw new IllegalArgumentException("Database login or password incorrect");
        }
    }


    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO rabbit (created_date) VALUES (?);")) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
