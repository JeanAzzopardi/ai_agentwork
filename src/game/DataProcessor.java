package game;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.ListIterator;

import mixmeta4.Board;
import mixmeta4.Move;
import mixmeta4.Moves;
import mixmeta4.Piece;
import mixmeta4.PieceSet;

public class DataProcessor {

	private ArrayList<String> directories;
	private ArrayList<Move> moves;
	private Attributes atts;
	private ArrayList<DataAtom> data;
	private String directory;
	private boolean debug = true;
	private boolean writeable;
	
	public DataProcessor(ArrayList<String> directories) {
		this.directories = directories;
		this.atts = new Attributes();
		this.data = new ArrayList<DataAtom>();
		this.writeable = false;
	}
	
	public ArrayList<DataAtom> scanData() {
		for(int x = 0; x < this.directories.size(); ++x) {
			this.scanOneDirectory(this.directories.get(x));
		}
		this.writeable = true;
		return this.data;
	}
	
	public boolean writeData(String filename) {
		if(!this.writeable) {
			return false;
		}

		File file;
		int x = 0;
		do {
			file = new File(filename + "." + x);
			x++;
		} while(file.exists());
		try {
			file.createNewFile();
			file.setWritable(true, true);
		} catch (IOException e) {
			e.printStackTrace();
			if(debug) System.out.println("Unable to create file!");
		}
		FileOutputStream fos;
		PrintStream pos;
		if(file.canWrite()) {
			try {
				fos = new FileOutputStream(file, true);
				pos = new PrintStream(fos);
				pos.println(";--++ This is javadb type file");
				pos.println(";; Description : Moves mapped to pieces on specified attributes.");
				pos.println("moves-database");
				for(int z = 0; z < this.data.size(); z++) {
					if(z == 0) {
						pos.println("object name " + attributeString(this.data.get(z)) + "type symbolic");
					}
					pos.println(this.data.get(z).toString());
				}
			} catch (FileNotFoundException e) {
				if(debug) System.out.println("Failed creating FileOutputStream!");
				if(debug) e.printStackTrace();
				return false;
			}
			pos.close();
			try {
				fos.close();
			} catch (IOException e) {
				if(debug) System.out.println("Unable to close FileOutputStream!");
				if(debug) e.printStackTrace();
			}
			return true;
		} 
		return false;
	}
	
	private int convertAlphaToDigit(String x) {
		return 0;
	}
	
	private String convertDigitToAlpha(int x) {
	    String initial = "" + x;
		StringBuilder s = new StringBuilder();
		for(int y = 0; y < initial.length(); ++y) {
			s.append((char)(Character.getNumericValue(initial.charAt(y)) + (int)'a'));
		}
		return s.toString();
	}
	
	private String attributeString(DataAtom lineone) {
		String result = "";
		for(int y = 0; y < lineone.getPieceAttributesSet().size(); y++) {
			DataPieceAttributes dpa = lineone.getPieceAttributesSet().get(y);
			//result += " " + dpa.getPiece().toString() + "";
			for(int z = 0; z < dpa.getAttributes().size(); z++) {
				result +=  (dpa.getPiece().toString() + dpa.getAttributes().get(z)).toLowerCase() + this.convertDigitToAlpha(y) + " symbolic ";
			}
		}
		return result;
	}
	
