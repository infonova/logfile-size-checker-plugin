package org.jenkinsci.plugins.logfilesizechecker.executors;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class LogFileSizeCheckerExecutor implements Describable<LogFileSizeCheckerExecutor>, ExtensionPoint {

    public static DescriptorExtensionList<LogFileSizeCheckerExecutor, Descriptor<LogFileSizeCheckerExecutor>> all() {
        return Jenkins.getInstance().getDescriptorList(LogFileSizeCheckerExecutor.class);
    }

    public abstract void execute(@Nonnull  Run currentRun) throws IOException, InterruptedException;

    @Override
    public Descriptor<LogFileSizeCheckerExecutor> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(getClass());
    }

}
