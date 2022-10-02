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

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class TestDriver {

    public static final String OCELLILESS_BASE_IRI = "https://incenp.org/ocelliless/";
    public static final String OCELLILESS_TARGET_IRI = OCELLILESS_BASE_IRI + "target";
    public static final String OCELLILESS_XFAIL_IRI = OCELLILESS_BASE_IRI + "xfail";
    public static final String OCELLILESS_TESTLOC_IRI = OCELLILESS_BASE_IRI + "test";

    private OWLReasonerFactory reasonerFactory;
    private OWLOntology checkedOntology;

    private List<TargetTestPair> targetsList;

    public TestDriver() {
        reasonerFactory = new ElkReasonerFactory();
        checkedOntology = null;
        targetsList = new ArrayList<TargetTestPair>();
    }

    public void setOntology(OWLOntology ontology) {
        checkedOntology = ontology;
    }

    public boolean runTest(OWLOntology test, String filename) {
        boolean xfail = isExpectedFailure(test);
        IRI target = getTargetIRI(test);
        if (target != null) {
            IRI testIRI = IRI.create(getBaseIRI() + filename);
            targetsList.add(new TargetTestPair(target, testIRI));
        }

        test.getOWLOntologyManager().addAxioms(test, checkedOntology.getAxioms());
        OWLReasoner reasoner = reasonerFactory.createReasoner(test);
        boolean pass = reasoner.isConsistent() &&
            reasoner.getUnsatisfiableClasses().getSize() == 1;
        reasoner.dispose();

        return pass || xfail;
    }

    private boolean isExpectedFailure(OWLOntology test) {
        for (OWLAnnotation annot : test.getAnnotations()) {
            if (annot.getProperty().getIRI().toString().equals(OCELLILESS_XFAIL_IRI)) {
                return annot.getValue().asLiteral().get().getLiteral().equalsIgnoreCase("yes");
            }
        }

        return false;
    }

    private IRI getTargetIRI(OWLOntology test) {
        for (OWLAnnotation annot : test.getAnnotations()) {
            if (annot.getProperty().getIRI().toString().equals(OCELLILESS_TARGET_IRI)) {
                return annot.getValue().asIRI().get();
            }
        }

        return null;
    }

    private String getBaseIRI() {
        String iri = checkedOntology.getOntologyID().getOntologyIRI().get().toString();
        return iri.substring(0, iri.lastIndexOf('/') + 1);
    }

    public List<OWLAnnotationAxiom> getAnnotations() {
        OWLDataFactory factory = checkedOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLAnnotationProperty property = factory
            .getOWLAnnotationProperty(IRI.create(OCELLILESS_TESTLOC_IRI));
        ArrayList<OWLAnnotationAxiom> axioms = new ArrayList<OWLAnnotationAxiom>();

        for (TargetTestPair test : targetsList) {
            OWLAnnotationAxiom axiom = factory.getOWLAnnotationAssertionAxiom(property, test.target,
                test.testfile);
            axioms.add(axiom);
        }

        return axioms;
    }

    private class TargetTestPair {
        IRI target;
        IRI testfile;

        TargetTestPair(IRI target, IRI testfile) {
            this.target = target;
            this.testfile = testfile;
        }
    }
}
