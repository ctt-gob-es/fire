// Copyright (C) 2018, Gobierno de España
// This program is licensed and may be used, modified and redistributed under the terms
// of the European Public License (EUPL), either version 1.1 or (at your
// option) any later version as soon as they are approved by the European Commission.
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and
// more details.
// You should have received a copy of the EUPL1.1 license
// along with this program; if not, you may find it at
// http://joinup.ec.europa.eu/software/page/eupl/licence-eupl

/** 
 * <b>File:</b><p>es.gob.signaturereport.controller.utils.CustomPieSectionLabelGenerator.java.</p>
 * <b>Description:</b><p> Class label pie generation.</p>
 * <b>Project:</b><p>Horizontal platform to generation signature reports in legible format.</p>
 * <b>Date:</b><p>02/03/2012.</p>
 * @author Spanish Government.
 * @version 1.0, 02/03/2012.
 */
package es.gob.fire.statistics.util;

import java.text.AttributedString;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.data.general.PieDataset;


/** 
 * <p>Class label pie generation.</p>
 * <b>Project:</b><p>Horizontal platform to generation signature reports in legible format.</p>
 * @version 1.0, 02/03/2012.
 */
public class CustomPieSectionLabelGenerator implements PieSectionLabelGenerator {

	/**
	 * {@inheritDoc}
	 * @see org.jfree.chart.labels.PieSectionLabelGenerator#generateAttributedSectionLabel(org.jfree.data.general.PieDataset, java.lang.Comparable)
	 */
	public AttributedString generateAttributedSectionLabel(PieDataset arg0, @SuppressWarnings("rawtypes") Comparable arg1) {
		return new AttributedString("");
	}

	/**
	 * {@inheritDoc}
	 * @see org.jfree.chart.labels.PieSectionLabelGenerator#generateSectionLabel(org.jfree.data.general.PieDataset, java.lang.Comparable)
	 */
	public String generateSectionLabel(PieDataset arg0, @SuppressWarnings("rawtypes") Comparable arg1) {
		return "";
	}

}
