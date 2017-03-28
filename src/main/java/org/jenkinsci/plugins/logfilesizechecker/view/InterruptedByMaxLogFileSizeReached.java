package org.jenkinsci.plugins.logfilesizechecker.view;

import com.google.common.base.Predicate;
import hudson.model.Run;
import jenkins.model.CauseOfInterruption;
import jenkins.model.InterruptedBuildAction;
import org.jenkinsci.plugins.logfilesizechecker.LogFileSizeCheckerWorker;

import javax.annotation.Nullable;

public class InterruptedByMaxLogFileSizeReached implements Predicate<Run> {

    @Override
    public boolean apply(@Nullable Run run) {
        if (run != null) {
            InterruptedBuildAction action = run.getAction(InterruptedBuildAction.class);
            if (action != null) {
                for (CauseOfInterruption cause : action.getCauses()) {
                    if (cause instanceof LogFileSizeCheckerWorker.MaxLogFileSizeReached) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
