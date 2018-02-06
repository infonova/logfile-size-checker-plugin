package org.jenkinsci.plugins.logfilesizechecker;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Failure;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jenkinsci.plugins.logfilesizechecker.executors.LogFileSizeCheckerExecutor;
import org.jenkinsci.plugins.logfilesizechecker.executors.TruncateLogExecutor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Extension
public class GlobalLogFileSizeCheckerConfig extends GlobalConfiguration {

    private static final String RECURRENCE_PERIOD_PARAMETER = "recurrencePeriod";
    private static final String MAX_LOGFILE_SIZE_PARAMETER  = "maxLogFileSize";
    private static final String EXECUTOR_TYPE_PARAMETER     = "executorType";

    public static final long MB_FACTOR = 1024 * 1024;

    private static final long MIN_RECURRENCE_PERIOD = 1;
    private static final long DEFAULT_RECURRENCE_PERIOD = 30;

    private static final long MIN_LOG_FILE_SIZE = 1;
    private static final long DEFAULT_LOG_FILE_SIZE = 100;

    private long recurrencePeriod;
    private long maxLogFileSize;
    private LogFileSizeCheckerExecutor logFileSizeCheckerExecutor;

    public GlobalLogFileSizeCheckerConfig() {
        super();
        load();
    }

    @Override
    @Nonnull
    public String getDisplayName() {
        return Messages.displayName();
    }

    public long getRecurrencePeriod() {
        if (recurrencePeriod < MIN_RECURRENCE_PERIOD) {
            recurrencePeriod = DEFAULT_RECURRENCE_PERIOD;
        }

        return recurrencePeriod;
    }

    public long getRecurrencePeriodMillis() {
        return TimeUnit.SECONDS.toMillis(getRecurrencePeriod());
    }

    public long getMaxLogFileSize() {
        if (maxLogFileSize < MIN_LOG_FILE_SIZE) {
            maxLogFileSize = DEFAULT_LOG_FILE_SIZE;
        }

        return maxLogFileSize;
    }

    public long getMaxLogFileSizeBytes() {
        return getMaxLogFileSize() * MB_FACTOR;
    }

    public LogFileSizeCheckerExecutor getLogFileSizeCheckerExecutor() {
        if (logFileSizeCheckerExecutor == null) {
            logFileSizeCheckerExecutor = new TruncateLogExecutor();
        }

        return logFileSizeCheckerExecutor;
    }

    private long checkNumber(String value, long minValue, String type) {
        if (StringUtils.isBlank(value)) {
            throw new Failure(Messages.GlobalConfig_numberIsEmpty(type));
        }
        if (!NumberUtils.isNumber(value)) {
            throw new Failure(Messages.GlobalConfig_numberIsNotANumber(type));
        }

        try {
            long number = Long.parseLong(value);
            if (number < minValue) {
                throw new Failure(Messages.GlobalConfig_numberIsInvalid(type, minValue));
            }
            return number;
        } catch (NumberFormatException e) {
            throw new Failure(Messages.GlobalConfig_numberIsTooBig(type, Long.MAX_VALUE));
        }
    }

    private long checkRecurrencePeriod(String value) {
        return checkNumber(value, MIN_RECURRENCE_PERIOD, Messages.GlobalConfig_recurrencePeriod());
    }

    private long checkMaxLogFileSize(String value) {
        return checkNumber(value, MIN_LOG_FILE_SIZE, Messages.GlobalConfig_maxLogFileSize());
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckRecurrencePeriod(@QueryParameter String value) throws IOException, ServletException {
        try {
            checkRecurrencePeriod(value);
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }

        return FormValidation.ok();
    }

    @SuppressWarnings("unused")
    public FormValidation doCheckMaxLogFileSize(@QueryParameter String value) throws IOException, ServletException {
        try {
            checkMaxLogFileSize(value);
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }

        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
        this.recurrencePeriod = checkRecurrencePeriod(formData.getString(RECURRENCE_PERIOD_PARAMETER));
        this.maxLogFileSize = checkMaxLogFileSize(formData.getString(MAX_LOGFILE_SIZE_PARAMETER));
        this.logFileSizeCheckerExecutor = LogFileSizeCheckerExecutor.all().newInstanceFromRadioList(formData.getJSONObject(EXECUTOR_TYPE_PARAMETER));

        save();

        return super.configure(req, formData);
    }

}

