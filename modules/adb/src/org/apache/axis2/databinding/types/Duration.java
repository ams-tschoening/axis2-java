/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.databinding.types;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Implementation of the XML Schema type duration. Duration supports a minimum fractional second
 * precision of milliseconds.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#duration">XML Schema 3.2.6</a>
 */
public class Duration implements Serializable {

    private static final long serialVersionUID = -3736760992541369098L;

    boolean isNegative;
    int years;
    int months;
    int days;
    int hours;
    int minutes;
    double seconds;

    /** Default no-arg constructor */
    public Duration() {
    }

    /**
     * Construct a new Duration.
     * 
     * @param negative Negative or positive duration?
     * @param aYears How many years the duration spans.
     * @param aMonths How many months the duration spans.
     * @param aDays How many days the duration spans.
     * @param aHours How many hours the duration spans.
     * @param aMinutes How many minutes the duration spans.
     * @param aSeconds How many seconds the duration spans.
     */
    public Duration(boolean negative, int aYears, int aMonths, int aDays,
                    int aHours, int aMinutes, double aSeconds) {
        isNegative = negative;
        years = aYears;
        months = aMonths;
        days = aDays;
        hours = aHours;
        minutes = aMinutes;
        setSeconds(aSeconds);
    }

    /**
     * Constructs Duration from a String in an xsd:duration format.
     *
     * @param duration Duration to parse in the format "PnYnMnDTnHnMnS".
     * @throws IllegalArgumentException if the string doesn't parse correctly.
     */
    public Duration(String duration) throws IllegalArgumentException {
        int position = 1;
        int timePosition = duration.indexOf("T");

        // P is required but P by itself is invalid
        if (duration.indexOf("P") == -1 || duration.equals("P")) {
            throw new IllegalArgumentException();
            // Messages.getMessage("badDuration"));
        }

        // if present, time cannot be empty
        if (duration.lastIndexOf("T") == duration.length() - 1) {
            throw new IllegalArgumentException();
//                    Messages.getMessage("badDuration"));
        }

        // check the sign
        if (duration.startsWith("-")) {
            isNegative = true;
            position++;
        }

        // parse time part
        if (timePosition != -1) {
            parseTime(duration.substring(timePosition + 1));
        } else {
            timePosition = duration.length();
        }

        // parse date part
        if (position != timePosition) {
            parseDate(duration.substring(position, timePosition));
        }
    }

    /**
     * Constructs Duration from a Calendar.
     *
     * @param negative Negative or positive duration?
     * @param calendar Calendar to get the individual values for the duration from.
     * @throws IllegalArgumentException if the calendar object does not represent any date nor
     *                                  time.
     */
    public Duration(boolean negative, Calendar calendar) throws
            IllegalArgumentException {
        this.isNegative = negative;
        this.years = calendar.get(Calendar.YEAR);
        this.months = calendar.get(Calendar.MONTH);
        this.days = calendar.get(Calendar.DATE);
        this.hours = calendar.get(Calendar.HOUR);
        this.minutes = calendar.get(Calendar.MINUTE);
        this.seconds = calendar.get(Calendar.SECOND);
        this.seconds += ((double)calendar.get(Calendar.MILLISECOND)) / 100;
        if (years == 0 && months == 0 && days == 0 && hours == 0 &&
                minutes == 0 && seconds == 0) {
            throw new IllegalArgumentException();
            //Messages.getMessage("badCalendarForDuration"));
        }
    }

