package io.docbot.mojo;

import io.github.classgraph.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo( name = "print-spring-mvc",
        defaultPhase= LifecyclePhase.COMPILE,
        configurator = "include-project-dependencies",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PrintSpringMvcMojo extends AbstractMojo {
    @Parameter
    private Set<String> packages;
    @Parameter
    private String outputPath;
    
    @Parameter(defaultValue = "false")
    private Boolean showClassGraphLog;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        ClassGraph graph = new ClassGraph().enableAllInfo().disableJarScanning();
        if(showClassGraphLog){
            graph.verbose();
        }
        if(packages != null && packages.size() > 0) {
            packages.forEach(p->graph.whitelistPackages(p));
        }
        this.getLog().info("scan:" + packages.toString());
        String restControllerAnnotation = "org.springframework.web.bind.annotation.RestController";
        String controllerAnnotation = "org.springframework.stereotype.Controller";
        String requestMappingAnnotation = "org.springframework.web.bind.annotation.RequestMapping";
        try (ScanResult scanResult = graph.scan()) {
            Set<ClassInfo> controllerClassInfo = new HashSet<>();
            controllerClassInfo.addAll(scanResult.getClassesWithAnnotation(restControllerAnnotation));
            controllerClassInfo.addAll(scanResult.getClassesWithAnnotation(controllerAnnotation));
            this.getLog().info("find Class:" + controllerClassInfo.toString());
            StringBuilder data = new StringBuilder();
            for (ClassInfo routeClassInfo : controllerClassInfo) {
                AnnotationInfo annotationInfo = routeClassInfo.getAnnotationInfo(requestMappingAnnotation);
                Set<String> prefix;
                prefix = getRequestMappingUrl(annotationInfo);
                if (prefix.isEmpty()) {
                    prefix.add("/");
                }
                Set<String> methods = getRequestMappingMethod(annotationInfo);
                MethodInfoList requestMappingMethods = routeClassInfo.getMethodInfo().filter(methodInfo ->
                        methodInfo.getAnnotationInfo(requestMappingAnnotation) != null);
                Map<String, MethodInfoList> methodInfoListMap  = requestMappingMethods.asMap();
                for(Map.Entry<String, MethodInfoList> entry : methodInfoListMap.entrySet()){
                    MethodInfoList methodInfos = entry.getValue();
                    for(MethodInfo m : methodInfos){
                        AnnotationInfo requestMappingAnnotationInfo = m.getAnnotationInfo(requestMappingAnnotation);
                        Set<String> uri = getRequestMappingUrl(requestMappingAnnotationInfo);
                        Set<String> requestMethods = getRequestMappingMethod(requestMappingAnnotationInfo);
                        requestMethods.addAll(methods);
                        if(uri.isEmpty()){
                            uri.add("");
                        }
                        if(requestMethods.isEmpty()){
                            requestMethods.add("");
                        }
                        for (String u : uri) {
                            for (String p : prefix) {
                                for(String method : requestMethods) {
                                    StringBuilder path = new StringBuilder();
                                    if (!p.startsWith("/")) {
                                        path.append("/");
                                    }
                                    path.append(p);
                                    if(!p.endsWith("/")){
                                          path.append("/");
                                    }
                                    if (u.startsWith("/")) {
                                        path.append(u.substring(1));
                                    } else {
                                        path.append(u);
                                    }
                                    data.append(String.format("%s:%s\n", path.toString(),method));
                                }
                            }
                        }
                    }
                }

            }
            if(outputPath != null){
                Path dataPath = Paths.get(outputPath,"data.url");
                File parentFile = dataPath.toFile().getParentFile();
                if(!parentFile.exists()){
                    parentFile.mkdirs();
                }
                try {
                    Files.write(dataPath,data.toString().getBytes());
                }catch (IOException e){
                    this.getLog().error(e);
                }
            }else{
                this.getLog().info(data.toString());
            }

        }
    }
    private  Set<String> getRequestMappingMethod(AnnotationInfo requestMappingAnnotationInfo){
        Set<String> requestMethods = new HashSet<>();
        if(requestMappingAnnotationInfo != null) {
            List<AnnotationParameterValue> methods = requestMappingAnnotationInfo.getParameterValues().stream().filter(
                    annotationParameterValue -> annotationParameterValue.getName().equals("method")).collect(Collectors.toList());
            for(AnnotationParameterValue m : methods){
                Object[] data = (Object[])m.getValue();
                for (Object code : data){
                    AnnotationEnumValue annotationEnumValue = (AnnotationEnumValue)code;
                    requestMethods.add(annotationEnumValue.getValueName().toLowerCase());
                }
            }
        }
        return requestMethods;
    }
    private  Set<String> getRequestMappingUrl(AnnotationInfo requestMappingAnnotationInfo){
        Set<String> urlSet = new HashSet<>();
        if(requestMappingAnnotationInfo != null) {
            List<AnnotationParameterValue> path = requestMappingAnnotationInfo.getParameterValues().stream().filter(
                    annotationParameterValue -> annotationParameterValue.getName().equals("value") || annotationParameterValue.getName().equals("path")).collect(Collectors.toList());
            for(AnnotationParameterValue annotationParameterValue : path){
                Object[] data = (Object[])annotationParameterValue.getValue();
                for (Object url : data){
                    urlSet.add((String)url);
                }
            }
        }

        return urlSet;
    }
}
