# logfile-size-checker-plugin

The `logfile-size-checker` plugin allows Jenkins to fail or abort builds if the log file gets to large.

For this reason, the administrator can configure following settings on the "Manage Jenkins" page:
* Set the period in seconds at which the check runs
* Set the maximum size in MB
* Specify the outcome when log file exceeds defined max size