	private void scanOneDirectory(String directoryname) {
		DataReader reader = new DataReader(directoryname);
		ArrayList<DataContainer> boards = reader.readData();
		PieceSet black = boards.get(0).getTheBoard().getBlackPieces();
if(debug) System.out.println("Number of blackPieces: " + black.size());
		PieceSet white = boards.get(0).getTheBoard().getRedPieces();
if(debug) System.out.println("Number of whitePieces: " + white.size());	
if(debug) System.out.println("Initialising the board iterator");
		ListIterator<DataContainer> it = boards.listIterator();
		int x = 0;
		DataContainer board = it.next();
			
		while(it.hasNext()) {
if(debug) System.out.println("Cycle x = " + x);
			Moves theMoves = board.getTheMoves();
			Move moi = null;
if(debug) System.out.println("Get the previous move...");
if(debug) System.out.println("theMoves size: " + theMoves.size());
			if(theMoves.size() > 1) {
				moi = (Move)theMoves.get(theMoves.size() - 1);
			}
if(debug) System.out.println("Current Move of interest: " + moi);
if(debug) System.out.println("Conditional entering data population loop");
			if(moi != null && board.getTheBoard().getSquare(moi.getDestination().getLocation()).isOccupiedByBlack()) {
				DataAtom datum = new DataAtom();
				datum.setKey(x);
				datum.setMove(moi);
if(debug) System.out.println("Initialising both of the Piece iterators");
				ListIterator<Piece> pieces = board.getTheBoard().getBlackPieces().listIterator();
				ListIterator<Piece> defaultset = black.listIterator();
				boolean doOther = true;
				if(pieces.hasNext()) {
					Piece piece = pieces.next();
					if(debug) System.out.println("Fill the data for each of the pieces loop");
					while(defaultset.hasNext()) {
						DataPieceAttributes dpa = new DataPieceAttributes();

						Piece defaultPiece = defaultset.next();

						if(piece.toString().compareTo(defaultPiece.toString()) != 0) {
							dpa.setPiece(defaultPiece);

							dpa.addAttribute("canBeTaken");
							dpa.addResult(false);

							dpa.addAttribute("canMove");
							dpa.addResult(false);

							dpa.addAttribute("canTake");
							dpa.addResult(false);

							dpa.addAttribute("takeKing");
							dpa.addResult(false);
							datum.addPieceAttributes(dpa);
							if(pieces.hasNext()) {
								piece = pieces.next();
							}
							//defaultPiece = defaultset.next();
							doOther = false;

						}
						if(doOther) {
							dpa.setPiece(piece);

							dpa.addAttribute("canBeTaken");
							dpa.addResult(atts.canBeTaken(board.getTheBoard(), piece));

							dpa.addAttribute("canMove");
							dpa.addResult(atts.canMove(board.getTheBoard(), piece));

							dpa.addAttribute("canTake");
							dpa.addResult(atts.canTake(board.getTheBoard(), piece));

							dpa.addAttribute("takeKing");
							dpa.addResult(atts.takeKing(board.getTheBoard()));
							datum.addPieceAttributes(dpa);
							if(pieces.hasNext()) {
								piece = pieces.next();
							}
							//defaultPiece = defaultset.next();
							
						}
						doOther = true;
					}
					if(debug) System.out.println("Number of pieces to search: " + black.size());
					if(debug) System.out.println("Number of piece data sets in DataAtom: " + datum.getPieceAttributesSet().size());
				}
				
				data.add(datum);
				

			}
			x++;
			board = it.next();

			
		}
	}
	
	public double calcUtility(Board board) {
		return 0.00;
	}
	
	public static void main(String[] args)
	{
		/*
		if(args.length != 1)
		{
			System.out.println("Usage: java game.DataProcessor DirectoryName");
			return;
		}
		*/
		String dirname = "testfiledir.7";
	//	String dirname1 = "testfiledir.1";
	//	String dirname2 = "testfiledir.2";
	//	String dirname3 = "testfiledir.3";
	//	String dirname4 = "testfiledir.4";
		ArrayList<String> directories = new ArrayList<String>();
		directories.add(dirname);
//		directories.add(dirname1);
//		directories.add(dirname2);
//		directories.add(dirname3);
//		directories.add(dirname4);
		System.out.println("Running the Data Processor over '" + dirname + "'.");
		DataProcessor d = new DataProcessor(directories);
		ArrayList<DataAtom> temp = d.scanData();
		d.writeData(dirname + ".results");
		System.out.println("Size of temp: " + temp.size());
		for(int x = 0; x < temp.size(); x++) {
			System.out.println("Board: " + x);
			DataAtom atts = temp.get(x);
			System.out.println("\tMove: " + atts.getMove().toString());
			for(int y = 0; y < atts.getPieceAttributesSet().size(); y++) {
				DataPieceAttributes dpa = atts.getPieceAttributesSet().get(y);
				System.out.println("\t\tPiece: " + dpa.getPiece().toString());
				for(int z = 0; z < dpa.getAttributes().size(); z++) {
					System.out.println("\t\t\t" + dpa.getAttributes().get(z) + " : " + dpa.getResults().get(z));
				}
			}
		}
	}
}