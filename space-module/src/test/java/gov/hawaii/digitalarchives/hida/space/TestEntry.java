package gov.hawaii.digitalarchives.hida.space;

import java.io.Serializable;

public class TestEntry implements Serializable {
    public String foo;
    public Integer id;

    public TestEntry(final String foo) {
        this.foo = foo;
        this.id = -1;
    }

    public TestEntry(final String foo, final Integer id) {
        this.foo = foo;
        this.id = id;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(final String foo) {
        this.foo = foo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }
}
