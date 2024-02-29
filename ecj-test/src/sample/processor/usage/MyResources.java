package sample.processor.usage;

import sample.processor.api.GeneratedResourceFamilyDefinition;
import sample.processor.api.WrapperDefinition;
import sample.processor.usage.testing.ITestResource;

@GeneratedResourceFamilyDefinition(wrapper = @WrapperDefinition(className = "MyResourceWrapperForTesting"), feature = ITestResource.class)
public class MyResources {

	public void doSomething() {
		// whatever
	}
}
