package sample.processor.usage.testing;

import sample.processor.api.ResourceDefinition;
import sample.processor.usage.MyResources;
import sample.processor.usage.checks.ISomeChecks;
import sample.processor.usage.wrappers.MyResourceWrapperForTesting;

/**
 * A Test resource
 */
@ResourceDefinition(family = MyResources.class, checks = { ISomeChecks.CHECK_FOO })
public interface ITestResource {

	/**
	 * For testing use the generated {@link MyResourceWrapperForTesting}
	 */
	String TEST_NAME = "foobar";

}
