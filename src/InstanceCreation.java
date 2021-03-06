import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;




import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import dataAccess.*;
import HadithRDFconvertor.*;

/**
 * 
 */

/**
 * @author Bushra
 *
 */
public class InstanceCreation  {

	/**
	 * @param args
	 */
	
	public static String SOURCE_FILE;
	public static String OutputFile;
	public static OWLOntology factoryOnt;
	private static OWLOntology owlOntology;
	private static FactoryHadithRDF hadithFactory;
	private static OWLOntologyManager manager;
	public static Connection conn = null ;
	public static Statement st = null;
	public InstanceCreation(String SOURCE_FILE, String OutputFile){
		this.SOURCE_FILE=SOURCE_FILE;
		this.OutputFile = OutputFile;
		
	}
	

	public static void InitializeHadithEngine() {

		try {
			// mapping of imported Ontologies 
			File file = new File("C:\\Users\\Bushra\\Desktop\\ExampleProject\\quranOntology.owl");
			File file2 = new File("C:\\Users\\Bushra\\Desktop\\ExampleProject\\qvoc.owl.ttl");
			manager = OWLManager.createOWLOntologyManager();
			IRI iri=IRI.create("http://quranontology.com/Resource/");
			IRI iri2 = IRI.create("http://www.nlp2rdf.org/quranvocab#");
			manager.addIRIMapper(new SimpleIRIMapper(iri, IRI.create(file)));
			manager.addIRIMapper(new SimpleIRIMapper(iri2, IRI.create(file2)));

			// Load Ontology From File
			owlOntology = manager.loadOntologyFromOntologyDocument(new 
					FileInputStream(SOURCE_FILE));
			hadithFactory = new FactoryHadithRDF(owlOntology);
		}

		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void createConnection(){

		try {
			conn = connectionFactory.createConnection();
			st = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void closeConnection(){
		try {
			if (conn != null) {
				conn.close();
			}
			if (st != null) {
				st.close();
			}
		} catch (SQLException sqlee) {
			sqlee.printStackTrace();
		}
	}
	// ******************* Display the Ontology *****************
	public static void displayIndv(){
		Collection Instances=hadithFactory.getAllHadithInstances();
		Iterator itr=Instances.iterator();
		while(itr.hasNext()){
			System.out.println(itr.next());
		}
	}

	// ******************* Save the Ontology *****************
	public static void saveOnt(){
		factoryOnt=hadithFactory.getOwlOntology();
		File fileformated = new File(OutputFile);
		//Get the Ontology format
		OWLOntologyFormat format = manager.getOntologyFormat(factoryOnt);
		OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
		if (format.isPrefixOWLOntologyFormat()) { 
			owlxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat()); 
		}
		try {
			manager.saveOntology(factoryOnt, owlxmlFormat, IRI.create(fileformated.toURI()));
		} catch (OWLOntologyStorageException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	// ******************* Helping function: to get number of rows in a sql table *****************
	public static int rowCount(String tableName){
		int count =0;
		try {

			ResultSet r = st.executeQuery("SELECT COUNT(*) AS rowcount FROM "+tableName);
			r.next();
			count = r.getInt("rowcount");
			r.close();		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	// ******************* Create Collection Instances *****************
	public static void CollectionInstance(){

		int row = rowCount("collection");
		for(int i = 1; i<=row; i++){
			CollectionDataAccess cda = new CollectionDataAccess();
			CollectionData cd = cda.setCollectionAtt(i, conn, st);
			String instanceName = "collection"+i;
			// Create Collection Instance and add its data properties
			HadithCollection collectionInstance = hadithFactory.createHadithCollection(instanceName);
			//	collectionInstance.addHadithVolumeNo(cd.getVolNo());
			collectionInstance.addLabel(cd.getCollectionEngName()+"@en");
			collectionInstance.addLabel(cd.getCollectionArabName()+"@ar");

		}
	}
	// ******************* Create Book Instances *****************
	public static void BookInstance(){
		int row = rowCount("books");
		for(int i = 1; i<=row; i++){
			BookDataAccess bda = new BookDataAccess();
			BookData bd = bda.setBookAtt(i, conn, st);
			String instanceName = "book"+i;
			// Create Book Instance and add its data properties
			HadithBook bookInstance = hadithFactory.createHadithBook(instanceName);
			if(bd.getStartHadithNo()!=null){
				bookInstance.addStartingHadithNo(bd.getStartHadithNo());
				bookInstance.addEndingHadithNo(bd.getEndHadithNo());
			}
			bookInstance.addCollectionName(bd.getCollectionName());
			bookInstance.addHadithBookNo(bd.getBookNo());
			bookInstance.addHadithBookUrl("");
			bookInstance.addLabel(bd.getBookTitleA()+"@ar");
			bookInstance.addLabel(bd.getBookTitleE()+"@en");
		}
	}

	// ******************* Create Chapter Instances *****************
	public static void ChapterInstance()
	{
		int row = rowCount("chapter");
		for(int i=1; i<=row; i++){
			ChapterDataAccess cda = new ChapterDataAccess();
			ChapterData cd = cda.setChapterAtt(i, conn, st);

			String InstanceName = "chapter"+i;
			// Create Chapter Instance and add its data properties
			HadithChapter chapterInstance = hadithFactory.createHadithChapter(InstanceName);
			if(cd.getChapIntro()!=null){
				chapterInstance.addHadithChapterIntro(cd.getChapIntro());
			}
			chapterInstance.addHadithChapterNo(cd.getChapterNo());
			chapterInstance.addLabel(cd.getChapLabelArab()+"@ar");
			chapterInstance.addLabel(cd.getChapLabelEng()+"@en");
			chapterInstance.addCollectionName(cd.getCollectionName());
			chapterInstance.addHadithBookNo(cd.getBookId());

		}
	}

	// ******************* Create Hadith Instances *****************
	public static void HadithInstance(){
		int row = rowCount("hadith2");
		for(int i=1; i<=row; i++){
			HadithDataAccess hda = new HadithDataAccess();
			HadithData hd = hda.setHadithAtt(i, conn, st);
			String instanceName = "hadith"+i;
			// Create Hadith Instance and add its data properties
			Hadith hadithInstance =	hadithFactory.createHadith(instanceName);
			hadithInstance.addDeprecatedHadithNo(hd.getDeprecatedHadithNo());
			hadithInstance.addHadithReferenceNo(hd.getHadithRefNo());
			hadithInstance.addHadithUrl(hd.getHadithUrl());
			hadithInstance.addInBookNo(hd.getInbookHadithNo());
			hadithInstance.addHadithBookNo(hd.getBookId());
			hadithInstance.addCollectionName(hd.getCollectionName());
			hadithInstance.addHadithChapterNo(hd.getChapterId());

		}
	}
	// ******************* Create Matan Instances *****************
	public static void MatanInstance(){
		int row = rowCount("hadith2");
		for(int i = 1; i<=row; i++){
			MatanDataAccess mda = new MatanDataAccess();
			MatanData md = mda.setMatanAtt(i, conn, st);;
			String instanceName = "matan"+i;
			HadithMatan matanInstance = hadithFactory.createHadithMatan(instanceName);
			matanInstance.addHadithText("See Hadith URL");

			/* not adding actual text due to copyright issue 
			 * matanInstance.addHadithText(md.getHadithTextArab()+"@ar");
			matanInstance.addHadithText(md.getHadithTextEng()+"@en");
			 */
			hadithFactory.getHadith("hadith"+i).addHasPart(matanInstance);
			matanInstance.addIsPartOf(hadithFactory.getHadith("hadith"+i));
		}
	}

	// ******************* Create Sanad Instances *****************
	public static void sanadInstance(){
		int row = rowCount("hadith2");
		for(int i =1; i<=row; i++){
			SanadData sd = SanadDataAccess.setSanadAtt(i, conn, st);;
			String instanceName = "sanad"+i;
			// Create Sanad Instance and add its data properties
			HadithSanad sanadInstance = hadithFactory.createHadithSanad(instanceName);
			sanadInstance.addNarratorChain("See Hadith URL");
			/* not adding actual text due to copyright issue 
			 * sanadInstance.addNarratorChain(sd.getSanadTextArab()+"@ar");
			sanadInstance.addNarratorChain(sd.getSanadTextEng()+"@en");
			 */
			hadithFactory.getHadith("hadith"+i).addHasPart(sanadInstance);
			sanadInstance.addIsPartOf(hadithFactory.getHadith("hadith"+i));
		}
	}

	// ******************* Create Verse Instances *****************
	public static void verseInstance(){
		int row = rowCount("hadith2");
		for(int i=1; i<=row; i++){
			VerseData vd = VerseDataAccess.setVerseData(i, conn, st);
			Hadith hadithInstance = hadithFactory.getHadith("hadith"+i);
			ArrayList<Integer> verseIndexE = vd.getVerseIndexE();
			ArrayList<Integer> verseIndexS = vd.getVerseIndexS();
			ArrayList<Integer> chapterIndex = vd.getChapterIndex();
			int numOfVerses = verseIndexE.size();
			if(numOfVerses!=0){
				if(numOfVerses>1){ 	
					for(int j = 0; j<numOfVerses; j++){
						Verse verseInstance = hadithFactory.createVerse("Verse"+chapterIndex.get(j)+verseIndexS.get(j));	
						verseInstance.addVerseIndex(verseIndexS.get(j));
						verseInstance.addChapterIndex(chapterIndex.get(j));

						hadithInstance.addContainsMentionOf(verseInstance);
						verseInstance.addMentionedIn(hadithInstance);
					}
				}
				else{
					Verse verseInstance = hadithFactory.createVerse("Verse"+chapterIndex.get(0)+verseIndexS.get(0));	
					verseInstance.addVerseIndex(verseIndexS.get(0));
					verseInstance.addChapterIndex(chapterIndex.get(0));
					hadithInstance.addContainsMentionOf(verseInstance);
					verseInstance.addMentionedIn(hadithInstance);
				}
			}
		}
	}

	//  ****************Helping function for CityInstance ****************
	// Read City List from a text file and make a comparison with the Hadith text
	public static ArrayList<String> readCityList(){
		ArrayList<String> cityList = new ArrayList();
		String line=null;
		try
		(BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Bushra\\Desktop\\cityList.txt"))) {
			while ((line=br.readLine())!=null) {
				cityList.add(line.trim());
				//  System.out.println(br.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cityList;
	}
	// ******************* Create City Instances *****************
	public static void cityInstance(){
		ArrayList<String> cities =readCityList();
		for(int c=0; c<cities.size(); c++){
			if(cities.get(c).contains(",")){
				List<String> list = Arrays.asList(cities.get(c).trim().split(","));
				for(int j= 0; j<list.size(); j++){
					City cityInstance = hadithFactory.createCity(list.get(j).trim());
					cityInstance.addLabel(list.get(j).trim());
				}
			}else {
				City cityInstance = hadithFactory.createCity(cities.get(c).trim());
				cityInstance.addLabel(cities.get(c).trim());
			}
		}

		int row = rowCount("hadith2");
		for(int i=1; i<=row; i++){
			CityData cd = CityDataAccess.setCityAtt(i, conn, st);
			Hadith hadithInstance = hadithFactory.getHadith("hadith"+i);

			ArrayList<String> cityTemp = cd.getCityName();
			if(!(cityTemp.isEmpty())){
				for(int j=0; j<cityTemp.size(); j++){
					City cityInstance = hadithFactory.getCity(cityTemp.get(j));
					hadithInstance.addContainsMentionOf(cityInstance);
					cityInstance.addMentionedIn(hadithInstance);
				}

			}
		}
	}

	public static void AssignObjectProperties(){
		// Get Size of Instances of each Class
		int h_size=hadithFactory.getAllHadithInstances().size();
		int ch_size = hadithFactory.getAllHadithChapterInstances().size();
		int b_size = hadithFactory.getAllHadithBookInstances().size();
		int c_size = hadithFactory.getAllHadithCollectionInstances().size();

		//*************** Check for connections to add Object properties ***********
		//--------------- 1- HadithCollection hasPart HadithBook ---------------\\
		//--------------- 2- HadithBook isPartOf HadithCollection ---------------\\

		for (int i=1; i<=c_size; i++)
		{
			HadithCollection c = hadithFactory.getHadithCollection("collection"+i);
			String col =c.getLabel().iterator().next().toString();
			for(int j=1; j<=b_size; j++)
			{
				HadithBook b =  hadithFactory.getHadithBook("book"+j);
				if(col.equals(b.getCollectionName().iterator().next()+"@en")){
					b.addIsPartOf(c);
					c.addHasPart(b);
				}
			}
		}
		//--------------- 3- HadithBook hasPart HadithChapter ---------------\\
		//--------------- 4- HadithChapter isPartOf HadithBook ---------------\\

		for(int i=1; i<=b_size; i++){
			HadithBook b = hadithFactory.getHadithBook("book"+i);
			for(int j=1; j<=ch_size; j++){
				HadithChapter ch = hadithFactory.getHadithChapter("chapter"+j);
				//System.out.println(b.getHadithBookNo());
				if(ch.getCollectionName().equals(b.getCollectionName())&&
						ch.getHadithBookNo().equals(b.getHadithBookNo()))
				{
					ch.addIsPartOf(b);
					b.addHasPart(ch);
				}
			}
		}

		//--------------- 5- HadithChapter hasPart Hadith ---------------\\
		//--------------- 6- Hadith isPartOf HadithChapter ---------------\\

		for(int i=1; i<=ch_size; i++){
			HadithChapter ch = hadithFactory.getHadithChapter("chapter"+i);
			for(int j=1; j<=h_size; j++){
				Hadith h = hadithFactory.getHadith("hadith"+j);
				if(ch.getCollectionName().equals(h.getCollectionName()) &&
						ch.getHadithBookNo().equals(h.getHadithBookNo())
						&& ch.getHadithChapterNo().equals(h.getHadithChapterNo()))
				{
					ch.addHasPart(h);
					h.addIsPartOf(ch);
				}
			}
		}

	}
}