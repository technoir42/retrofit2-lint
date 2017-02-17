package com.sch.retrofit2.lint.checks;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import org.intellij.lang.annotations.Language;

import java.util.Collections;
import java.util.List;

public class UnusedCallObjectDetectorTest extends LintDetectorTest {
    @Language("JAVA")
    private static final String RETROFIT_CALL_SOURCE = "package retrofit2;\n" +
            "\n" +
            "public class Call<T> {\n" +
            "    public void execute() {\n" +
            "    }\n" +
            "}\n";
    @Language("JAVA")
    private static final String RETROFIT_GET_SOURCE = "package retrofit2.http;\n" +
            "\n" +
            "import java.lang.annotation.ElementType;\n" +
            "import java.lang.annotation.Target;\n" +
            "\n" +
            "@Target(ElementType.METHOD)\n" +
            "public @interface GET {\n" +
            "    String value();\n" +
            "}\n";
    @Language("JAVA")
    private static final String API_INTERFACE_SOURCE = "package com.example;\n" +
            "\n" +
            "import retrofit2.Call;\n" +
            "import retrofit2.http.GET;\n" +
            "\n" +
            "public interface ApiInterface {\n" +
            "    @GET(\"foo\")\n" +
            "    Call<Void> foo();\n" +
            "}\n";

    @Override
    protected Detector getDetector() {
        return new UnusedCallObjectDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(UnusedCallObjectDetector.ISSUE);
    }

    public void testUnusedCall() throws Exception {
        @Language("JAVA")
        final String source = "package com.example;\n" +
                "\n" +
                "public class TestClient {\n" +
                "    public void callFoo(ApiInterface apiInterface) {\n" +
                "        apiInterface.foo();\n" +
                "    }\n" +
                "}\n";
        final String expected = "src/com/example/TestClient.java:5: Error: Call object was created but never used. [UnusedCallObject]\n" +
                "        apiInterface.foo();\n" +
                "        ~~~~~~~~~~~~~~~~~~\n" +
                "1 errors, 0 warnings\n";
        final String result = lintProject(
                java("src/retrofit2/Call.java", RETROFIT_CALL_SOURCE),
                java("src/retrofit2/http/GET.java", RETROFIT_GET_SOURCE),
                java("src/com/example/ApiInterface.java", API_INTERFACE_SOURCE),
                java("src/com/example/TestClient.java", source));
        assertEquals(expected, result);
    }

    public void testExecute() throws Exception {
        @Language("JAVA")
        final String source = "package com.example;\n" +
                "\n" +
                "public class TestClient {\n" +
                "    public void callFoo(ApiInterface apiInterface) {\n" +
                "        apiInterface.foo().execute();\n" +
                "    }\n" +
                "}\n";
        final String expected = "No warnings.";
        final String result = lintProject(
                java("src/retrofit2/Call.java", RETROFIT_CALL_SOURCE),
                java("src/retrofit2/http/GET.java", RETROFIT_GET_SOURCE),
                java("src/com/example/ApiInterface.java", API_INTERFACE_SOURCE),
                java("src/com/example/TestClient.java", source));
        assertEquals(expected, result);
    }

    public void testAssignToVariable() throws Exception {
        @Language("JAVA")
        final String source = "package com.example;\n" +
                "\n" +
                "import retrofit2.Call;\n" +
                "\n" +
                "public class TestClient {\n" +
                "    public void callFoo(ApiInterface apiInterface) {\n" +
                "        Call<Void> call = apiInterface.foo();\n" +
                "    }\n" +
                "}\n";
        final String expected = "No warnings.";
        final String result = lintProject(
                java("src/retrofit2/Call.java", RETROFIT_CALL_SOURCE),
                java("src/retrofit2/http/GET.java", RETROFIT_GET_SOURCE),
                java("src/com/example/ApiInterface.java", API_INTERFACE_SOURCE),
                java("src/com/example/TestClient.java", source));
        assertEquals(expected, result);
    }

    public void testReturn() throws Exception {
        @Language("JAVA")
        final String source = "package com.example;\n" +
                "\n" +
                "import retrofit2.Call;\n" +
                "\n" +
                "public class TestClient {\n" +
                "    public Call<Void> callFoo(ApiInterface apiInterface) {\n" +
                "        return apiInterface.foo();\n" +
                "    }\n" +
                "}\n";
        final String expected = "No warnings.";
        final String result = lintProject(
                java("src/retrofit2/Call.java", RETROFIT_CALL_SOURCE),
                java("src/retrofit2/http/GET.java", RETROFIT_GET_SOURCE),
                java("src/com/example/ApiInterface.java", API_INTERFACE_SOURCE),
                java("src/com/example/TestClient.java", source));
        assertEquals(expected, result);
    }

    public void testPassParameter() throws Exception {
        @Language("JAVA")
        final String source = "package com.example;\n" +
                "\n" +
                "import retrofit2.Call;\n" +
                "\n" +
                "public class TestClient {\n" +
                "    public void callFoo(ApiInterface apiInterface) {\n" +
                "        bar(apiInterface.foo());\n" +
                "    }\n" +
                "    \n" +
                "    private void bar(Call<Void> call) {\n" +
                "    }\n" +
                "}\n";
        final String expected = "No warnings.";
        final String result = lintProject(
                java("src/retrofit2/Call.java", RETROFIT_CALL_SOURCE),
                java("src/retrofit2/http/GET.java", RETROFIT_GET_SOURCE),
                java("src/com/example/ApiInterface.java", API_INTERFACE_SOURCE),
                java("src/com/example/TestClient.java", source));
        assertEquals(expected, result);
    }
}
