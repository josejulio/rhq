/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.rhq.enterprise.remoting.cli;

import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.XmlClass;
import org.testng.TestNG;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

public class ScriptTestRunner {

    private File scriptDir = new File(System.getProperty("script.dir"));

    private String outputDir = System.getProperty("test.output.dir");

    private String testName = System.getProperty("test");

    private boolean singleTestMode;

    private FilenameFilter scriptFilter;

    public ScriptTestRunner() {
        singleTestMode = testName != null && !testName.isEmpty() && !testName.equals("${test}");

        if (singleTestMode) {
            scriptFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(testName);
                }
            };
        }
        else {
            scriptFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".js");
                }
            };
        }
    }

    public void execute() throws IOException {
        Collection<Script> scripts = findScripts();
        XmlSuite suite = createSuite();

        for (Script script : scripts) {
            addTestToSuite(suite, script);
        }

        runSuite(suite);
    }

    private Collection<Script> findScripts() throws IOException {
        return findScripts(scriptDir);
    }

//    private List<File> findScripts(File dir) {
//        List<File> scripts = new ArrayList<File>();
//        File[] paths = dir.listFiles();
//        List<File> dirs = new ArrayList<File>();
//
//        for (File path : paths) {
//            if (path.isDirectory() && !path.isHidden()) {
//                dirs.add(path);
//            }
//            else if (scriptFilter.accept(dir, path.getAbsolutePath())) {
//                scripts.add(path);
//                if (singleTestMode) {
//                    return scripts;
//                }
//            }
//        }
//
//        for (File subdir : dirs) {
//            scripts.addAll(findScripts(subdir));
//        }
//
//        return scripts;
//    }

    private List<Script> findScripts(File dir) throws IOException {
        List<Script> scripts = new ArrayList<Script>();
        File[] paths = dir.listFiles();
        List<File> dirs = new ArrayList<File>();

        for (File path : paths) {
            if (path.isDirectory() && !path.isHidden()) {
                dirs.add(path);
            }
            else if (scriptFilter.accept(dir, path.getAbsolutePath())) {
                Script script = new Script();
                script.srcFile = path;
                script.args = findScriptArgs(path);

                scripts.add(script);

                if (singleTestMode) {
                    return scripts;
                }
            }
        }

        for (File subdir : dirs) {
            scripts.addAll(findScripts(subdir));
        }

        return scripts;
    }

    private String findScriptArgs(final File script) throws IOException {
        File parentDir = script.getParentFile();

        File[] argsFiles = parentDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".args") && baseNamesAreSame(script.getName(), name);
            }
        });

        if (argsFiles.length > 0) {
            return IOUtils.toString(new FileReader(argsFiles[0]));
        }

        return null;
    }

    private boolean baseNamesAreSame(String scriptFileName, String scriptArgsFileName) {
        return FilenameUtils.getBaseName(scriptFileName).equals(FilenameUtils.getBaseName(scriptArgsFileName));
    }

    private XmlSuite createSuite() {
        XmlSuite suite = new XmlSuite();
        suite.setName("Command line suite");
        return suite;
    }

    private void addTestToSuite(XmlSuite suite, Script script) {
        XmlTest test = new XmlTest(suite);
        test.setName(FilenameUtils.getBaseName(script.srcFile.getAbsolutePath()));
        test.addParameter("script", script.srcFile.getAbsolutePath());
        if (script.args != null) {
            test.addParameter("args", script.args);
        }
        
        List<XmlClass> classes = new ArrayList<XmlClass>();
        classes.add(new XmlClass(ScriptTest.class));
        test.setXmlClasses(classes);
    }

    private void runSuite(XmlSuite suite) {
        List<XmlSuite> suites = new LinkedList<XmlSuite>();
        suites.add(suite);

        TestNG testNG = new TestNG();
        testNG.setOutputDirectory(outputDir);
        testNG.setXmlSuites(suites);

        testNG.run();
    }

    private static class Script {
        public File srcFile;

        public String args;
    }

    public static void main(String[] args) throws Exception {
        ScriptTestRunner runner = new ScriptTestRunner();
        runner.execute();
    }

}
