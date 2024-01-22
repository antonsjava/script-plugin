/*
 * Copyright 2024 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.script;


import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;


@Mojo( name = "script", defaultPhase = LifecyclePhase.PACKAGE
    , requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ScriptMojo extends AbstractMojo {


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    protected ArtifactRepository localRepository;
    @Parameter(property = "destination", defaultValue = "${script.destination}", required = false )
    protected String destination;
    @Parameter(property = "filename", defaultValue = "${script.name}", required = false )
    protected String filename;
    @Parameter(property = "includeTest", defaultValue = "${script.includeTest}", required = false )
    protected String includeTest;
    @Parameter(property = "includeRepo", defaultValue = "${script.includeRepo}", required = false )
    protected String includeRepo;
    @Parameter(property = "exec", defaultValue = "${script.exec}", required = false )
    protected String executable;
    @Parameter(property = "shell", defaultValue = "${script.shell}", required = false )
    protected String shell;
    @Parameter(property = "unix", defaultValue = "${script.unix}", required = false )
    protected String unix;
    @Parameter(property = "windows", defaultValue = "${script.windows}", required = false )
    protected String windows;
    @Parameter(property = "pack", defaultValue = "${script.pack}", required = false )
    protected String pack;


	protected static String initProperty(String value, String defaultValue) {
		if(value == null) value = defaultValue;
        return value;
	}

	protected void initProperties() {

        destination = initProperty(destination, "target/script");
        filename = initProperty(filename, project.getArtifactId());
        includeTest = initProperty(includeTest, "false");
        includeRepo = initProperty(includeRepo, "false");
        shell = initProperty(shell, "/bin/bash");
        unix = initProperty(unix, "true");
        windows = initProperty(windows, "false");
        pack = initProperty(pack, "false");
	}


    protected void printConf() {
        getLog().info("[script] conf destination: " + destination);
        getLog().info("[script] conf name: " + filename);
        getLog().info("[script] conf includeTest: " + includeTest);
        getLog().info("");
    }

    protected boolean includeTest() {
        return "true".equals(includeTest);
    }
    protected boolean includeRepo() {
        return "true".equals(includeRepo);
    }
    protected boolean unix() {
        return "true".equals(unix);
    }
    protected boolean windows() {
        return "true".equals(windows);
    }
    protected boolean pack() {
        return "true".equals(pack);
    }


    @Override
    public void execute() throws MojoExecutionException {
        initProperties();
        printConf();
        try {

            FindMains mains = FindMains.instance();
            if(includeTest()) {
                List<String> items = project.getTestCompileSourceRoots();
                if(items != null) {
                    for(String item : items) {
                        mains.find(item);
                    }
                }
            }
            {
                List<String> items = project.getCompileSourceRoots();
                if(items != null) {
                    for(String item : items) {
                        mains.find(item);
                    }
                }
            }
            //getLog().info("[script] mains: " + mains.mains());

            List<String> cp = new ArrayList<>();
            if(includeRepo()) {
                if(includeTest()) mergeResouce(cp, project.getTestResources());
                mergeResouce(cp, project.getResources());
            }
            if(includeTest()) merge(cp, project.getTestClasspathElements());
            merge(cp, project.getCompileClasspathElements());


            if(pack()) {
                String repo = localRepository.getBasedir();
                String local = project.getBasedir().getAbsolutePath();
                List<String> newcp = new ArrayList();
                for(String string : cp) {
                    String dest = string;
                    if(dest.startsWith(repo)) {
                        dest = "repo"+dest.substring(repo.length());
                    } else if(dest.startsWith(local)) {
                        dest = "project"+dest.substring(local.length());
                    }
                    if(dest.startsWith("/")) dest = dest.substring(1);
                    String newname = "lib/"+dest;
                    Fs.copy(string, destination + "/" + newname);
                    newcp.add(newname);
                }
                cp = newcp;
            }


            if(unix()) {
                StringBuilder content = new StringBuilder();
                boolean first = true;
                for(String string : cp) {
                    if(first) {
                        content.append("-cp \"");
                        first = false;
                    } else {
                        content.append("\n:");
                    }
                    content.append(string).append('\\');
                }
                content.append("\n\"");
                Fs.save(destination + "/"+filename+"-cp.arg", "utf-8", content.toString());
            }
            if(windows()) {
                StringBuilder content = new StringBuilder();
                boolean first = true;
                for(String string : cp) {
                    if(first) {
                        content.append("-cp \"");
                        first = false;
                    } else {
                        content.append("\n;");
                    }
                    content.append(string.replace('/', '\\').replace("\\", "\\\\")).append('\\');
                }
                content.append("\n\"");
                Fs.save(destination + "/win-"+filename+"-cp.arg", "utf-8", content.toString());
            }


            if(unix()) {
                StringBuilder content = new StringBuilder();
                if(!"".equals(shell)) content.append("#!").append(shell).append('\n');
                content.append('\n');
                for(String string : mains.mains()) {
                    content.append("# java @"+filename+"-cp.arg ").append(string).append(" \"$@\"\n");
                }
                if(executable != null) content.append("java @"+filename+"-cp.arg ").append(executable).append(" \"$@\"\n");
                content.append('\n');
                String fname = destination + "/"+filename+".sh";
                Fs.save(fname, "utf-8", content.toString());
                Fs.makeExecutable(fname);
            }
            if(windows()) {
                StringBuilder content = new StringBuilder();
                content.append("@echo off\n");
                content.append('\n');
                for(String string : mains.mains()) {
                    content.append("rem java @win-"+filename+"-cp.arg ").append(string).append(" %*\n");
                }
                if(executable != null) content.append("java @win-"+filename+"-cp.arg ").append(executable).append(" %*\n");
                content.append('\n');
                String fname = destination + "/"+filename+".bat";
                Fs.save(fname, "utf-8", content.toString());
            }


            if(unix()) {
                StringBuilder content = new StringBuilder();
                if(!"".equals(shell)) content.append("#!").append(shell).append('\n');
                content.append('\n');
                boolean first = true;
                for(String string : cp) {
                    if(first) {
                        content.append("CLASSPATH=");
                        first = false;
                    } else {
                        content.append("CLASSPATH=$CLASSPATH:");
                    }
                    content.append(string).append('\n');
                }
                content.append("export CLASSPATH\n");
                content.append('\n');
                for(String string : mains.mains()) {
                    content.append("# java ").append(string).append(" \"$@\"\n");
                }
                if(executable != null) content.append("java ").append(executable).append(" \"$@\"\n");
                content.append('\n');
                String fname = destination + "/"+filename+"-old.sh";
                Fs.save(fname, "utf-8", content.toString());
                Fs.makeExecutable(fname);
            }
            if(windows()) {
                StringBuilder content = new StringBuilder();
                content.append("@echo off\n");
                content.append('\n');
                boolean first = true;
                for(String string : cp) {
                    if(first) {
                        content.append("set CLASSPATH=");
                        first = false;
                    } else {
                        content.append("set CLASSPATH=%CLASSPATH%;");
                    }
                    content.append(string.replace('/', '\\')).append('\n');
                }
                content.append('\n');
                for(String string : mains.mains()) {
                    content.append("rem java ").append(string).append(" %*\n");
                }
                if(executable != null) content.append("java ").append(executable).append(" %*\n");
                content.append('\n');
                String fname = destination + "/"+filename+"-old.bat";
                Fs.save(fname, "utf-8", content.toString());
            }

            getLog().info("[script] script generated in " + destination);

        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void merge(List<String> target, List<String> source) {
        if(target == null) return;
        if(source == null) return;
        for(String string : source) {
            if(target.contains(string)) continue;
            target.add(string);
        }
    }

    private static void mergeResouce(List<String> target, List<Resource> source) {
        if(target == null) return;
        if(source == null) return;
        for(Resource string : source) {
            if(target.contains(string.getDirectory())) continue;
            target.add(string.getDirectory());
        }
    }

}
