package org.jenkinsci.plugins.logfilesizechecker.view;

import hudson.Extension;
import hudson.model.*;
import hudson.views.ViewJobFilter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;

@Extension
public class MaxLogFileSizeReachedViewFilter extends ViewJobFilter {

    /**
     * Our constructor.
     */
    @DataBoundConstructor
    public MaxLogFileSizeReachedViewFilter() {
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

                if (!job.getBuilds().filter(new InterruptedByMaxLogFileSizeReached()).isEmpty()) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends Descriptor<ViewJobFilter> {

        @Override
        public String getDisplayName() {
            return org.jenkinsci.plugins.logfilesizechecker.Messages.LogFileSizeCheckerViewFilter_displayName();
        }
    }
}
