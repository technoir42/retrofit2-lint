package com.sch.retrofit2.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.sch.retrofit2.lint.checks.UnusedCallObjectDetector;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class LintIssueRegistry extends IssueRegistry {
    @Override
    public List<Issue> getIssues() {
        return Collections.singletonList(UnusedCallObjectDetector.ISSUE);
    }
}
