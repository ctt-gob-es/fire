/*
/*******************************************************************************
 * Copyright (C) 2018 MINHAFP, Gobierno de Espa&ntilde;a
 * This program is licensed and may be used, modified and redistributed under the  terms
 * of the European Public License (EUPL), either version 1.1 or (at your option)
 * any later version as soon as they are approved by the European Commission.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * more details.
 * You should have received a copy of the EUPL1.1 license
 * along with this program; if not, you may find it at
 * http:joinup.ec.europa.eu/software/page/eupl/licence-eupl
 ******************************************************************************/

/** 
 * <b>File:</b><p>es.gob.fire.service.impl.StatisticsServiceImpl.java.</p>
 * <b>Description:</b><p>Service for creating Fire statistics charts.</p>
 * <b>Project:</b><p>Horizontal platform of validation services of multiPKI certificates and electronic signature.</p>
 * <b>Date:</b><p> 22/01/2025.</p>
 * @author Gobierno de España.
 * @version 1.0, 22/01/2025.
 */
package es.gob.fire.service.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import com.aowagie.text.Document;
import com.aowagie.text.DocumentException;
import com.aowagie.text.Element;
import com.aowagie.text.Font;
import com.aowagie.text.FontFactory;
import com.aowagie.text.HeaderFooter;
import com.aowagie.text.PageSize;
import com.aowagie.text.Paragraph;
import com.aowagie.text.Phrase;
import com.aowagie.text.pdf.DefaultFontMapper;
import com.aowagie.text.pdf.PdfContentByte;
import com.aowagie.text.pdf.PdfPCell;
import com.aowagie.text.pdf.PdfPTable;
import com.aowagie.text.pdf.PdfTemplate;
import com.aowagie.text.pdf.PdfWriter;

import es.gob.fire.commons.utils.Constants;
import es.gob.fire.commons.utils.NumberConstants;
import es.gob.fire.i18n.IWebViewMessages;
import es.gob.fire.i18n.Language;
import es.gob.fire.persistence.dto.SignatureDTO;
import es.gob.fire.persistence.dto.TransactionDTO;
import es.gob.fire.service.IStatisticsService;
import es.gob.fire.statistics.GroupedStatistics;
import es.gob.fire.statistics.util.CustomPieSectionLabelGenerator;

/**
 * <p>Service for creating Fire statistics charts.</p>
 * <b>Project:</b><p>Application for monitoring services of @firma suite systems.</p>
 * @version 1.0, 22/01/2025.
 */
@Service
public class StatisticsServiceImpl implements IStatisticsService {

	/**
	 * Attribute that represents two part decimal format
	 */
	private static DecimalFormat df2 = new DecimalFormat("#.##");

	@Override
	public JFreeChart getChartTransactions(final List<TransactionDTO> transactions, final String field) {

		final GroupedStatistics gs = getGSTransactions(transactions, field);

		final JFreeChart chart = ChartFactory.createPieChart(null, getStatPieDataset(gs), true, false, false);

		chart.setBackgroundPaint(Color.WHITE);
		chart.setTextAntiAlias(true);
		chart.setAntiAlias(true);

		// Hides the labels
		final Color transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f);
		final org.jfree.chart.plot.PiePlot plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
		plot.setLabelLinksVisible(Boolean.FALSE);
		plot.setLabelOutlinePaint(transparent);
		plot.setLabelBackgroundPaint(transparent);
		plot.setLabelShadowPaint(transparent);
		final PieSectionLabelGenerator generator = new CustomPieSectionLabelGenerator();
		plot.setLabelGenerator(generator);
		plot.setOutlinePaint(null);
		chart.setBackgroundPaint(Color.WHITE);
		plot.setBackgroundPaint(Color.WHITE);

