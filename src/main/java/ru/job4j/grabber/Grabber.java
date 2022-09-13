package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private final Properties cfg = new Properties();

    public Store store() {
        Store store = null;
        try {
            store = new PsqlStore(cfg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return store;
    }

    public Parse parser() {
        return new HabrCareerParse(new HabrCareerDateTimeParser());
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg(String fileName) throws FileNotFoundException {
        try (InputStream in = Grab.class.getClassLoader().getResourceAsStream(fileName)) {
            cfg.load(in);
            propertiesValidation(cfg);
        } catch (IOException e) {
            throw new FileNotFoundException("Cannot find property file. Please, check file name.");
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("parse", parse);
        data.put("store", store);
        data.put("link", cfg.getProperty("parser.link"));
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInHours(Integer.parseInt(cfg.getProperty("scheduler.interval")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }


    private void propertiesValidation(Properties property) {
        final String checkFileText = " Please, check property file.";
        if (property.size() != 6) {
            throw new IllegalArgumentException("Wrong arguments number." + checkFileText);
        }
        if (property.getProperty("store.driver").isBlank()) {
            throw new IllegalArgumentException("No database driver." + checkFileText);
        }
        if (property.getProperty("store.url").isBlank()) {
            throw new IllegalArgumentException("Can't read database URL." + checkFileText);
        }
        if (property.getProperty("store.login").isBlank() || property.getProperty("store.password").isBlank()) {
            throw new IllegalArgumentException("Database login or password incorrect." + checkFileText);
        }
        if (property.getProperty("parser.link").isBlank()) {
            throw new IllegalArgumentException("No link to parse." + checkFileText);
        }
    }


    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context)  {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            Parse parse = (Parse) data.get("parse");
            Store store = (Store) data.get("store");
            String link = (String) data.get("link");
            List<Post> posts = null;
            try {
                posts = parse.list(link);
            } catch (IOException e) {
                e.printStackTrace();
            }
            posts.forEach(store::save);
        }
    }


    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        grab.cfg("habrCareerGrabber.properties");
        grab.init(grab.parser(), grab.store(), grab.scheduler());
    }
}
