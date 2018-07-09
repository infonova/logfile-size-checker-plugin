package org.jenkinsci.plugins.logfilesizechecker;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.support.steps.ExecutorStepExecution;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class LogFileSizeCheckerWorker extends AsyncAperiodicWork {

    private static final Lock EXECUTE_LOCK = new ReentrantLock();

    private static final Logger LOGGER = Logger.getLogger(LogFileSizeCheckerWorker.class.getName());

    public LogFileSizeCheckerWorker() {
        super(Messages.LogFileSizeCheckerWorker_name());
    }


    private Run getCurrentRun(Queue.Executable executable) {
        if (executable instanceof Run) {
            return (Run)executable;
        }

        if (Jenkins.getInstance().getPlugin("workflow-durable-task-step") != null) {
            if (executable.getParent() instanceof ExecutorStepExecution.PlaceholderTask) {
                return ((ExecutorStepExecution.PlaceholderTask) executable.getParent()).run();
            }
        }

        if (executable != null && executable.getParent().getOwnerTask() instanceof Job) {
            Job job = (Job)executable.getParent().getOwnerTask();
            return job.getLastBuild();
        }

        return null;
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        if (EXECUTE_LOCK.tryLock()) {
            try {
                Computer[] computers = Jenkins.getInstance().getComputers();
                for (Computer computer : computers) {
                    List<Executor> executors = computer.getExecutors();

                    for (Executor executor : executors) {
                        try {
                            if (executor.isBusy()) {
                                Run currentRun = getCurrentRun(executor.getCurrentExecutable());

                                if (currentRun != null) {
                                    LOGGER.log(Level.FINER, Messages.LogFileSizeCheckerWorker_checkingCurrentRun(currentRun.getFullDisplayName()));
                                    long currentLogFileSize = currentRun.getLogFile().length();

                                    if (currentLogFileSize >= getConfig().getMaxLogFileSizeBytes()) {
                                        getConfig().getLogFileSizeCheckerExecutor().execute(currentRun);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, Messages.LogFileSizeCheckerWorker_unableToCheckLog(executor), e);
                        }
                    }
                }
            } finally {
                EXECUTE_LOCK.unlock();
            }
        } else {
            LOGGER.log(Level.WARNING, Messages.LogFileSizeCheckerWorker_checkAlreadyRunning());
        }
    }

    @Override
    public AperiodicWork getNewInstance() {
        return new LogFileSizeCheckerWorker();
    }

    @Override
    public long getRecurrencePeriod() {
        return getConfig().getRecurrencePeriodMillis();
    }

    public GlobalLogFileSizeCheckerConfig getConfig() {
        // injecting with @Inject won't work, because it class gets a new instance every now and then, so use this
        // method as suggested by the documentation of GlobalConfiguration
        return GlobalConfiguration.all().get(GlobalLogFileSizeCheckerConfig.class);
    }

}