		chart.getLegend().setItemFont(new java.awt.Font("Arial", Font.NORMAL, 8));
		return chart;
	}

	@Override
	public JFreeChart getChartSignatures(final List<SignatureDTO> signatures, final String field) {

		final GroupedStatistics gs = getGSSignatures(signatures, field);

		final JFreeChart chart = ChartFactory.createPieChart(null, getStatPieDataset(gs), true, false, false);

		chart.setBackgroundPaint(Color.WHITE);
		chart.setTextAntiAlias(true);
		chart.setAntiAlias(true);

		// Hides the labels
		final Color transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f);
		final org.jfree.chart.plot.PiePlot plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
		plot.setLabelLinksVisible(Boolean.FALSE);
		plot.setLabelOutlinePaint(transparent);
		plot.setLabelBackgroundPaint(transparent);
		plot.setLabelShadowPaint(transparent);
		final PieSectionLabelGenerator generator = new CustomPieSectionLabelGenerator();
		plot.setLabelGenerator(generator);
		plot.setOutlinePaint(null);
		chart.setBackgroundPaint(Color.WHITE);
		plot.setBackgroundPaint(Color.WHITE);

		return chart;
	}

	@Override
	public byte[] writeTransStatAsPDF(final int width, final int height, final List<JFreeChart> chartList, final List<TransactionDTO> stats,
			final int tableType, final String title) throws DocumentException {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		final Document document = new Document(PageSize.A4);
		final PdfWriter writer = PdfWriter.getInstance(document, out);

		// Image logo = null;
		// try {
		// logo = Image.getInstance("static/images/cabeceraLogoGobierno.png");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// logo.setAlignment(Image.RIGHT);
		// logo.scaleAbsoluteHeight(20);
		// logo.scaleAbsoluteWidth(20);
		// logo.scalePercent(100);
		// Chunk chunk = new Chunk(logo, 0, -45);
		// HeaderFooter header = new HeaderFooter(new Phrase(chunk), false);

		final HeaderFooter header = new HeaderFooter(
				new Phrase(Language.getResWebFire(IWebViewMessages.STAT_PDF_GENERAL_TITLE),
						FontFactory.getFont("arial", 12, Font.NORMAL, Color.GRAY)),
				false);
		header.setAlignment(Element.ALIGN_LEFT);
		header.setBorderWidth(0);
		document.setHeader(header);

		final HeaderFooter footer = new HeaderFooter(new Phrase("Generado: " + new Date() + " P\u00E1gina: ",
				FontFactory.getFont("arial", 8, Font.NORMAL, Color.GRAY)), true);
		footer.setAlignment(Element.ALIGN_RIGHT);
		footer.setBorderWidth(0);
		document.setFooter(footer);

		document.open();

		document.add(new Paragraph(" "));
		document.add(new Paragraph(title, FontFactory.getFont("arial", 10f, Color.black)));
		document.add(new Paragraph(" "));
		document.add(new Paragraph(" "));

		int offsetx = 0;
		int margin = 0;
		int spacetable = 0;

		if (chartList.size() > NumberConstants.NUM1) {

			margin = 38;
			spacetable = 15;

		} else {

			margin = 115;
			spacetable = 20;

		}

		final PdfContentByte cb = writer.getDirectContent();

		for (final JFreeChart chart : chartList) {

			// get the direct pdf content
			//PdfTemplate tp = cb.createTemplate(width, height);
			final PdfTemplate tp = cb.createTemplate(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			// create an AWT renderer from the pdf template
			final Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());

			final Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);

			chart.draw(g2, r2D, null);

			g2.dispose();
			tp.sanityCheck();
			// add the rendered pdf template to the direct content
			// you will have to play around with this because the chart is
			// absolutely positioned.
			// 38 is just a typical left margin
			// docWriter.getVerticalPosition(true) will approximate the position
			// that the content above the chart ended

			cb.addTemplate(tp, (float)(margin + offsetx), writer.getVerticalPosition(true) - height);

			offsetx += width;

		}

		// add space between chart and table
		for (int i = 0; i < spacetable; i++) {
			document.add(new Paragraph(" "));
		}

		// add table
		switch (tableType) {
			// Tipo basico de correctas e incorrectas
			case NumberConstants.NUM1:
				document.add(getTableBasic(stats, null));
				break;
			// Tipo compuesto (simple o lote) de correctas e incorrectas
			case NumberConstants.NUM2:
				document.add(getTableBasic(stats, null));
				break;
			// Tipo tamanos
			case NumberConstants.NUM3:
				document.add(getTableSizes(stats));
				break;
			default:
				document.add(getTableBasic(stats, null));
		}

		document.close();

		return out.toByteArray();

	}

	@Override
	public byte[] writeTransStatCompositeAsPDF(final int width, final int height, final List<JFreeChart> chartList,
			final List<TransactionDTO> stats, final int tableType, final List<String> titles) throws DocumentException {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		final Document document = new Document(PageSize.A4);
		final PdfWriter writer = PdfWriter.getInstance(document, out);

		// Image logo = null;
		// try {
		// logo = Image.getInstance("static/images/cabeceraLogoGobierno.png");
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// logo.setAlignment(Image.RIGHT);
		// logo.scaleAbsoluteHeight(20);
		// logo.scaleAbsoluteWidth(20);
		// logo.scalePercent(100);
		// Chunk chunk = new Chunk(logo, 0, -45);
		// HeaderFooter header = new HeaderFooter(new Phrase(chunk), false);

		final HeaderFooter header = new HeaderFooter(
				new Phrase(Language.getResWebFire(IWebViewMessages.STAT_PDF_GENERAL_TITLE),
						FontFactory.getFont("arial", 12, Font.NORMAL, Color.GRAY)),
				false);
		header.setAlignment(Element.ALIGN_RIGHT);
		header.setBorderWidth(0);
		document.setHeader(header);

		final HeaderFooter footer = new HeaderFooter(new Phrase("Generado: " + new Date() + " P\u00E1gina: ",
				FontFactory.getFont("arial", 8, Font.NORMAL, Color.GRAY)), true);
		footer.setAlignment(Element.ALIGN_RIGHT);
		footer.setBorderWidth(0);
		document.setFooter(footer);

		document.open();

		int offsetx = 0;

		final PdfContentByte cb = writer.getDirectContent();

		document.add(new Paragraph(" "));
		document.add(new Paragraph(titles.get(NumberConstants.NUM0), FontFactory.getFont("arial", 10f, Color.black)));
		document.add(new Paragraph(" "));
		document.add(new Paragraph(" "));

		JFreeChart chart = null;

		// Transacciones simples por aplicaci\u00f3n
		for (int i = 0; i < 2; i++) {

			chart = chartList.get(i);

			// get the direct pdf content
			//PdfTemplate tp = cb.createTemplate(width, height);
			final PdfTemplate tp = cb.createTemplate(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			// create an AWT renderer from the pdf template
			final Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());

			final Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);

			chart.draw(g2, r2D, null);

			g2.dispose();
			tp.sanityCheck();
			// add the rendered pdf template to the direct content
			// you will have to play around with this because the chart is
			// absolutely positioned.
			// 38 is just a typical left margin
			// docWriter.getVerticalPosition(true) will approximate the position
			// that the content above the chart ended

			cb.addTemplate(tp, (float)(38 + offsetx), writer.getVerticalPosition(true) - height);

			offsetx += width;

		}

		// add space between chart and table
		for (int i = 0; i < 15; i++) {
			document.add(new Paragraph(" "));
		}
		// Tabla de transacciones simples
		document.add(getTableSimple(stats));

		// Las transacciones lote iran a una nueva pagina
		document.newPage();

		document.add(new Paragraph(" "));
		document.add(new Paragraph(titles.get(NumberConstants.NUM1), FontFactory.getFont("arial", 10f, Color.black)));
		document.add(new Paragraph(" "));
		document.add(new Paragraph(" "));

		offsetx = 0;

		// Transacciones lote correctas por aplicaci\u00f3n
		for (int i = 2; i < 4; i++) {

			chart = chartList.get(i);

			// get the direct pdf content
			//PdfTemplate tp = cb.createTemplate(width, height);
			final PdfTemplate tp = cb.createTemplate(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			// create an AWT renderer from the pdf template
			final Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());

			final Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);

			chart.draw(g2, r2D, null);

			g2.dispose();
			tp.sanityCheck();
			// add the rendered pdf template to the direct content
			// you will have to play around with this because the chart is
			// absolutely positioned.
			// 38 is just a typical left margin
			// docWriter.getVerticalPosition(true) will approximate the position
			// that the content above the chart ended

			cb.addTemplate(tp, (float)(38 + offsetx), writer.getVerticalPosition(true) - height);

			offsetx += width;

		}

		// add space between chart and table
		for (int i = 0; i < 15; i++) {
			document.add(new Paragraph(" "));
		}
		// Tabla de transacciones lote
		document.add(getTableLote(stats));

		document.close();

		return out.toByteArray();
	}

	/**
	 * @param stats
	 * @return
	 */
	private PdfPTable getTableBasic(final List<TransactionDTO> statsTrans, final List<SignatureDTO> statsSig) {

		final PdfPTable table = new PdfPTable(4);

		PdfPCell c1 = new PdfPCell(new Phrase("Nombre", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Correctas", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Incorrectas", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Totales", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		table.setHeaderRows(1);

		int totalCorrectas = 0;
		int totalIncorrectas = 0;
		int totalTotales = 0;

		if (statsTrans != null) {

			for (final TransactionDTO trans : statsTrans) {

				table.addCell(new Phrase(trans.getName(), FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
				table.addCell(new Phrase(trans.getCorrects().toString(),
						FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
				table.addCell(new Phrase(trans.getIncorrects().toString(),
						FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
				table.addCell(new Phrase(trans.getTotal().toString(),
						FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));

				totalCorrectas += trans.getCorrects();
				totalIncorrectas += trans.getIncorrects();
				totalTotales += trans.getTotal();
			}

		} else if (statsSig != null) {

			for (final SignatureDTO sig : statsSig) {

				table.addCell(new Phrase(sig.getName(), FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
				table.addCell(new Phrase(sig.getCorrects().toString(),
						FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
				table.addCell(new Phrase(sig.getIncorrects().toString(),
						FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
				table.addCell(new Phrase(sig.getTotal().toString(),
						FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));

				totalCorrectas += sig.getCorrects();
				totalIncorrectas += sig.getIncorrects();
				totalTotales += sig.getTotal();
			}

		}

		PdfPCell c2 = new PdfPCell(new Phrase("Totales", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setHorizontalAlignment(Element.ALIGN_CENTER);
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalCorrectas).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setHorizontalAlignment(Element.ALIGN_LEFT);
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalIncorrectas).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalTotales).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		return table;

	}

	/**
	 * @param stats
	 * @return
	 */
	private PdfPTable getTableSizes(final List<TransactionDTO> stats) {

		final PdfPTable table = new PdfPTable(2);

		PdfPCell c1 = new PdfPCell(new Phrase("Nombre", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Megabyte", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		table.setHeaderRows(1);

		double totalSize = 0;

		for (final TransactionDTO trans : stats) {

			table.addCell(new Phrase(trans.getName(), FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
			table.addCell(new Phrase(String.valueOf(trans.getSizeBytes()),
					FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));

			totalSize += trans.getSizeBytes();
		}

		PdfPCell c2 = new PdfPCell(new Phrase("Totales", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setHorizontalAlignment(Element.ALIGN_CENTER);
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(
				new Phrase(String.valueOf(df2.format(totalSize)), FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		return table;

	}

	private PdfPTable getTableSimple(final List<TransactionDTO> stats) {

		final PdfPTable table = new PdfPTable(4);

		PdfPCell c1 = new PdfPCell(new Phrase("Nombre", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Correctas", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Incorrectas", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Totales", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		table.setHeaderRows(1);

		int totalCorrectasSimple = 0;
		int totalIncorrectasSimple = 0;
		int totalTotalesSimple = 0;

		for (final TransactionDTO trans : stats) {

			table.addCell(new Phrase(trans.getName(), FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
			// Simple
			table.addCell(new Phrase(trans.getCorrectSimpleSignatures().toString(),
					FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
			table.addCell(new Phrase(trans.getIncorrectSimpleSignatures().toString(),
					FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
			table.addCell(new Phrase(trans.getTotalSimple().toString(),
					FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));

			totalCorrectasSimple += trans.getCorrectSimpleSignatures();
			totalIncorrectasSimple += trans.getIncorrectSimpleSignatures();
			totalTotalesSimple += trans.getTotalSimple();

		}

		PdfPCell c2 = new PdfPCell(new Phrase("Totales", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setHorizontalAlignment(Element.ALIGN_CENTER);
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalCorrectasSimple).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setHorizontalAlignment(Element.ALIGN_LEFT);
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalIncorrectasSimple).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalTotalesSimple).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		return table;
	}

	private PdfPTable getTableLote(final List<TransactionDTO> stats) {

		final PdfPTable table = new PdfPTable(4);

		PdfPCell c1 = new PdfPCell(new Phrase("Nombre", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Correctas", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Incorrectas", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Totales", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setGrayFill(0.6f);
		table.addCell(c1);

		table.setHeaderRows(1);

		int totalCorrectasLote = 0;
		int totalIncorrectasLote = 0;
		int totalTotalesLote = 0;

		for (final TransactionDTO trans : stats) {

			table.addCell(new Phrase(trans.getName(), FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
			// Lote
			table.addCell(new Phrase(trans.getCorrectBatchSignatures().toString(),
					FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
			table.addCell(new Phrase(trans.getIncorrectBatchSignatures().toString(),
					FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));
			table.addCell(new Phrase(trans.getTotalBatch().toString(),
					FontFactory.getFont("arial", 10, Font.NORMAL, Color.black)));

			totalCorrectasLote += trans.getCorrectBatchSignatures();
			totalIncorrectasLote += trans.getIncorrectBatchSignatures();
			totalTotalesLote += trans.getTotalBatch();

		}

		PdfPCell c2 = new PdfPCell(new Phrase("Totales", FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setHorizontalAlignment(Element.ALIGN_CENTER);
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalCorrectasLote).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setHorizontalAlignment(Element.ALIGN_LEFT);
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalIncorrectasLote).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		c2 = new PdfPCell(new Phrase(new Integer(totalTotalesLote).toString(),
				FontFactory.getFont("arial", 10, Font.BOLD, Color.black)));
		c2.setGrayFill(0.6f);
		table.addCell(c2);

		return table;
	}


	/**
	 * @param transactions
	 * @param field
	 * @return
	 */
	private GroupedStatistics getGSTransactions(final List<TransactionDTO> transactions, final String field) {

		final GroupedStatistics gs = new GroupedStatistics();
		final LinkedHashMap<String, Long> values = new LinkedHashMap<>();
		final LinkedHashMap<String, Double> valuesSize = new LinkedHashMap<>();

		int index = 0;
		long otrasValue = 0;
		double otrasSize = 0;

		if (Constants.CORRECTAS.equals(field)) {

			final Comparator<TransactionDTO> transCorrectComparator = Comparator.comparing(TransactionDTO::getCorrects);
			Collections.sort(transactions, transCorrectComparator.reversed());

			for (final TransactionDTO t : transactions) {

				if (index < 9) {

					values.put(t.getName(), t.getCorrects().longValue());

				} else {

					if (values.get(Constants.OTRAS) != null) {
						otrasValue += Long.valueOf(t.getCorrects());
					}

					values.put(Constants.OTRAS, otrasValue);
				}

				index++;
			}

			gs.setValues(values);

		} else if (Constants.INCORRECTAS.equals(field)) {

			final Comparator<TransactionDTO> transIncorrectComparator = Comparator.comparing(TransactionDTO::getIncorrects);

			Collections.sort(transactions, transIncorrectComparator.reversed());

			for (final TransactionDTO t : transactions) {

				if (index < 9) {

					values.put(t.getName(), t.getIncorrects().longValue());

				} else {

					if (values.get(Constants.OTRAS) != null) {
						otrasValue += Long.valueOf(t.getIncorrects());
					}

					values.put(Constants.OTRAS, otrasValue);
				}

				index++;

			}

			gs.setValues(values);

		} else if (Constants.SIZE.equals(field)) {

			final Comparator<TransactionDTO> transSizeComparator = Comparator.comparing(TransactionDTO::getSizeBytes);

			Collections.sort(transactions, transSizeComparator.reversed());

			for (final TransactionDTO t : transactions) {

				valuesSize.put(t.getName(), t.getSizeBytes());

				if (index < 9) {

					valuesSize.put(t.getName(), t.getSizeBytes());

				} else {

					if (values.get(Constants.OTRAS) != null) {
						otrasSize += Double.valueOf(t.getSizeBytes());
					}

					valuesSize.put(Constants.OTRAS, otrasSize);
				}

				index++;
			}

			gs.setValuesSize(valuesSize);

		} else if (Constants.CORRECTAS_SIMPLE.equals(field)) {

			final Comparator<TransactionDTO> transCorrectComparator = Comparator.comparing(TransactionDTO::getCorrectSimpleSignatures);

			Collections.sort(transactions, transCorrectComparator.reversed());

			for (final TransactionDTO t : transactions) {

				if (index < 9) {

					values.put(t.getName(), t.getCorrectSimpleSignatures().longValue());

				} else {

					if (values.get(Constants.OTRAS) != null) {
						otrasValue += Long.valueOf(t.getCorrectSimpleSignatures());
					}

					values.put(Constants.OTRAS, otrasValue);
				}

				index++;
			}

			gs.setValues(values);

		} else if (Constants.INCORRECTAS_SIMPLE.equals(field)) {

			final Comparator<TransactionDTO> transCorrectComparator = Comparator.comparing(TransactionDTO::getIncorrectSimpleSignatures);

			Collections.sort(transactions, transCorrectComparator.reversed());

			for (final TransactionDTO t : transactions) {

				if (index < 9) {

					values.put(t.getName(), t.getIncorrectSimpleSignatures().longValue());

				} else {

					if (values.get(Constants.OTRAS) != null) {
						otrasValue += Long.valueOf(t.getIncorrectSimpleSignatures());
					}

					values.put(Constants.OTRAS, otrasValue);
				}

				index++;
			}

			gs.setValues(values);

		} else if (Constants.CORRECTAS_LOTE.equals(field)) {

			final Comparator<TransactionDTO> transCorrectComparator = Comparator.comparing(TransactionDTO::getCorrectBatchSignatures);

			Collections.sort(transactions, transCorrectComparator.reversed());

			for (final TransactionDTO t : transactions) {

				if (index < 9) {

					values.put(t.getName(), t.getCorrectBatchSignatures().longValue());

				} else {

					if (values.get(Constants.OTRAS) != null) {
						otrasValue += Long.valueOf(t.getCorrectBatchSignatures());
					}

					values.put(Constants.OTRAS, otrasValue);

				}

				index++;
			}

			gs.setValues(values);

		} else if (Constants.INCORRECTAS_LOTE.equals(field)) {

			final Comparator<TransactionDTO> transCorrectComparator = Comparator.comparing(TransactionDTO::getIncorrectBatchSignatures);

			Collections.sort(transactions, transCorrectComparator.reversed());

			for (final TransactionDTO t : transactions) {

				if (index < 9) {

					values.put(t.getName(), t.getIncorrectBatchSignatures().longValue());

				} else {

					if (values.get(Constants.OTRAS) != null) {
						otrasValue += Long.valueOf(t.getIncorrectBatchSignatures());
					}

					values.put(Constants.OTRAS, otrasValue);

				}

				index++;
			}

			gs.setValues(values);

		}

		return gs;

	}

	/**
	 * @param signatures
	 * @param field
	 * @return
	 */
	private GroupedStatistics getGSSignatures(final List<SignatureDTO> signatures, final String field) {

		final GroupedStatistics gs = new GroupedStatistics();
		final LinkedHashMap<String, Long> values = new LinkedHashMap<>();

		if (Constants.CORRECTAS.equals(field)) {

			final Comparator<SignatureDTO> sigCorrectComparator = Comparator.comparing(SignatureDTO::getCorrects);

			Collections.sort(signatures, sigCorrectComparator);

			for (final SignatureDTO t : signatures) {
				values.put(t.getName(), t.getCorrects().longValue());
			}

			gs.setValues(values);

		} else if (Constants.INCORRECTAS.equals(field)) {

			final Comparator<SignatureDTO> sigIncorrectComparator = Comparator.comparing(SignatureDTO::getIncorrects);

			Collections.sort(signatures, sigIncorrectComparator);

			for (final SignatureDTO t : signatures) {

				values.put(t.getName(), t.getIncorrects().longValue());

			}

			gs.setValues(values);

		}

		return gs;

	}

	/**
	 * Method for generating of the statistics pie data.
	 *
	 * @return Statistics pie data.
	 */
	private DefaultPieDataset getStatPieDataset(final GroupedStatistics statsGrouped) {

		final DefaultPieDataset result = new DefaultPieDataset();

		final TreeMap<String, Long> mapSortLong = new TreeMap<>();
		final TreeMap<String, Double> mapSortDouble = new TreeMap<>();

		if (!statsGrouped.getValues().isEmpty()) {

			mapSortLong.putAll(statsGrouped.getValues());

			final Iterator<String> itKeys = mapSortLong.keySet().iterator();

			while (itKeys.hasNext()) {
				final String key = itKeys.next();
				result.setValue(key, statsGrouped.getValues().get(key));
			}

		} else if (!statsGrouped.getValuesSize().isEmpty()) {

			mapSortDouble.putAll(statsGrouped.getValuesSize());

			final Iterator<String> itKeys = mapSortDouble.keySet().iterator();

			while (itKeys.hasNext()) {
				final String key = itKeys.next();
				result.setValue(key, statsGrouped.getValuesSize().get(key));
			}
		}

		return result;
	}


	@Override
	public byte[] writeSigStatAsPDF(final int width, final int height, final List<JFreeChart> chartList, final List<SignatureDTO> stats, final String title)
			throws DocumentException {

		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		final Document document = new Document(PageSize.A4);
		final PdfWriter writer = PdfWriter.getInstance(document, out);

		final HeaderFooter header = new HeaderFooter(
				new Phrase(Language.getResWebFire(IWebViewMessages.STAT_PDF_GENERAL_TITLE),
						FontFactory.getFont("arial", 12, Font.NORMAL, Color.GRAY)),
				false);
		header.setAlignment(Element.ALIGN_RIGHT);
		header.setBorderWidth(0);
		document.setHeader(header);

		final HeaderFooter footer = new HeaderFooter(new Phrase("Generado: " + new Date() + "P\u00E1gina: ",
				FontFactory.getFont("arial", 8, Font.NORMAL, Color.GRAY)), true);
		footer.setAlignment(Element.ALIGN_RIGHT);
		footer.setBorderWidth(0);
		document.setFooter(footer);

		document.open();

		document.add(new Paragraph(" "));
		document.add(new Paragraph(title, FontFactory.getFont("arial", 10f, Color.black)));
		document.add(new Paragraph(" "));
		document.add(new Paragraph(" "));

		int offsetx = 0;

		final PdfContentByte cb = writer.getDirectContent();
		for (final JFreeChart chart : chartList) {

			// get the direct pdf content
			//PdfTemplate tp = cb.createTemplate(width, height);
			final PdfTemplate tp = cb.createTemplate(PageSize.A4.getWidth(), PageSize.A4.getHeight());
			// create an AWT renderer from the pdf template
			final Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());

			final Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);

			chart.draw(g2, r2D, null);

			g2.dispose();
			tp.sanityCheck();
			// add the rendered pdf template to the direct content
			// you will have to play around with this because the chart is
			// absolutely positioned.
			// 38 is just a typical left margin
			// docWriter.getVerticalPosition(true) will approximate the position
			// that the content above the chart ended

			cb.addTemplate(tp, (float)(38 + offsetx), writer.getVerticalPosition(true) - height);

			offsetx += width;
		}

		// add space between chart and table
		for (int i = 0; i < 15; i++) {
			document.add(new Paragraph(" "));
		}

		document.add(getTableBasic(null, stats));

		document.close();

		return out.toByteArray();
	}


}
