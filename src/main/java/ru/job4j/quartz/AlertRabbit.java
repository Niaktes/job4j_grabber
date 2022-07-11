package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static void main(String[] args) throws FileNotFoundException {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(readIntervalFromResource("rabbit.properties"))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }


    private static int readIntervalFromResource(String fileName) throws FileNotFoundException {
        Properties properties = new Properties();
        int interval;
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream(fileName)) {
            properties.load(in);
            propertiesValidation(properties);
            interval = Integer.parseInt(properties.getProperty("rabbit.interval"));
        } catch (IOException e) {
            throw new FileNotFoundException("Where is \"rabbit.properties\"?");
        }
        return interval;
    }

    private static void propertiesValidation(Properties properties) {
        if (properties.size() != 1) {
            throw new IllegalArgumentException("Something wrong with \"rabbit.properties\" file");
        }
        if (Integer.parseInt(properties.getProperty("rabbit.interval")) < 1) {
            throw new IllegalArgumentException("Wrong timer value");
        }
    }


    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here...");
        }
    }

}
