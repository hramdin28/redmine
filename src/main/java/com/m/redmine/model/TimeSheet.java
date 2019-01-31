package com.m.redmine.model;

import com.opencsv.bean.CsvBindByName;

public class TimeSheet {

  @CsvBindByName(column = "Tags", required = true)
  private String issue;

  @CsvBindByName(column = "Description", required = true)
  private String comment;

  @CsvBindByName(column = "Duration", required = true)
  private String hour;

  @CsvBindByName(column = "Project", required = true)
  private String activity;

  @CsvBindByName(column = "Start Date", required = true)
  private String date;

  public String getIssue() {
    return issue;
  }

  public void setIssue(String issue) {
    this.issue = issue;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getHour() {
    return hour;
  }

  public void setHour(String hour) {
    this.hour = hour;
  }

  public String getActivity() {
    return activity;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }



}
