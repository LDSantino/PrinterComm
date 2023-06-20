package com.ldsanto.itr;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.internal.ZebraPrinterZpl;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.print.PrintException;
import javax.print.PrintService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class Zebra {

	protected Zebra(){
		super();
	}

	public void convert(String filepath, PDDocument doc, int dpi) {

		PDFRenderer pdfRenderer = new PDFRenderer(doc);
		BufferedImage bim = null;

		//Ciclo che scandisce pagina per pagina il documento
		for (int page = 0; page < doc.getNumberOfPages(); ++page){ 
			try {
				//effettuiamo un tentativo di creazione di una BuffferedImage
				bim = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.BINARY);
			} catch (IOException e) {
				//lancio la stampa dello stack di tracciamento nel caso di IOException
				e.printStackTrace();
				//esco dal ciclo
				//break;
			}
			try {
				//Se siamo riusciti a creare la BufferedImage scriviamo l'immagine su un file specifico
				ImageIOUtil.writeImage(bim, filepath.replace(".pdf", "-") + (page+1) + ".png", dpi);
			} catch (IOException e) {
				//lancio la stampa dello stack di tracciamento nel caso di IOException
				e.printStackTrace();
				//esco dal ciclo
				//break;
			}
		}
	} 


	public void print(String filepath, PrintService pservice, int copie) throws PrintException, IOException, SQLException {
		long startTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()); //variabile inizializzata per avere il tempo di avvio dell'applicativo
		long elapsedTime; //variabile utilizzata per calcolare in seonda sede il tempo passato

		DPC driverPrinterConnection = null; //variabile che gestisce la connesisone con la stampante
		PDDocument doc = null; //variabile legata alla gestione del docume

		System.out.println("Starting!");

		try {
			driverPrinterConnection = new DPC(pservice.getName());
			driverPrinterConnection.open();
		} catch (ConnectionException e) {
			//lancio la stampa dello stack di tracciamento nel caso di ConnectionException
			e.printStackTrace();
			//esco dal ciclo
			//break;
		}

		if (driverPrinterConnection.isConnected()){
			//elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;
			//System.out.println("1. Elapsed Time: " + elapsedTime);
			try {
				//Carico il file nella variabile creata precedentemente
				doc = PDDocument.load(new File(filepath));
			} catch (IOException e) {
				//lancio la stampa dello stack di tracciamento nel caso di IOException
				e.printStackTrace();
				//esco dal ciclo
				//break;
			}
			if (doc != null) {
				try {
					elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;
					System.out.println("2. Elapsed Time: " + elapsedTime);
					//lancio la funzione di conversione del documento in 1 o più file PNG
					String[] infos = pservice.getName().split("\\s");
					convert(filepath, doc, Integer.parseInt(infos[1].replaceAll("\\D", "")));
					//convert(filepath, doc, 300);
				} catch (NumberFormatException e) {
					//lancio la stampa dello stack di tracciamento nel caso di NumberFormatException
					e.printStackTrace();
					//esco dal ciclo
					//break;
				}

				System.out.println("Start Printing");
				//elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;
				//System.out.println("3. Elapsed Time: " + elapsedTime);
				//lancio la funzione di stampa dei file creati in formato PNG
				try {
					//Criamo la variabile che gestisce la funzione di stampa nativa con la stampante creata e connessa in precedenza
					ZebraPrinter printer = new ZebraPrinterZpl((Connection)driverPrinterConnection);
					//elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;
					//System.out.println("Print 1. Elapsed Time: " + elapsedTime);
					//Pirmo Ciclo che gestisce la quantità di copie da stampare
					for (int cycle = 0; cycle < copie; cycle++)
						//Ciclo interno che scandisce le pagine del documento per comporre il path del file PNG da recuperare
						for (int page = 0; page < doc.getNumberOfPages(); ++page){ 
							//System.out.println("Printing Page " + (page+1) + "...");
							//Lancio della gunzione di stampa nativa 
							printer.printImage(filepath.replace(".pdf", "-") + (page+1) + ".png",8,8);
						} 
				} catch (ConnectionException e) {
					//lancio la stampa dello stack di tracciamento nel caso di ConnectionException
					e.printStackTrace();
				}catch(Exception e) {
					//lancio la stampa dello stack di tracciamento nel caso di altre Exception
					e.printStackTrace();
				}
				try {
					//elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;
					//System.out.println("4. Elapsed Time: " + elapsedTime);
					doc.close();
				} catch (IOException e) {
					//lancio la stampa dello stack di tracciamento nel caso di IOException
					e.printStackTrace();
					//esco dal ciclo
					//break;
				}
			}
			try {
				//elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;
				//System.out.println("5. Elapsed Time: " + elapsedTime);
				//chiudo la connessione con la stampante
				driverPrinterConnection.close();
			} catch (ConnectionException e) {
				//lancio la stampa dello stack di tracciamento nel caso di ConnectionException
				e.printStackTrace();
				//esco dal ciclo
				//break;
			}
		}


		System.out.println("End!");
		elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - startTime;
		System.out.println("Elapsed Time: " + elapsedTime);
	}

}
