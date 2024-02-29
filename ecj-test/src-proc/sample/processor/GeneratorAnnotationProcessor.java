package sample.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import sample.processor.api.GeneratedResourceFamilyDefinition;

@SupportedAnnotationTypes({ "sample.processor.api.GeneratedResourceFamilyDefinition",
		"sample.processor.api.WrapperDefinition", "sample.processor.api.ResourceDefinition" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class GeneratorAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		for (TypeElement annotation : annotations) {
			Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

			for (Element element : annotatedElements) {
				GeneratedResourceFamilyDefinition annotation2 = element
						.getAnnotation(GeneratedResourceFamilyDefinition.class);
				if (annotation2 != null) {
					String wrapperClassName = annotation2.wrapper().className();
					if (wrapperClassName == null || wrapperClassName.trim().isEmpty())
						continue;

					try {
						writeWrapperFile(wrapperClassName, (TypeElement) element);
					} catch (IOException e) {
						throw new IllegalStateException("Unable to generate output. " + e.getMessage(), e);
					}
				}

			}
		}

		return true;
	}

	private void writeWrapperFile(String simpleClassName, TypeElement annotatedElement) throws IOException {
		String packageName = ((PackageElement) annotatedElement.getEnclosingElement()).getQualifiedName() + ".wrappers";

		String wrapperClassName = packageName + "." + simpleClassName;
		JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(wrapperClassName, annotatedElement);

		try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

			out.print("package ");
			out.print(packageName);
			out.println(";");
			out.println();

			out.print("public class ");
			out.print(simpleClassName);
			out.println(" {");
			out.println();

			out.println("}");
		}
	}
}