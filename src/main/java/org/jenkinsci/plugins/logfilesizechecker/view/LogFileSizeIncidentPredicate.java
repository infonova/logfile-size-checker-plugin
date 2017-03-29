package org.jenkinsci.plugins.logfilesizechecker.view;

import com.google.common.base.Predicate;
import hudson.model.Run;
import jenkins.model.CauseOfInterruption;
import jenkins.model.InterruptedBuildAction;
import org.jenkinsci.plugins.logfilesizechecker.actions.TruncateLogAction;
import org.jenkinsci.plugins.logfilesizechecker.executors.TerminateExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogFileSizeIncidentPredicate implements Predicate<Run> {

    static boolean wasTerminated(@Nonnull Run run) {
        InterruptedBuildAction action = run.getAction(InterruptedBuildAction.class);

        if (action != null) {
            for (CauseOfInterruption cause : action.getCauses()) {
                if (cause instanceof TerminateExecutor.LogFileSizeInterruption) {
                    return true;
                }
            }
        }

        return false;
    }

    static boolean wasTruncated(@Nonnull Run run) {
        return run.getAction(TruncateLogAction.class) != null;
    }

    @Override
    public boolean apply(@Nullable Run run) {
        return run != null && (wasTerminated(run) || wasTruncated(run));
    }
}
