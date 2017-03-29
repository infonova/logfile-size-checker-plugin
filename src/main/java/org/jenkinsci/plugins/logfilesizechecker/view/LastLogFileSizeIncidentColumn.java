package org.jenkinsci.plugins.logfilesizechecker.view;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.logfilesizechecker.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;


public class LastLogFileSizeIncidentColumn extends ListViewColumn {

    @DataBoundConstructor
    public LastLogFileSizeIncidentColumn() {
        // do not remove, otherwise Jenkins cannot create instance
    }

    @SuppressWarnings("unused")
    public Run getLastLogFileSizeIncident(Job job) {
        return job.getBuilds().filter(new LogFileSizeIncidentPredicate()).getLastBuild();
    }

    public String getIncidentType(@Nonnull Run run) {
        if (LogFileSizeIncidentPredicate.wasTerminated(run)) {
            return Messages.LastLogFileSizeIncidentColumn_terminateIncident();
        }

        if (LogFileSizeIncidentPredicate.wasTruncated(run)) {
            return Messages.LastLogFileSizeIncidentColumn_truncateIncident();
        }

        return Messages.LastLogFileSizeIncidentColumn_unspecifiedIncident();
    }

    @Extension
    @Symbol("lastLogFileSizeIncidentColumn")
    public static class DescriptorImpl extends ListViewColumnDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.LastLogFileSizeIncidentColumn_displayName();
        }

        @Override
        public boolean shownByDefault() {
            return false;
        }
    }

}
