/**
 * 
 */
package com.tunein.tvschedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Everton Rosario (erosario@gmail.com)
 */
public class TVSchedule {
    
    
    private static final String CONFLICTED_PERIODS = "CONFLICTED_PERIODS";

    /**
     * Runs entire week splitting between programs for further grouping and checking for the conflicts 
     * @param periods The periods in the week
     * @return A map for each TV Program and if any conflict happens, a key CONFLICTED_PERIODS is returned
     */
    public static Map<String, List<TVTimePeriod>> analyseAndSplitTVPrograms(TreeSet<TVTimePeriod> orderedPeriods) {
        
        Map<String, List<TVTimePeriod>> result = new HashMap<String, List<TVTimePeriod>>();
        
        TVTimePeriod previous = null;
        for (TVTimePeriod period : orderedPeriods) {
            
            if(period.hasConflict(previous)) {
                List<TVTimePeriod> conflicts = result.get(CONFLICTED_PERIODS);
                if (conflicts == null) {
                    conflicts = new ArrayList<TVTimePeriod>(); 
                }
                conflicts.add(period);
                conflicts.add(previous);
            }
            
            List<TVTimePeriod> programEntries = result.get(period.getShortName());
            if (programEntries == null) {
                programEntries = new ArrayList<TVTimePeriod>();
            }
            programEntries.add(period);
            result.put(period.getShortName(), programEntries);
        }
        
        
        return result;
    }

    
    
    public static List<TVTimePeriod> optmize(List<TVTimePeriod> periods) {
        
        TreeSet<TVTimePeriod> orderedPeriods = new TreeSet<TVTimePeriod>(periods);
        Map<String, List<TVTimePeriod>> tvPrograms = analyseAndSplitTVPrograms(orderedPeriods);
        
        // Stop process, no conflict is expected
        if (tvPrograms.containsKey(CONFLICTED_PERIODS)) {
            // DO SOMETHING
            // DONT DO THE OPTMIZATION
            return new ArrayList<TVTimePeriod>();
        }
        
        
        // Starts recursion
        return new ArrayList<TVTimePeriod>(optmizationProcess(orderedPeriods));
        
    }



    private static TreeSet<TVTimePeriod> optmizationProcess(TreeSet<TVTimePeriod> orderedPeriods) {
        
        if (orderedPeriods == null || orderedPeriods.size() <= 1) {
            return orderedPeriods;
        }
        
        // Horizontal grouping (Program thru week days)
        Map<String, TVTimePeriod> groupStartDuration = new HashMap<String, TVTimePeriod>();
        
        // Vertical grouping (Contiguous show entries)
        List<TVTimePeriod> groupContiguous = new ArrayList<TVTimePeriod>();
        
        TVTimePeriod previousPeriod = null;
        for (TVTimePeriod period : orderedPeriods) {
            TVTimePeriod group = groupStartDuration.get(period.getGroupingKey());
            if (group == null) {
                group = period;
            } else {
                group.group(period);
            }

            if (period.isContiguous(previousPeriod)) {
                previousPeriod = previousPeriod.group(period);
            } else {
                if (previousPeriod != null) {
                    groupContiguous.add(previousPeriod);
                }
                previousPeriod = period;
            }
        }
        // Remaining period of loop
        if (previousPeriod != null) {
            groupContiguous.add(previousPeriod);
        }
        
        
        // Checks the biggest groups formed
        TVTimePeriod biggestWeekdays = getBiggestGroup(groupStartDuration.values());
        TVTimePeriod biggestContiguous = getBiggestGroup(groupContiguous);
        
        TVTimePeriod biggest = getBiggestGroup(Arrays.asList(biggestWeekdays, biggestContiguous));
        
        // No groups formed, end of recursion
        if (biggest == null || biggest.size() == 0) {
            return orderedPeriods;
        }
        
        TreeSet<TVTimePeriod> remainingPeriods = removeRelatedPeriods(biggest, orderedPeriods);
        remainingPeriods = optmizationProcess(remainingPeriods);
        remainingPeriods.add(biggest);
        return remainingPeriods;
    }



    private static TreeSet<TVTimePeriod> removeRelatedPeriods(TVTimePeriod biggest, TreeSet<TVTimePeriod> orderedPeriods) {
        TreeSet<TVTimePeriod> nonConflicted = new TreeSet<TVTimePeriod>();
        
        for (TVTimePeriod period : orderedPeriods) {
            if(!period.hasConflict(biggest)) {
                nonConflicted.add(period);
            }
        }
        
        return nonConflicted;
    }



    private static TVTimePeriod getBiggestGroup(Collection<TVTimePeriod> values) {
        TVTimePeriod biggest = null;
        
        for (TVTimePeriod period : values) {
            if (period != null && (biggest == null || biggest.size() < period.size())) {
                biggest = period;
            }
        }

        return biggest;
    }

}
