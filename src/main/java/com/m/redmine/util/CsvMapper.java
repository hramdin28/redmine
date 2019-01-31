package com.m.redmine.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.m.redmine.model.TimeSheet;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

public class CsvMapper {



  public static List<TimeSheet> getCsv(String csvPath) throws IOException {
    try (Reader reader = Files.newBufferedReader(Paths.get(csvPath));) {
      CsvToBean<TimeSheet> csvToBean = new CsvToBeanBuilder<TimeSheet>(reader)
          .withType(TimeSheet.class).withIgnoreLeadingWhiteSpace(true).build();

      Iterator<TimeSheet> timeSheetIterator = csvToBean.iterator();

      List<TimeSheet> timesheetList = new ArrayList<>();

      timeSheetIterator.forEachRemaining(timesheetList::add);

      return timesheetList;

    }
  }

}
