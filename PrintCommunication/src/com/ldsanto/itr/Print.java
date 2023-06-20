package com.ldsanto.itr;

import java.io.IOException;
import java.sql.SQLException;

import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class EPrint {

	public static void main(String[] args) {
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		int i = 0;
		boolean printed = false;
		
		if (printServices.length == 0)
			//Lancio un'eccezione se l'array creato è vuoto
			throw new RuntimeException("No printer services available."); 

		while(i<printServices.length && !printed) {
			System.out.println("Inizio ricerca stampanti");
			if (printServices[i].getName().toUpperCase().contains("GODEX")) {
				System.out.println("Inizializzo costruttore Godex");
				Godex g = new Godex();
				String redFPath = g.reducePDF(args[0]);
				/*try {
					g.checkprint(redFPath);
				} catch (PrintException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(redFPath);*/
				try {
					System.out.println("Inizio stampa");
					g.print(redFPath, printServices[i], Integer.parseInt(args[1]));
					//g.print(args[2], printServices[i], Integer.parseInt(args[1]));
					printed = true;
					
				} catch (NumberFormatException | PrintException | IOException e) { 
					// TODO Auto-generated catch block
					printed = false;
					e.printStackTrace();
				}
				i++;
			}else if (printServices[i].getName().toUpperCase().contains("ZEBRA")) {
				System.out.println("Inizializzo costruttore Zebra");
				Zebra z = new Zebra();
				try {
					z.print(args[0], printServices[i], Integer.parseInt(args[1]));
				} catch (NumberFormatException | PrintException | IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
				}
				i++;
			}else {
				i++;
			}
			
			
		}
	}


}
