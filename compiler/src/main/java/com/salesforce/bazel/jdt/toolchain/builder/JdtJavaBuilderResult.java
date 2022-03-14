package com.salesforce.bazel.jdt.toolchain.builder;

/**
 * Return result of a {@link JdtJavaBuilderResult} build.
 */
public class JdtJavaBuilderResult {
	  
    private final boolean ok;
    private final String output;

    public JdtJavaBuilderResult(boolean ok, String output) {
        this.ok = ok;
        this.output = output;
    }

    /**
     * True if the compilation was successful.
     */
    public boolean isOk() {
    	return ok;
    }

    /**
     * Log output from the compilation.
     */
    public String output() {
        return output;
    }

}