package org.softauto.avro.tools;

import org.apache.avro.Compiler;
import org.apache.avro.Protocol;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompilerTool implements Tool {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(CompilerTool.class);

    @Override
    public int run(InputStream in, PrintStream out, PrintStream err, List<Object> arguments) throws Exception {

        try {
            if (arguments.size() < 5) {
                System.err.println(
                        "Usage: -sourceDirectory <schema source directory>  -src <src>  -classpath <fully qualified velocity resource path jar >   -template <template> -name <name>  outputdir");
                System.err.println(" sourceDirectory   - schema source directory ");
                System.err.println(" src               - fully qualified schema url without the avpr extension ");
                System.err.println(" classpath         - fully qualified velocity resource path of absolute jar ");
                System.err.println(" template          - vm template name to use");
                System.err.println(" name              - name of the output file");
                System.err.println(" outputdir         - directory to write generated java");
                return 1;
            }

            Optional<String> src = Optional.empty();
            Optional<String> template = Optional.empty();
            Optional<String> name = Optional.empty();
            Optional<String> sourceDirectory = Optional.empty();
            Optional<String> classpath = Optional.empty();
            int arg = 0;
            List<Object> args = new ArrayList<>(arguments);

            if (args.contains("-sourceDirectory")) {
                arg = args.indexOf("-sourceDirectory") + 1;
                sourceDirectory = Optional.of(args.get(arg).toString());
                args.remove(arg);
                args.remove(arg - 1);
            }

            if (args.contains("-src")) {
                arg = args.indexOf("-src") + 1;
                src = Optional.of(args.get(arg).toString());
                args.remove(arg);
                args.remove(arg - 1);
            }

            if (args.contains("-classpath")) {
                arg = args.indexOf("-classpath") + 1;
                classpath = Optional.of(args.get(arg).toString());
                args.remove(arg);
                args.remove(arg - 1);
            }

            if (args.contains("-template")) {
                arg = args.indexOf("-template") + 1;
                template = Optional.of(args.get(arg).toString());
                args.remove(arg);
                args.remove(arg - 1);
            }

            if (args.contains("-name")) {
                arg = args.indexOf("-name") + 1;
                name = Optional.of(args.get(arg).toString());
                args.remove(arg);
                args.remove(arg - 1);
            }

            File output = new File(args.get(args.size() - 1).toString());
            doCompile(src.get(),sourceDirectory.get(),output,classpath.get(),template.get(),name.get());

            logger.info("successfully compile schema ");
        }catch (Exception e){
            logger.error("fail compile schema ", e);
            return 1;
        }
        return 0;
    }

    public static void addURL(URL u, URLClassLoader sysloader)  throws MojoExecutionException {
        Class[] parameters = new Class[]{URL.class};
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u });
        } catch (Throwable t) {
            throw new MojoExecutionException("add url fail "+ u ,t);
        }
    }

    public static void addJarToClasspath(String f) {
        try {
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            addURL(new File(f).toURL(),classLoader);

        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    protected void doCompile(String filename, String sourceDirectory, File outputDirectory,String classpath,String template,String outputName) throws IOException {
        File src = new File(sourceDirectory, filename);
        Protocol protocol = Protocol.parse(src);
        Compiler compiler = new Compiler(protocol);
        addJarToClasspath(classpath);
        compiler.setTemplateDir("");
        compiler.compileProtocolToDestination(src, outputDirectory, template, outputName);
    }

    @Override
    public String getName() {
        return "CompilerTool";
    }

    @Override
    public String getShortDescription() {
        return "Generates Java code for the given protocol.";
    }
}
