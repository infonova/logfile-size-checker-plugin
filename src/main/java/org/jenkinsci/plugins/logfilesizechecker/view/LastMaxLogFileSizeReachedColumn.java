package org.jenkinsci.plugins.logfilesizechecker.view;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.logfilesizechecker.Messages;
import org.kohsuke.stapler.DataBoundConstructor;


public class LastMaxLogFileSizeReachedColumn extends ListViewColumn {

    @DataBoundConstructor
    public LastMaxLogFileSizeReachedColumn() {

    }

    @SuppressWarnings("unused")
    public Run getLastMaxLogFileSizeReachedBuild(Job job) {
        return job.getBuilds().filter(new InterruptedByMaxLogFileSizeReached()).getLastBuild();
    }

    @Extension @Symbol("lastMaxLogFileSizeReached")
    public static class DescriptorImpl extends ListViewColumnDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.LastMaxLogFileSizeReachedColumn_displayName();
        }

        @Override
        public boolean shownByDefault() {
            return false;
        }
    }

}
