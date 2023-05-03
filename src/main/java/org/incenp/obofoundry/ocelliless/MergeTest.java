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

package org.incenp.obofoundry.ocelliless;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * A test based on the merging of a test ontology.
 * 
 * This tests merges a (presumably small) ontology with the ontology to be
 * tested, then checks whether the merged ontology is consistent and does not
 * contain any unsatisfiable classes.
 */
public class MergeTest implements ITest {

    private OWLOntology test;
    private OWLReasonerFactory reasonerFactory;
    private String baseName;

    /**
     * Creates a new instance.
     * 
     * @param ontology        The ontology to merge with the ontology to be tested.
     * @param reasonerFactory The OWLReasonerFactory to use to create the reasoner
     *                        to test the ontology’s consistency.
     * @param baseName        The base filename of the test ontology.
     */
    public MergeTest(OWLOntology ontology, OWLReasonerFactory reasonerFactory, String baseName) {
        this.test = ontology;
        this.reasonerFactory = reasonerFactory;
        this.baseName = baseName;
    }

    public boolean run(OWLOntology ontology) {
        boolean xfail = isExpectedFailure();

        test.getOWLOntologyManager().addAxioms(test, ontology.getAxioms());
        OWLReasoner reasoner = reasonerFactory.createReasoner(test);
        boolean pass = reasoner.isConsistent() && reasoner.getUnsatisfiableClasses().getSize() == 1;
        reasoner.dispose();

        return pass || xfail;
    }

    public List<OWLAnnotationAxiom> getAnnotations(OWLOntology ontology) {
        OWLDataFactory factory = test.getOWLOntologyManager().getOWLDataFactory();
        OWLAnnotationProperty property = factory.getOWLAnnotationProperty(IRI.create(OCELLILESS_TESTLOC_IRI));
        ArrayList<OWLAnnotationAxiom> axioms = new ArrayList<OWLAnnotationAxiom>();
        String baseIRI = getOntologyBaseIRI(ontology);

        for ( OWLAnnotation annot : test.getAnnotations() ) {
            if ( annot.getProperty().getIRI().toString().equals(OCELLILESS_TARGET_IRI) ) {
                OWLAnnotationAxiom axiom = factory.getOWLAnnotationAssertionAxiom(property,
                        annot.getValue().asIRI().get(), IRI.create(baseIRI + baseName));
                axioms.add(axiom);
            }
        }

        return axioms;
    }

    /**
     * Indicates whether this test is expected to fail.
     * 
     * This method look for a “xfail” annotation in the test ontology. If such an
     * annotation is present and contains the literal value “yes”, then this test is
     * expected to fail. That is, the merged ontology is supposed to be inconsistent
     * or to contain unsatisfiable classes.
     * 
     * @return True if the test is expected to fail, False otherwise.
     */
    protected boolean isExpectedFailure() {
        for ( OWLAnnotation annot : test.getAnnotations() ) {
            if ( annot.getProperty().getIRI().toString().equals(OCELLILESS_XFAIL_IRI) ) {
                return annot.getValue().asLiteral().get().getLiteral().equalsIgnoreCase("yes");
            }
        }

        return false;
    }

    private String getOntologyBaseIRI(OWLOntology ontology) {
        String iri = ontology.getOntologyID().getOntologyIRI().get().toString();
        return iri.substring(0, iri.lastIndexOf('/') + 1);
    }
}
