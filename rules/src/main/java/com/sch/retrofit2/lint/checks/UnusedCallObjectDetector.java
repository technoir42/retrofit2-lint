package com.sch.retrofit2.lint.checks;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnusedCallObjectDetector extends Detector implements Detector.JavaPsiScanner {
    private static final List<String> RETROFIT_HTTP_METHOD_ANNOTATIONS = Arrays.asList(
            "retrofit2.http.GET", "retrofit2.http.POST", "retrofit2.http.PUT", "retrofit2.http.DELETE",
            "retrofit2.http.HEAD", "retrofit2.http.OPTIONS", "retrofit2.http.PATCH", "retrofit2.http.HTTP");

    public static final Issue ISSUE = Issue.create(
            "UnusedCallObject",
            "Unused call object",
            "Detects when call object is created but not used",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            new Implementation(UnusedCallObjectDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<Class<? extends PsiElement>> getApplicablePsiTypes() {
        return Collections.singletonList(PsiMethodCallExpression.class);
    }

    @Override
    public JavaElementVisitor createPsiVisitor(JavaContext context) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression node) {
                if (isRetrofitEndpoint(node.resolveMethod()) && !isCallResultUsed(node)) {
                    context.report(ISSUE, context.getLocation(node), "Call object was created but never used.");
                }
            }
        };
    }

    private boolean isRetrofitEndpoint(PsiMethod method) {
        PsiAnnotation[] annotations = method.getModifierList().getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            if (isRetrofitHttpMethodAnnotation(annotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRetrofitHttpMethodAnnotation(PsiAnnotation annotation) {
        return RETROFIT_HTTP_METHOD_ANNOTATIONS.contains(annotation.getQualifiedName());
    }

    private boolean isCallResultUsed(PsiMethodCallExpression call) {
        if (hasChainedMethodCall(call)) {
            return true;
        } else if (getBoundVariable(call, true) != null) {
            // TODO: Check that variable is actually used
            return true;
        } else if (PsiTreeUtil.getParentOfType(call, PsiReturnStatement.class) != null) {
            return true;
        } else if (PsiTreeUtil.getParentOfType(call, PsiMethodCallExpression.class) != null) {
            return true;
        }
        return false;
    }

    private PsiVariable getBoundVariable(PsiElement rhs, boolean allowChainedCalls) {
        PsiElement parent = LintUtils.skipParentheses(rhs.getParent());
        if (allowChainedCalls) {
            while (parent instanceof PsiReferenceExpression) {
                PsiElement assignment = LintUtils.skipParentheses(parent.getParent());
                if (!(assignment instanceof PsiMethodCallExpression)) {
                    break;
                }

                parent = LintUtils.skipParentheses(assignment.getParent());
            }
        }

        if (parent instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignment = (PsiAssignmentExpression) parent;
            PsiExpression lhs = assignment.getLExpression();
            if (lhs instanceof PsiReference) {
                PsiElement element = ((PsiReference) lhs).resolve();
                if (element instanceof PsiVariable) {
                    return (PsiVariable) element;
                }
            }
        } else if (parent instanceof PsiVariable) {
            return (PsiVariable) parent;
        }

        return null;
    }

    private boolean hasChainedMethodCall(PsiMethodCallExpression node) {
        for (PsiElement parent = LintUtils.skipParentheses(node.getParent()); parent != null; parent = LintUtils.skipParentheses(parent.getParent())) {
            if (parent instanceof PsiMethodCallExpression) {
                return true;
            } else if (!(parent instanceof PsiReferenceExpression)) {
                return false;
            }
        }
        return false;
    }
}
