package org.textformatter;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AppTest {

    @DataProvider(name = "badArgsProvider")
    public Object[][] badArgsProviderMethod() {
        return new Object[][] {
                { null },
                { "" },
                { "", "" },
                { "a", "b", "c", "d" },
                { "r", "0", "a" },
                { "r", "-1", "a" }
        };
    }

    @DataProvider(name = "goodArgsProvider")
    public Object[][] goodArgsProviderMethod() {
        return new Object[][] {
                { new String[]{"r", "10", "a b c d"}, "   a b c d"},
                { new String[]{"r", "20", "a b c d"}, "             a b c d" },

                { new String[]{"r", "10", "Lorem\nipsum\ndolor"}, "     Lorem\n     ipsum\n     dolor"},
                { new String[]{"r", "10", "  Lorem \nipsum   \n dolor         "}, "     Lorem\n     ipsum\n     dolor"},
                { new String[]{"r", "20", "Lorem ipsum\ndolor\nsit amet"}, "         Lorem ipsum\n               dolor\n            sit amet"},
                { new String[]{"r", "10", "This text should be right aligned"}, " This text\n should be\n     right\n   aligned"},
                { new String[]{"r", "20", "This text should be right aligned"}, " This text should be\n       right aligned"},

                { new String[]{"l", "10", "Lorem\nipsum\ndolor"}, "Lorem     \nipsum     \ndolor     "},
                { new String[]{"l", "10", "   Lorem\n ipsum\n               dolor"}, "Lorem     \nipsum     \ndolor     "},
                { new String[]{"l", "20", "Lorem ipsum\ndolor\nsit amet"}, "Lorem ipsum         \ndolor               \nsit amet            "},
                { new String[]{"l", "10", "This text should be left aligned"}, "This text \nshould be \nleft      \naligned   "},
                { new String[]{"l", "20", "This text should be left aligned"}, "This text should be \nleft aligned        "},

                { new String[]{"c", "10", "This text should be center aligned"}, "This text \nshould be \n  center  \n aligned  "},
                { new String[]{"c", "20", "This text should be center aligned"}, "This text should be \n   center aligned   "},
                { new String[]{"c", "11", "Lorem\nipsum\ndolor"}, "   Lorem   \n   ipsum   \n   dolor   "},
                { new String[]{"c", "11", "Lorem ipsum \n  dolor \n        sit amet"}, "Lorem ipsum\n   dolor   \n sit amet  "},
        };
    }

    App p = null;
    ByteArrayOutputStream baos = null;

    @BeforeMethod
    public void before() {
        baos = new ByteArrayOutputStream();
        p = new App(new PrintStream(baos));
    }

    @AfterMethod
    public void after() throws IOException {
        p.close();
    }

    private String getOutput() {
        return baos.toString(StandardCharsets.UTF_8);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class, ArithmeticException.class }, dataProvider = "badArgsProvider")
    public void badArgs(String[] args) throws Exception {
        p.invoke(args);
    }

    @Test(dataProvider = "goodArgsProvider")
    public void goodArgs(String[] args, String expected) throws Exception {
        p.invoke(args);
        assertThat(getOutput(), equalTo(expected));
    }
}
