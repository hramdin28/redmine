package com.m.redmine.enumeration;

public enum Activites {
  CODE_REVIEW(19), CONCEPTION(8), DEVELOPMENT(9), TESTING(10), TECHNICAL_SPECIFICATION(
      12), FUNCTIONAL_SPECIFICATION(12), SUPPORT(13), TRAINING(14), PROJECT_MANAGEMENT(
          15), SYSTEM_ADMINISTRATION(16), LEAVES(17), MEETING(18), UPDATER(40), CONF_CAL(60);

  private int id;

  Activites(int id) {
    this.id = id;
  }

  public int getID() {
    return id;
  }
}
