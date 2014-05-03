/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation, and proper error handling, might not be present in
 * this sample code.
 */

package bugdates;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;
import static java.util.Comparator.*;

/**
 * date format: 17 Feb 2014
 *
 */
public class BugDates {
    
    // <created>Thu, 20 Feb 2014 02:13:36 -0800</created>
    // <resolved>Thu, 13 Feb 2014 14:52:51 -0800</resolved>
    
	/**
	 * Loads the given file and returns a map of dates to the number
	 * of occurrences of that date.
	 */
    static Map<LocalDate, Long> loadFile(String fn, String patstr) throws IOException {
        Pattern pattern = Pattern.compile(patstr);
        try (BufferedReader br = Files.newBufferedReader(Paths.get(fn))) {
            return
                br.lines()
                  .map(line -> pattern.matcher(line))
                  .filter(matcher -> matcher.find())
                  .map(matcher -> matcher.group(1))
                  .map(s -> LocalDate.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME))
                  .collect(groupingBy(d -> d, counting()));
        }
    }

    /**
     * Scans the file to get the number of creations and the number
     * of resolutions on each date. Then run through each date of the
     * date range and call the QuadConsumer with each date, passing
     * the cumulative number of bugs created, resolved, and net open
     * bugs on that day.
     */
    public static void generate(QuadConsumer<LocalDate, Long, Long, Long> qc) throws IOException {
        Map<LocalDate, Long> creations   = loadFile("open-teststabilization.xml", "<created>(.*)</created>");
        Map<LocalDate, Long> resolutions = loadFile("open-teststabilization.xml", "<resolved>(.*)</resolved>");
        
        LocalDate firstDate =
            Stream.concat(creations.keySet().stream(),
                          resolutions.keySet().stream())
                  .min(naturalOrder())
                  .get();
        LocalDate lastDate = LocalDate.parse("2014-02-22"); // bug data was queried on this day
        long ndays = lastDate.toEpochDay() - firstDate.toEpochDay();
        System.out.printf("Scanning %s to %s (%d days)%n", firstDate, lastDate, ndays);
        System.out.printf("Number of creations = %d%n", creations.values().stream().mapToLong(Long::longValue).sum());
        System.out.printf("Number of resolutions = %d%n", resolutions.values().stream().mapToLong(Long::longValue).sum());

        long ncre = 0L;
        long nres = 0L;
        long nnet = 0L;
        
        for (long i = 0L; i < ndays; i++) {
            LocalDate curdate = firstDate.plusDays(i);
            long nc = creations.getOrDefault(curdate, 0L);
            long nr = resolutions.getOrDefault(curdate, 0L);
            ncre += nc;
            nres += nr;
            nnet += nc - nr;

            qc.accept(curdate, ncre, nres, nnet);
        }
    }
}
