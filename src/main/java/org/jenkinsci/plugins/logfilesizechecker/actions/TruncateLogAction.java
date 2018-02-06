package org.jenkinsci.plugins.logfilesizechecker.actions;

import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.logfilesizechecker.Messages;

public class TruncateLogAction extends InvisibleAction {

    private final int count;

    public TruncateLogAction() {
        this(0);
    }

    public TruncateLogAction(int count) {
        this.count = count;
    }

    public String getCause() {
        return Messages.TruncateLogAction_cause(count);
    }

    public int getCount() {
        return count;
    }
}
