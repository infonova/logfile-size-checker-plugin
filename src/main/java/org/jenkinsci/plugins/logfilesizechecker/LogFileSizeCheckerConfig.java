package org.jenkinsci.plugins.logfilesizechecker;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Failure;
import hudson.model.Result;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Extension
public class LogFileSizeCheckerConfig extends GlobalConfiguration {

    static final long MB_FACTOR = 1024 * 1024;

    private static final long MIN_RECURRENCE_PERIOD = 1;
    private static final long DEFAULT_RECURRENCE_PERIOD = 30;

    private static final long MIN_LOG_FILE_SIZE = 1;
    private static final long DEFAULT_LOG_FILE_SIZE = 100;

    private static final Result DEFAULT_BUILD_RESULT = Result.FAILURE;

    private long recurrencePeriod;
    private long maxLogFileSize;
    private Result buildResult;

    public LogFileSizeCheckerConfig() {
        super();
        load();
        init();
    }

    private void init() {
        boolean isDirty = false;
        if (recurrencePeriod < MIN_RECURRENCE_PERIOD) {
            recurrencePeriod = DEFAULT_RECURRENCE_PERIOD;
            isDirty = true;
        }

        if (maxLogFileSize < MIN_LOG_FILE_SIZE) {
            maxLogFileSize = DEFAULT_LOG_FILE_SIZE;
            isDirty = true;
        }

        if (buildResult == null) {
            buildResult = DEFAULT_BUILD_RESULT;
            isDirty = true;
        }

        if (isDirty) {
            save();
        }
    }

    @Override
    @Nonnull
    public String getDisplayName() {
        return Messages.displayName();
    }

    public long getRecurrencePeriod() {
        return recurrencePeriod;
    }

    @SuppressWarnings("unused")
    public void setRecurrencePeriod(long recurrencePeriod) {
        this.recurrencePeriod = recurrencePeriod;
    }

    public long getRecurrencePeriodMillis() {
        return TimeUnit.SECONDS.toMillis(getRecurrencePeriod());
    }

    public long getMaxLogFileSize() {
        return maxLogFileSize;
    }

    public long getMaxLogFileSizeBytes() {
        return getMaxLogFileSize() * MB_FACTOR;
    }

    @SuppressWarnings("unused")
    public void setMaxLogFileSize(long maxLogFileSize) {
        this.maxLogFileSize = maxLogFileSize;
    }

    public Result getBuildResult() {
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
        this.recurrencePeriod = checkRecurrencePeriod(formData.getString("recurrencePeriod"));
        this.maxLogFileSize = checkMaxLogFileSize(formData.getString("maxLogFileSize"));
        this.buildResult = Result.fromString(formData.getString("buildResult"));
        save();

        return super.configure(req, formData);
    }
}