    /**
     * This method parses the time portion of a String that represents xsd:duration.
     *
     * @param time Duration to parse the time from in the format "nHnMnS". 
     * @throws IllegalArgumentException if time does not match pattern.
     */
    public void parseTime(String time) throws IllegalArgumentException {
        if (time.length() == 0 || time.indexOf("-") != -1) {
            throw new IllegalArgumentException();
            //Messages.getMessage("badTimeDuration"));
        }

        // check if time ends with either H, M, or S
        if (!time.endsWith("H") && !time.endsWith("M") && !time.endsWith("S")) {
            throw new IllegalArgumentException();
            //Messages.getMessage("badTimeDuration"));
        }

        try {
            // parse string and extract hours, minutes, and seconds
            int start = 0;

            // Hours
            int end = time.indexOf("H");
            // if there is H in a string but there is no value for hours,
            // throw an exception
            if (start == end) {
                throw new IllegalArgumentException();
                //Messages.getMessage("badTimeDuration"));
            }
            if (end != -1) {
                hours = Integer.parseInt(time.substring(0, end));
                start = end + 1;
            }

            // Minutes
            end = time.indexOf("M");
            // if there is M in a string but there is no value for hours,
            // throw an exception
            if (start == end) {
                throw new IllegalArgumentException();
//                        Messages.getMessage("badTimeDuration"));
            }

            if (end != -1) {
                minutes = Integer.parseInt(time.substring(start, end));
                start = end + 1;
            }

            // Seconds
            end = time.indexOf("S");
            // if there is S in a string but there is no value for hours,
            // throw an exception
            if (start == end) {
                throw new IllegalArgumentException();
                //Messages.getMessage("badTimeDuration"));
            }

            if (end != -1) {
                setSeconds(Double.parseDouble(time.substring(start, end)));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
            //Messages.getMessage("badTimeDuration"));
        }
    }

    /**
     * This method parses the date portion of a String that represents xsd:duration.
     *
     * @param date Duration to parse the date from in the format "nYnMnD".
     * @throws IllegalArgumentException if date does not match pattern.
     */
    public void parseDate(String date) throws IllegalArgumentException {
        if (date.length() == 0 || date.indexOf("-") != -1) {
            throw new IllegalArgumentException();
            //Messages.getMessage("badDateDuration"));
        }

        // check if date string ends with either Y, M, or D
        if (!date.endsWith("Y") && !date.endsWith("M") && !date.endsWith("D")) {
            throw new IllegalArgumentException();
            //Messages.getMessage("badDateDuration"));
        }

        // catch any parsing exception
        try {
            // parse string and extract years, months, days
            int start = 0;
            int end = date.indexOf("Y");

            // if there is Y in a string but there is no value for years,
            // throw an exception
            if (start == end) {
                throw new IllegalArgumentException();
                // Messages.getMessage("badDateDuration"));
            }
            if (end != -1) {
                years = Integer.parseInt(date.substring(0, end));
                start = end + 1;
            }

            // months
            end = date.indexOf("M");
            // if there is M in a string but there is no value for months,
            // throw an exception
            if (start == end) {
                throw new IllegalArgumentException();
                //Messages.getMessage("badDateDuration"));
            }
            if (end != -1) {
                months = Integer.parseInt(date.substring(start, end));
                start = end + 1;
            }

            end = date.indexOf("D");
            // if there is D in a string but there is no value for days,
            // throw an exception
            if (start == end) {
                throw new IllegalArgumentException();
                // Messages.getMessage("badDateDuration"));
            }
            if (end != -1) {
                days = Integer.parseInt(date.substring(start, end));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
            //Messages.getMessage("badDateDuration"));
        }
    }

    /**
     * Is this duration negative?
     * 
     * @return {@code true} if duration is negative, {@code false} else.
     */
    public boolean isNegative() {
        return isNegative;
    }

    /**
     * Gets the number of years this duration spans.
     * 
     * @return Number of years.
     */
    public int getYears() {
        return years;
    }

    /**
     * Gets the number of months this duration spans.
     * 
     * @return Number of months.
     */
    public int getMonths() {
        return months;
    }

    /**
     * Gets the number of days this duration spans.
     * 
     * @return Number of days.
     */
    public int getDays() {
        return days;
    }

    /**
     * Gets the number of hours this duration spans.
     * 
     * @return Number of hours.
     */
    public int getHours() {
        return hours;
    }

    /**
     * Gets the number of minutes this duration spans.
     * 
     * @return Number of minutes.
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Gets the number of seconds this duration spans.
     * 
     * @return Number of seconds.
     */
    public double getSeconds() {
        return seconds;
    }

    /**
     * Should this duration be negative?
     *
     * @param negative {@code true} if the duration is negative, {@code false} otherwise.
     */
    public void setNegative(boolean negative) {
        isNegative = negative;
    }

    /**
     * Sets the number of years this duration spans.
     *
     * @param years Number of years.
     */
    public void setYears(int years) {
        this.years = years;
    }

    /**
     * Sets the number of months this duration spans.
     *
     * @param months Number of months.
     */
    public void setMonths(int months) {
        this.months = months;
    }

    /**
     * Sets the number of days this duration spans.
     *
     * @param days Number of days.
     */
    public void setDays(int days) {
        this.days = days;
    }

    /**
     * Sets the number of hours this duration spans.
     *
     * @param hours Number of hours.
     */
    public void setHours(int hours) {
        this.hours = hours;
    }

    /**
     * Sets the number of minutes this duration spans.
     *
     * @param minutes Number of minutes.
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    /**
     * Sets the number of seconds this duration spans.
     *
     * @param seconds Number of seconds.
     * @deprecated Use {@link #setSeconds(double)} instead.
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    /**
     * Sets the number of seconds this duration spans.
     * NOTE: The fractional value of seconds is rounded up to milliseconds.
     *
     * @param seconds Number of seconds.
     */
    public void setSeconds(double seconds) {
        this.seconds = ((double)(Math.round(seconds * 100))) / 100;
    }

    /**
     * This returns the XML representation of an xsd:duration object.
     * 
     * @return XML representation
     */
    public String toString() {
        StringBuffer duration = new StringBuffer();

        duration.append("P");

        if (years != 0) {
            duration.append(years).append("Y");
        }
        if (months != 0) {
            duration.append(months).append("M");
        }
        if (days != 0) {
            duration.append(days).append("D");
        }
        if (hours != 0 || minutes != 0 || seconds != 0.0) {
            duration.append("T");

            if (hours != 0) {
                duration.append(hours).append("H");

            }
            if (minutes != 0) {
                duration.append(minutes).append("M");

            }
            if (seconds != 0) {
                if (seconds == (int)seconds) {
                    duration.append((int)seconds).append("S");
                } else {
                    duration.append(seconds).append("S");
                }
            }
        }

        if (duration.length() == 1) {
            duration.append("T0S");
        }

        if (isNegative) {
            duration.insert(0, "-");
        }

        return duration.toString();
    }

    /**
     * The equals method compares the time represented by duration object, not its string
     * representation. Hence, a duration object representing 65 minutes is considered equal to a
     * duration object representing 1 hour and 5 minutes.
     *
     * @param object Other Duration to compare with.
     * @return {@code true} if the other object is a Duration and holds the same time span.
     */
    public boolean equals(Object object) {
        if (!(object instanceof Duration)) {
            return false;
        }

        Duration duration = (Duration)object;

        return this.isNegative == duration.isNegative &&
                this.getAsCalendar().equals(duration.getAsCalendar());
    }

    public long compare(Duration duration) {
        return this.getAsCalendar().getTimeInMillis() - duration.getAsCalendar().getTimeInMillis();
    }

    public int hashCode() {
        int hashCode = 0;

        if (isNegative) {
            hashCode++;
        }
        hashCode += years;
        hashCode += months;
        hashCode += days;
        hashCode += hours;
        hashCode += minutes;
        hashCode += seconds;
        // milliseconds
        hashCode += (seconds * 100) % 100;

        return hashCode;
    }

    /**
     * Returns duration as a calendar.  Due to the way a Calendar class works, the values for
     * particular fields may not be the same as obtained through getter methods.  For example, if a
     * duration's object getMonths returns 20, a similar call on a calendar object will return 1
     * year and 8 months.
     *
     * @return Calendar
     */
    public Calendar getAsCalendar() {
        return getAsCalendar(Calendar.getInstance());
    }

    /**
     * Returns duration as a calendar.  Due to the way a Calendar class works, the values for
     * particular fields may not be the same as obtained through getter methods.  For example, if a
     * Duration's object getMonths returns 20, a similar call on a Calendar object will return 1
     * year and 8 months.
     *
     * @param startTime Calendar with default data.
     * @return Calendar
     */
    public Calendar getAsCalendar(Calendar startTime) {
        Calendar ret = (Calendar)startTime.clone();
        ret.set(Calendar.YEAR, years);
        ret.set(Calendar.MONTH, months);
        ret.set(Calendar.DATE, days);
        ret.set(Calendar.HOUR, hours);
        ret.set(Calendar.MINUTE, minutes);
        ret.set(Calendar.SECOND, (int)seconds);
        ret.set(Calendar.MILLISECOND,
                (int)(seconds * 100 - Math.round(seconds) * 100));
        return ret;
    }
}
