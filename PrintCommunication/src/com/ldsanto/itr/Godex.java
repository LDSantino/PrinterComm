package com.ldsanto.itr;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.print.PrintException;
import javax.print.PrintService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class Godex {
	public Godex() {
		super();
	}

	public void print(String filepath, PrintService pservice, int copie) throws PrintException, IOException {
		PDDocument doc = PDDocument.load(new File(filepath));
		PrinterJob job = PrinterJob.getPrinterJob();
	    job.setPageable(new PDFPageable(doc));

	    // define custom paper
	    Paper paper = new Paper();
	    //paper.setSize(306, 396); // 1/72 inch
	    //paper.setSize(595, 842); // 1/72 inch
	    //paper.setImageableArea(0, 0, 354, 128); // no margins
	    //paper.setSize(doc.getPage(0).getCropBox().getWidth(), doc.getPage(0).getCropBox().getHeight()); // 1/72 inch
	    paper.setSize(334.49f, 121.88f);
	    paper.setImageableArea(10, 10, doc.getPage(0).getCropBox().getWidth()+10, doc.getPage(0).getCropBox().getHeight()+10); // no margins

	    // custom page format
	    PageFormat pageFormat = new PageFormat();
	    pageFormat.setPaper(paper);
	    
	    // override the page format
	    Book book = new Book();
	    // append all pages
	    book.append(new PDFPrintable(doc), pageFormat, doc.getNumberOfPages());
	    for (int i = 0; i<copie; i++) {
	    	try {
	    		job.setPrintService(pservice);
	    	} catch (PrinterException e1) {
	    		// TODO Auto-generated catch block
	    		e1.printStackTrace();
	    	}
		}
	    job.setPageable(book);
	    job.setCopies(copie);
	    try {
			job.print();
		} catch (PrinterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void checkprint(String filepath) throws PrintException, IOException {
		PDDocument doc = PDDocument.load(new File(filepath));
	    System.out.println(doc.getPage(0).getCropBox().getWidth());
	    System.out.println(doc.getPage(0).getCropBox().getHeight());
	    System.out.println(doc.getNumberOfPages());
		PrinterJob job = PrinterJob.getPrinterJob();
	    job.setPageable(new PDFPageable(doc));

	    // define custom paper
	    Paper paper = new Paper();
	    paper.setSize(334.49f, 121.88f);
	    paper.setImageableArea(0, 0, doc.getPage(0).getCropBox().getWidth(), doc.getPage(0).getCropBox().getHeight()); // no margins

	    // custom page format
	    PageFormat pageFormat = new PageFormat();
	    pageFormat.setPaper(paper);
	    System.out.println(pageFormat.getHeight());
	    System.out.println(pageFormat.getWidth());
	    
	    // override the page format
	    Book book = new Book();
	    // append all pages
	    book.append(new PDFPrintable(doc), pageFormat, doc.getNumberOfPages());
	    System.out.println(book.getPageFormat(0).getHeight());
	    System.out.println(book.getPageFormat(0).getWidth());
	}
	
	public String reducePDF(String filepath) {
		float width = 334.49f;
		float height = 121.89f;
		float tolerance = 1f;

		PdfReader reader = null;
		try {
			reader = new PdfReader(filepath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int i = 1; i <= reader.getNumberOfPages(); i++)
		{
		    Rectangle cropBox = reader.getCropBox(i);
		    float widthToAdd = width - cropBox.getWidth();
		    float heightToAdd = height - cropBox.getHeight();
		    if (Math.abs(widthToAdd) > tolerance || Math.abs(heightToAdd) > tolerance)
		    {
		    	/*float[] newBoxValues = new float[] { 
		            cropBox.getLeft() - widthToAdd / 2,
		            cropBox.getBottom() - heightToAdd / 2,
		            cropBox.getRight() + widthToAdd / 2,
		            cropBox.getTop() + heightToAdd / 2
		        };*/
		    	float[] newBoxValues = new float[] { 
			            0,
			            cropBox.getTop() - height,
			            width,
			            cropBox.getTop()
			        };
		        PdfArray newBox = new PdfArray(newBoxValues);

		        PdfDictionary pageDict = reader.getPageN(i);
		        pageDict.put(PdfName.CROPBOX, newBox);
		        pageDict.put(PdfName.MEDIABOX, newBox);
		    }
		}

		PdfStamper stamper = null;
		try {
			stamper = new PdfStamper(reader, new FileOutputStream(filepath.replace(".pdf","red.pdf")));
		} catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			stamper.close();
		} catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filepath.replace(".pdf","red.pdf");
	}
}
