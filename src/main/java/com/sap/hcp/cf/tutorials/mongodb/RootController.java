package com.sap.hcp.cf.tutorials.mongodb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

// TODO: Auto-generated Javadoc
/**
 * The Class RootController.
 */
@Controller
public class RootController {

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(RootController.class);

	/** The mongo template. */
	@Autowired
	MongoTemplate mongoTemplate;

	/** The db collection. */
	DBCollection dbCollection;

	/** The Constant COLLECTION_NAME. */
	private final static String COLLECTION_NAME = "CloudFoundryData.files";

	/** The Constant BUCKET_NAME. */
	private final static String BUCKET_NAME = "CloudFoundryData";


	/**
	 * Index.
	 *
	 * @return the string
	 */
	@RequestMapping("/")
	public String index() {

		List<String> fileNames = new ArrayList<String>();
		dbCollection = mongoTemplate.getCollection(COLLECTION_NAME);
		DBCursor cursor = dbCollection.find();
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			fileNames.add(obj.get("filename").toString());
		}
		return "index.html";
		//return "NewFile";

	}


	/**
	 * Upload file.
	 *
	 * @param file the file
	 * @param imageName the image name
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@RequestMapping(value="/fileUpload",method = RequestMethod.POST)
	public @ResponseBody String uploadFile(
			@RequestParam("file")  MultipartFile file,
			@RequestParam("imageName")  String imageName
			) throws IOException {
		
		File convFile = convert(file);		
		dbCollection = mongoTemplate.getCollection(COLLECTION_NAME);	
		long count = dbCollection.getCount();	
		log.info("Object count in 'test' collection before insert: " + count + "<br/> Inserting one object.<br/>");
		DB db = mongoTemplate.getDb();
		GridFS gridFs = new GridFS(db,BUCKET_NAME); 
		upload(gridFs, convFile,imageName);
		return "File has been uploaded successfully";
	}



	/**
	 * Download file.
	 *
	 * @param imageName the image name
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@RequestMapping(value="/fileDownload",method = RequestMethod.GET)
	public @ResponseBody String downloadFile(
			@RequestParam String imageName
			) throws IOException {

		DB db = mongoTemplate.getDb();
		GridFS gridFs = new GridFS(db,BUCKET_NAME); 
		download(gridFs, imageName);
		return "success.html";
	}




	/**
	 * Convert.
	 *
	 * @param file the file
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static File convert(MultipartFile file) throws IOException
	{    
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile(); 
		FileOutputStream fos = new FileOutputStream(convFile); 
		fos.write(file.getBytes());
		fos.close(); 
		return convFile;
	}

	/**
	 * Upload.
	 *
	 * @param gridFs the grid fs
	 * @param csvFile the csv file
	 * @param imageName the image name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	// Upload file
	public static void upload(GridFS gridFs, File csvFile, String imageName) throws IOException{
		GridFSInputFile gridFsInputFile = gridFs.createFile(csvFile);
		gridFsInputFile.setFilename(imageName); 
		gridFsInputFile.save(); 
	}

	/**
	 * Download.
	 *
	 * @param gridFs the grid fs
	 * @param imageName the image name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	//Download file
	public static void download(GridFS gridFs, String imageName) throws IOException{
		GridFSDBFile outputImageFile = gridFs.findOne(imageName);
		String home = System.getProperty("user.home");
		String outcsvLocation = home+"/Downloads/output7.csv";
		outputImageFile.writeTo(new File(outcsvLocation));
	}

	/**
	 * Delete.
	 *
	 * @param gridFs the grid fs
	 * @param imageName the image name
	 */
	//Delete file
	public static void delete(GridFS gridFs, String imageName){
		gridFs.remove(gridFs.findOne(imageName) );
	}

}
