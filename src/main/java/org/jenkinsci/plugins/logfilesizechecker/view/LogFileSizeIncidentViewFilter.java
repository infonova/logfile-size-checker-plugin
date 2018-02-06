package org.jenkinsci.plugins.logfilesizechecker.view;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.views.ViewJobFilter;
import org.jenkinsci.plugins.logfilesizechecker.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


@Extension
public class LogFileSizeIncidentViewFilter extends ViewJobFilter {

    /**
     * Our constructor.
     */
    @DataBoundConstructor
    public LogFileSizeIncidentViewFilter() {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopLevelItem> filter(List<TopLevelItem> added, List<TopLevelItem> all, View filteringView) {
        List<TopLevelItem> result = new ArrayList<>(added.size());

        for (TopLevelItem item : all) {
            if (item instanceof Job) {
                Job job = (Job) item;

                if (!job.getBuilds().filter(new LogFileSizeIncidentPredicate()).isEmpty()) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<ViewJobFilter> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.LogFileSizeIncidentViewFilter_displayName();
        }
    }
}
