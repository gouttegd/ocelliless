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

import java.util.List;

import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A single ontology test.
 */
public interface ITest {

    /** Base for all annotation properties used by Ocelliless. */
    public static final String OCELLILESS_BASE_IRI = "https://incenp.org/ocelliless/";

    /**
     * IRI for the "target" property.
     * 
     * The "target" annotation property is intended to indicate the class in the
     * tested ontology that a given test is about.
     */
    public static final String OCELLILESS_TARGET_IRI = OCELLILESS_BASE_IRI + "target";

    /**
     * IRI for the "xfail" property.
     * 
     * The "xfail" annotation property indicates that a given test is expected to
     * fail.
     */
    public static final String OCELLILESS_XFAIL_IRI = OCELLILESS_BASE_IRI + "xfail";

    /**
     * IRI for the "test" property.
     * 
     * The "test" annotation property is intended to be added to the tested ontology
     * to point to the location of a test file.
     */
    public static final String OCELLILESS_TESTLOC_IRI = OCELLILESS_BASE_IRI + "test";

    /**
     * Runs the test.
     * 
     * @param ontology The ontology to run the test against.
     * @return True if the test passes, False otherwise.
     */
    public boolean run(OWLOntology ontology);

    /**
     * Gets annotations for the specified ontology.
     * 
     * This method should return annotations intended to be merged with the
     * specified ontology, and indicating the location of this test.
     * 
     * @param ontology The ontology the annotations should be added to. It should
     *                 not be modified, but can be used to extract the base IRI for
     *                 the annotations to create.
     * @return A list of annotations (which may be empty).
     */
    public List<OWLAnnotationAxiom> getAnnotations(OWLOntology ontology);
}
