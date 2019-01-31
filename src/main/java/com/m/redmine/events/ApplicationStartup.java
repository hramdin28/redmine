package com.m.redmine.events;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import com.m.redmine.enumeration.Activites;
import com.m.redmine.util.CsvMapper;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import com.taskadapter.redmineapi.internal.ResultsWrapper;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

  Logger logger = LoggerFactory.getLogger(ApplicationStartup.class);

  @Value("${redmine.url}")
  String uri;
  @Value("${redmine.key}")
  String apiAccessKey;

  @Value("${redmine.userId}")
  String userId;

  @Value("${redmine.csv.file}")
  String csvFilePath;



  @Override
  public void onApplicationEvent(final ApplicationReadyEvent event) {

    createTimeEntriesFromCSV(csvFilePath);
  }

  @SuppressWarnings("unused")
  private void createTimeEntriesFromCSV(String csvPath) {
    try {
      CsvMapper.getCsv(csvFilePath).forEach(timeSheet -> {

        Integer activityId = Activites.valueOf(timeSheet.getActivity()).getID();
        String comment = timeSheet.getComment();
        String date = timeSheet.getDate();
        Integer issueId =
            Integer.parseInt(timeSheet.getIssue().replaceAll("[^\\d.]+|\\.(?!\\d)", ""));

        Float hour = Float.parseFloat(timeSheet.getHour().split(":")[0]);

        Float minutes = Float.parseFloat(timeSheet.getHour().split(":")[1]) / 60;

        Float duration = hour + minutes;


        createOneTimeEntry(null, issueId, duration, comment, activityId, date);

      });


    } catch (IOException e) {
      logger.error("", e);
    }
  }

  @SuppressWarnings("unused")
  private void findAllMyIssues() {

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);
    mgr.setObjectsPerPage(100);

    Map<String, String> parameters = new HashMap<>();
    parameters.put("assigned_to_id", userId);
    parameters.put("status_id", "open");
    parameters.put("limit", "100");
    parameters.put("done_ratio", "<=99");

    ResultsWrapper<Issue> issues;
    try {

      issues = mgr.getIssueManager().getIssues(parameters);
      for (Issue issue : issues.getResults()) {

        logger.info(issue.getCreatedOn() + " " + issue.getUpdatedOn() + " " + issue.toString()
            + " ratio=" + issue.getDoneRatio() + " status=" + issue.getStatusName() + " status_id="
            + issue.getStatusId());


        if (issue.getStatusId() == 8) {
          issue.setDoneRatio(100);
          issue.setStatusId(8);
          mgr.getIssueManager().update(issue);
        }


      }


    } catch (RedmineException e) {
      e.printStackTrace();
    }

  }



  @SuppressWarnings("unused")
  private void createOneTimeEntry(Integer projectId, Integer issueId, Float hours, String comment,
      Integer activityId, String date) {

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);

    TimeEntry timeEntry = TimeEntryFactory.create();

    timeEntry.setProjectId(projectId);
    timeEntry.setIssueId(issueId);
    timeEntry.setHours(hours);
    timeEntry.setComment(comment);
    timeEntry.setActivityId(activityId);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    LocalDate localSpentOn = LocalDate.parse(date, formatter);

    Date spentOn = Date.from(localSpentOn.atStartOfDay(ZoneId.systemDefault()).toInstant());

    timeEntry.setSpentOn(spentOn);

    logger.info("Spent On date: ", spentOn);


    try {
      TimeEntry te = mgr.getTimeEntryManager().createTimeEntry(timeEntry);

      logger.info("{}", te);

    } catch (RedmineException e) {

      logger.error("createOneTimeEntry: ", e);
    }

  }

  @SuppressWarnings("unused")
  private void createRangeDateTimeEntries(String dateStart, String dateEnd, Integer projectId,
      Integer issueId, Float hours, String comment, Integer activityId) {

    logger.info("CREATING:");

    LocalDate start = LocalDate.parse(dateStart);
    LocalDate end = LocalDate.parse(dateEnd);

    while (!start.isAfter(end)) {

      if (start.getDayOfWeek() != DayOfWeek.SATURDAY && start.getDayOfWeek() != DayOfWeek.SUNDAY) {

        createOneTimeEntry(projectId, issueId, hours, comment, activityId, start.toString());

      }

      start = start.plusDays(1);
    }

  }

  @SuppressWarnings("unused")
  private void deleteTimeEntriesByDates(String dateStart, String dateEnd, Integer projectId,
      Integer issueId) {

    logger.info("DELETING:");

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);


    Map<String, String> parameters = new HashMap<>();

    if (projectId != null) {
      parameters.put("project_id", projectId.toString());
    }

    if (issueId != null) {
      parameters.put("issue_id", issueId.toString());
    }

    parameters.put("from", dateStart);
    parameters.put("to", dateEnd);
    parameters.put("user_id", userId);

    try {
      ResultsWrapper<TimeEntry> rw = mgr.getTimeEntryManager().getTimeEntries(parameters);

      rw.getResults().stream().forEach(t -> {
        try {
          mgr.getTimeEntryManager().deleteTimeEntry(t.getId());
          logger.info("{}", t);

        } catch (RedmineException e) {
          logger.error("deleteTimeEntriesByDates: ", e);
        }
      });


    } catch (RedmineException e) {
      logger.error("deleteTimeEntriesByDates: ", e);
    }

  }


}
