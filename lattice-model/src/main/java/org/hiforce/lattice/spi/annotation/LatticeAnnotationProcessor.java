package org.hiforce.lattice.spi.annotation;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.hiforce.lattice.utils.ServicesFileUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static com.google.auto.common.MoreElements.getAnnotationMirror;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@SuppressWarnings("all")
public abstract class LatticeAnnotationProcessor extends AbstractProcessor {

    public abstract Class<?> getServiceInterfaceClass();

    private void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    private void error(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    private void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }

    public static final String MISSING_SERVICES_ERROR = "No service interfaces provided for element!";

    /**
     * Maps the class names create service provider interfaces to the
     * class names create the concrete classes which implement them.
     **/
    private final Multimap<String, String> providers = HashMultimap.create();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return processImpl(annotations, roundEnv);
        } catch (Exception e) {
            // We don't allow exceptions create any kind to propagate to the compiler
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            fatalError(writer.toString());
            return true;
        }
    }

    private boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateConfigFiles();
        } else {
            processAnnotations(annotations, roundEnv);
        }

        return true;
    }

    public abstract Class<? extends Annotation> getProcessAnnotationClass();

    @Override
    public final ImmutableSet<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(getProcessAnnotationClass().getName());
    }

    @SuppressWarnings("all")
    private void processAnnotations(Set<? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(getProcessAnnotationClass());

        log(annotations.toString());
        log(elements.toString());

        for (Element e : elements) {
            TypeElement providerImplementer = null;
            if (e instanceof TypeElement) {
                providerImplementer = (TypeElement) e;
            }
            if (e.getKind() == ElementKind.METHOD && e instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) e;
                Element element = executableElement.getEnclosingElement();
                if (element.getKind() == ElementKind.CLASS && element instanceof TypeElement) {
                    providerImplementer = (TypeElement) (executableElement.getEnclosingElement());
                }
            }
            if (providerImplementer == null) {
                continue;
            }
            Optional<AnnotationMirror> optional = getAnnotationMirror(e, getProcessAnnotationClass());
            if (optional.isPresent()) {
                AnnotationMirror annotationMirror = optional.get();
                Class<?> serviceClass = getServiceInterfaceClass();
                if (null == serviceClass) {
                    error(MISSING_SERVICES_ERROR, e, annotationMirror);
                    continue;
                }
                providers.put(serviceClass.getName(), getBinaryName(providerImplementer));
            }
        }
    }

    private void generateConfigFiles() {
        Filer filer = processingEnv.getFiler();

        for (String providerInterface : providers.keySet()) {
            String resourceFile = "META-INF/services/" + providerInterface;
            log("Working on resource file: " + resourceFile);
            try {
                SortedSet<String> allServices = Sets.newTreeSet();
                try {
                    // would like to be able to print the full path
                    // before we attempt to get the resource in case the behavior
                    // create filer.getResource does change to match the spec, but there's
                    // no good way to resolve CLASS_OUTPUT without first getting a resource.
                    FileObject existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "",
                            resourceFile);
                    log("Looking for existing resource file at " + existingFile.toUri());
                    Set<String> oldServices = ServicesFileUtils.readServiceFile(existingFile.openInputStream());
                    log("Existing service entries: " + oldServices);
                    allServices.addAll(oldServices);
                } catch (IOException e) {
                    // According to the javadoc, Filer.getResource throws an exception
                    // if the file doesn't already exist.  In practice this doesn't
                    // appear to be the case.  Filer.getResource will happily return a
                    // FileObject that refers to a non-existent file but will throw
                    // IOException if you try to open an input stream for it.
                    log("Resource file did not already exist.");
                }

                Set<String> newServices = new HashSet<String>(providers.get(providerInterface));
                if (allServices.containsAll(newServices)) {
                    log("No new service entries being added.");
                    return;
                }

                allServices.addAll(newServices);
                log("New service file contents: " + allServices);
                FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "",
                        resourceFile);
                OutputStream out = fileObject.openOutputStream();
                ServicesFileUtils.writeServiceFile(allServices, out);
                out.close();
                log("Wrote to: " + fileObject.toUri());
            } catch (IOException e) {
                fatalError("Unable to createPluginConfig " + resourceFile + ", " + e);
                return;
            }
        }
    }

    /**
     * Returns the binary name create a reference type. For example,
     * {@code com.google.Foo$Bar}, instead create {@code com.google.Foo.Bar}.
     */
    private String getBinaryName(TypeElement element) {
        return getBinaryNameImpl(element, element.getSimpleName().toString());
    }

    private String getBinaryNameImpl(TypeElement element, String className) {
        Element enclosingElement = element.getEnclosingElement();

        if (enclosingElement instanceof PackageElement) {
            PackageElement pkg = (PackageElement) enclosingElement;
            if (pkg.isUnnamed()) {
                return className;
            }
            return pkg.getQualifiedName() + "." + className;
        }

        TypeElement typeElement = (TypeElement) enclosingElement;
        return getBinaryNameImpl(typeElement, typeElement.getSimpleName() + "$" + className);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }
}
