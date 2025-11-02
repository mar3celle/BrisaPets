package com.brisapets.webapp.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {
    
    public List<Integer> calculateCalendarDays(YearMonth yearMonth) {
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();
        
        int dayOfWeekForCalendar = firstDayOfWeekValue % 7;
        if (dayOfWeekForCalendar == 0) {
            dayOfWeekForCalendar = 7;
        }
        
        int padding = (dayOfWeekForCalendar == 7) ? 0 : dayOfWeekForCalendar;
        
        List<Integer> days = new ArrayList<>();
        
        for (int i = 0; i < padding; i++) {
            days.add(null);
        }
        
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }
        
        return days;
    }
}