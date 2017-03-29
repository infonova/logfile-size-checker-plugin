package org.jenkinsci.plugins.logfilesizechecker.executors;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Executor;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.CauseOfInterruption;
import org.jenkinsci.plugins.logfilesizechecker.GlobalLogFileSizeCheckerConfig;
import org.jenkinsci.plugins.logfilesizechecker.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Logger;

public class TerminateExecutor extends LogFileSizeCheckerExecutor {

    private static final Logger LOGGER = Logger.getLogger(TerminateExecutor.class.getName());

    public static class LogFileSizeInterruption extends CauseOfInterruption {

        private final long logFileSize;

        LogFileSizeInterruption(long logFileSize) {
            this.logFileSize = logFileSize;
        }

        @Override
        public String getShortDescription() {
            String logFileSizeMB = String.format("%.2f", (float) logFileSize / GlobalLogFileSizeCheckerConfig.MB_FACTOR);
            return Messages.LogFileSizeInterruption_shortDescription(logFileSizeMB);
        }
    }


    private static final Result DEFAULT_BUILD_RESULT = Result.FAILURE;

    private Result buildResult;

    @DataBoundConstructor
    public TerminateExecutor(String buildResult) {
        this.buildResult = Result.fromString(buildResult);
    }


    public Result getBuildResult() {
        if (buildResult == null) {
            buildResult = DEFAULT_BUILD_RESULT;
        }

        return buildResult;
    }

    @SuppressWarnings("unused")
    public boolean isAbortBuild() {
        return Result.ABORTED.equals(buildResult);
    }

    @SuppressWarnings("unused")
    public boolean isFailBuild() {
        return Result.FAILURE.equals(buildResult);
    }

    @Override
    public void execute(@Nonnull Run currentRun) throws IOException, InterruptedException {
        long logFileSize = currentRun.getLogFile().length();

        Executor executor = currentRun.getExecutor();

        if (executor != null) {
            executor.interrupt(getBuildResult(), new LogFileSizeInterruption(logFileSize));
        } else {
            LOGGER.severe(Messages.TerminateExecutor_unableToTerminateRun(currentRun.getFullDisplayName()));
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<LogFileSizeCheckerExecutor> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.TerminateExecutor_displayName();
        }

    }
}
