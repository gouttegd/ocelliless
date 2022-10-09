/*
 * Ocelliless - Tests driver for OWL ontologies
 * Copyright © 2022 Damien Goutte-Gattat
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.ocelliless;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class Program {

    private final static String name = "ocelliless";
    private final static int NO_ERROR = 0;
    private final static int COMMAND_LINE_ERROR = 1;
    private final static int ONTOLOGY_LOADING_ERROR = 2;
    private final static int ONTOLOGY_WRITING_ERROR = 3;
    private final static int FAILED_TESTS_ERROR = 4;

    private OWLOntologyManager ontologyManager = null;

    public Program() {
    }

    private void print(PrintStream stream, String format, Object... args) {
        stream.printf("%s: ", name);
        stream.printf(format, args);
        stream.print('\n');
    }

    private void info(String format, Object... args) {
        print(System.out, format, args);
    }

    private void error(int exitCode, String format, Object... args) {
        print(System.err, format, args);
        System.exit(exitCode);
    }

    private OWLOntologyManager getManager() {
        if (ontologyManager == null) {
            ontologyManager = OWLManager.createOWLOntologyManager();
        }
        return ontologyManager;
    }

    private Options getOptions() {
        Options opts = new Options();
        opts.addOption(Option.builder("i")
            .longOpt("input")
            .hasArg()
            .argName("ONTOLOGY")
            .desc("The ontology to check.")
            .build());

        opts.addOption(Option.builder("c")
            .longOpt("component")
            .hasArg()
            .argName("FILE")
            .desc("Save annotations to a new component.")
            .build());

        opts.addOption(Option.builder("v")
            .longOpt("version")
            .desc("Print version information.")
            .build());
        opts.addOption(Option.builder("h")
            .longOpt("help")
            .desc("Print the help message.")
            .build());

        return opts;
    }

    private CommandLine parseArguments(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            showHelp(options, e.getMessage());
        }

        if (cmd.hasOption('h')) {
            showHelp(options, null);
        }
        else if (cmd.hasOption('v')) {
            System.out.println(String.format("Ocelliless %s\n" +
                "Copyright © 2022 Damien Goutte-Gattat\n\n" +
                "This program is released under the GNU General Public License.\n", getVersion()));
            System.exit(NO_ERROR);
        }
        else if (!cmd.hasOption('i')) {
            showHelp(options, "Missing required option -i");
        }
        else if (cmd.getArgs().length == 0) {
            showHelp(options, "Missing unit tests");
        }

        return cmd;
    }

    private String getVersion() {
        return Program.class.getPackage().getImplementationVersion();
    }

    private void showHelp(Options options, String errorMessage) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ocelliless [options] -i <ONTOLOGY> <TEST...>",
            "Run unit tests on an ontology\n\nOptions:", options,
            "\nReport bugs to Damien Goutte-gattat <dgouttegattat@incenp.org>\n\n");

        if (errorMessage != null) {
            error(COMMAND_LINE_ERROR, "Invalid command line: %s", errorMessage);
        }

        System.exit(NO_ERROR);
    }

    private boolean process(String inputOntology, String[] tests, String componentFile) {
        OWLOntology ontology = loadOntology(inputOntology, true);
        ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
        List<OWLAnnotationAxiom> annotations = new ArrayList<OWLAnnotationAxiom>();

        int passed = 0;
        for (int i = 0; i < tests.length; i++) {
            OWLOntology testOntology = loadOntology(tests[i], false);
            String result;
            if (testOntology == null) {
                result = "ERROR";
            }
            else {
                ITest test = new MergeTest(testOntology, reasonerFactory, tests[i]);
                if (test.run(ontology)) {
                    result = "PASS";
                    passed += 1;
                } else {
                    result = "FAIL";
                }

                annotations.addAll(test.getAnnotations(ontology));
            }

            info("Test %d/%d (%s): %s", i + 1, tests.length, tests[i], result);
        }
        
        info("%s: %d/%d tests passed", inputOntology, passed, tests.length);

        
        if (componentFile != null) {
            if (annotations.size() > 0 ) {
                info("Saving annotations component to %s", componentFile);
                
                OWLOntology component;
                try {
                    component = getManager().createOntology();
                    
                    for (OWLAnnotationAxiom axiom: annotations) {
                        getManager().addAxiom(component, axiom);
                    }
                    File f = new File(componentFile);
                    component.saveOntology(new FunctionalSyntaxDocumentFormat(), new FileOutputStream(f));
                }
                catch (OWLOntologyCreationException e) {
                    error(ONTOLOGY_WRITING_ERROR, "Cannot create annotations component: %s",
                        e.getMessage());
                }
                catch (OWLOntologyStorageException e) {
                    error(ONTOLOGY_WRITING_ERROR, "Cannot save annotations component: %s",
                        e.getMessage());
                } catch (FileNotFoundException e) {
                    error(ONTOLOGY_WRITING_ERROR, "Cannot write annotations component to %s: %s",
                        componentFile, e.getMessage());
                }
            }
        }
        
        return passed == tests.length;
    }

    private OWLOntology loadOntology(String filename, boolean exitOnError) {
        File f = new File(filename);
        OWLOntology ont = null;

        try {
            ont = getManager().loadOntologyFromOntologyDocument(f);
        } catch (Exception e) {
            if (exitOnError) {
                error(ONTOLOGY_LOADING_ERROR, "Cannot load ontology %s: %s", filename,
                    e.getMessage());
            }
        }

        return ont;
    }

    public static void main(String[] args) {
        Program p = new Program();
        CommandLine cmd = p.parseArguments(args);

        System.setProperty("log4j.configuration", "ocelliless-log4j.properties");

        boolean ok = p.process(cmd.getOptionValue('i'), cmd.getArgs(),
            cmd.getOptionValue('c', null));
        System.exit(ok ? NO_ERROR : FAILED_TESTS_ERROR);
    }
}
