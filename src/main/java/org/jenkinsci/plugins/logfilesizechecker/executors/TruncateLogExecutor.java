package org.jenkinsci.plugins.logfilesizechecker.executors;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Run;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.logfilesizechecker.Messages;
import org.jenkinsci.plugins.logfilesizechecker.actions.TruncateLogAction;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TruncateLogExecutor extends LogFileSizeCheckerExecutor {

    @DataBoundConstructor
    public TruncateLogExecutor() {
        super();
    }


    @Override
    public void execute(@Nonnull Run currentRun) throws IOException, InterruptedException {
        File logFile = currentRun.getLogFile();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(logFile, false);
            fos.getChannel().truncate(0);
            IOUtils.write(Messages.TruncateLogExecutor_truncateIndication() + "\n", fos);

            TruncateLogAction currentAction = currentRun.getAction(TruncateLogAction.class);
            if (currentAction == null) {
                currentAction = new TruncateLogAction();
            }
            int currentCount = currentAction.getCount();

            currentRun.addOrReplaceAction(new TruncateLogAction(currentCount + 1));
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<LogFileSizeCheckerExecutor> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.TruncateLogExecutor_displayName();
        }

    }
}
