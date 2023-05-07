package org.incenp.obofoundry.ocelliless;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.obolibrary.robot.Command;
import org.obolibrary.robot.CommandLineHelper;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(RobotCommand.class);

    private Options options;

    public RobotCommand() {
        options = CommandLineHelper.getCommonOptions();
        options.addOption("i", "input", true, "load ontology from file");
        options.addOption("t", "test", true, "load test from file");
        options.addOption("c", "component", true, "save annotations to file");
        options.addOption("r", "reasoner", true, "reasoner to use");
        options.addOption("x", "abort", false, "abort pipeline if a test fails");
    }

    public String getName() {
        return "ocelliless";
    }

    public String getDescription() {
        return "run reasoning-based unit tests against an ontology";
    }

    public String getUsage() {
        return "robot ocelliless -i <INPUT> -t <TEST...> [-c COMPONENT]";
    }

    public Options getOptions() {
        return options;
    }

    public void main(String[] args) {
        try {
            execute(null, args);
        } catch ( Exception e ) {
            CommandLineHelper.handleException(e);
        }
    }

    public CommandState execute(CommandState state, String[] args) throws Exception {
        CommandLine line = CommandLineHelper.getCommandLine(getUsage(), options, args);
        if ( line == null ) {
            return null;
        }

        if ( state == null ) {
            state = new CommandState();
        }
        IOHelper ioHelper = CommandLineHelper.getIOHelper(line);
        state = CommandLineHelper.updateInputOntology(ioHelper, state, line);
        
        if ( !line.hasOption('t')) {
            throw new IllegalArgumentException("No test specified (missing -t option(s))");
        }
        
        OWLReasonerFactory factory = CommandLineHelper.getReasonerFactory(line);
        List<OWLAnnotationAxiom> annotations = new ArrayList<OWLAnnotationAxiom>();

        String[] tests = line.getOptionValues('t');
        int passed = 0;
        for (int i = 0; i < tests.length; i++) {
            String result;
            
            try {
                OWLOntology testOntology = ioHelper.loadOntology(tests[i]);
                ITest test = new MergeTest(testOntology, factory, tests[i]);
                if ( test.run(state.getOntology()) ) {
                    result = "PASS";
                    passed += 1;
                } else {
                    result = "FAIL";
                }

                annotations.addAll(test.getAnnotations(state.getOntology()));
            } catch ( Exception e ) {
                result = "ERROR";
            }

            logger.info("Test {}/{} ({}): {}", i + 1, tests.length, tests[i], result);
        }

        logger.info("{}/{} tests passed", passed, tests.length);

        if ( line.hasOption('c') && annotations.size() > 0 ) {
            String componentFile = line.getOptionValue('c');
            logger.info("Saving annotations component to {}", componentFile);

            OWLOntologyManager mgr = state.getOntology().getOWLOntologyManager();
            OWLOntology component = mgr.createOntology();
            for ( OWLAnnotationAxiom axiom : annotations ) {
                mgr.addAxiom(component, axiom);
            }
            File f = new File(componentFile);
            component.saveOntology(new FunctionalSyntaxDocumentFormat(), new FileOutputStream(f));
        }

        if ( passed < tests.length && line.hasOption('x') ) {
            throw new Exception("Some tests failed");
        }

        return state;
    }

}
