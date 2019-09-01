package com.parrot.movietask.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.parrot.movietask.bean.IqiyiSearchTermsTV;
import com.parrot.movietask.service.S3Service;

@Service
public class S3ServiceImpl implements S3Service{
	private static Logger logger = Logger.getLogger(S3ServiceImpl.class);
    private static AmazonS3 s3 = null;
    private static final String BUCKET_NAME="techtasks";
    private static final String FILE_PREFIX = "spe-nicolas";
    private static final String LINK_STR = "/";
    private static final String FILE_POSTFIX = ".json";
    private static final String CHARSET_NAME = "UTF-8";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
    
    @Autowired  
    private Environment env;  
    
    
    public void uploadJSONFiles(List<IqiyiSearchTermsTV> tvList) throws Exception {
    	/*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (C:\\Users\\xxx\\.aws\\credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
        	logger.error(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\xxx\\.aws\\credentials), and is in valid format.",
                    e);
        }
        
       s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
        
       	String bucketName = BUCKET_NAME;
        String key = "";
        
        String s3FilePath = FILE_PREFIX+LINK_STR+dateFormat.format(new Date()) + LINK_STR ;
        String currentTime = timeFormat.format(new Date());
        String splitFileName = "";
        //Reasonable size optimized for apache spark reads, it's from the configure file.
        int splitFileSize =Integer.parseInt(env.getProperty("S3_SPARK_FILESIZE")) * 1024 * 1024;
        //The local path of saving the raw dump Vimeo API JSON 
        String filePath = env.getProperty("S3_FILE_LOCALPATH");
        //S3 file name.
        String fileName = currentTime+FILE_POSTFIX;
        //Generate the raw JSON file.
        File rawFile = saveRawJSONFile(tvList, filePath, fileName);
        
        //Read the raw JSON file
        
        byte[] fileContent= new byte[(int) rawFile.length()];
        try {
            //将文件内容读取到内存中 
            FileInputStream fis=new FileInputStream(rawFile);
            fis.read(fileContent);
            fis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        //Get file size of the raw JSON 
        long rawFileSize = rawFile.length();
        
        if(rawFileSize <= splitFileSize) {
        	//If the raw JSON file size is smaller than the configuration file size, upload directly to S3
     	   key = s3FilePath + fileName;
     	   s3.putObject(new PutObjectRequest(bucketName, key, rawFile));
     	   //Batch delete all local files.
	     	batchDeleteFiles(bucketName, key , filePath);
        }else {
        	//Otherwise, split the raw JSON file.
     	   int fileCount = 0;
     	   if(rawFileSize % splitFileSize ==0) {
    	    		fileCount = (int) (rawFileSize / splitFileSize);
	    	}else {
	    		fileCount = (int) (rawFileSize / splitFileSize)+ 1;
	    	}
     	   
     	  for (int i=0;i<fileCount;i++){
     		  splitFileName = currentTime +"-" + (i + 1) + FILE_POSTFIX;
     		  File splitFile = new File(filePath + splitFileName);
              byte[] eachContent;
              //将源文件内容复制到拆分的文件中 
              if(i!=fileCount-1){
                  eachContent = Arrays.copyOfRange(fileContent, splitFileSize*i, splitFileSize*(i+1));
              } else{
                  eachContent = Arrays.copyOfRange(fileContent, splitFileSize*i, fileContent.length);
              }
              try {
					FileOutputStream fos = new FileOutputStream(splitFile);
					fos.write(eachContent);
					fos.close();
					  
					//Upload split files.
					key = s3FilePath + splitFileName;
					s3.putObject(new PutObjectRequest(bucketName, key, splitFile));
					logger.info("Upload split file "+key+", the size is " + splitFile.length());

				}
              catch (Exception e) {
                  // TODO: handle exception 
                  e.printStackTrace();
              }
          }
     	  
	     	  //Batch delete all local files.
     	  batchDeleteFiles(bucketName, s3FilePath + currentTime +"-" + fileCount + FILE_POSTFIX , filePath);
        }
    }

    /**
     * Batch delete all local files.
     * Make sure all files are uploaded to S3 by getting the URL returned by the last file uploaded.
     * @param bucketName
     * @param key
     * @param filePath
     */
    private void batchDeleteFiles(String bucketName, String key, String filePath) {
    	GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName,  key);
		URL url = s3.generatePresignedUrl(urlRequest);
		if(url.toString().trim().length()>0) {
			File rawFilePath = new File(filePath);
			if(!rawFilePath.exists()) return;
			File[] files = rawFilePath.listFiles();
			for(File file:files) {
				if(file.isFile() || file.list()==null) {
					file.delete();
				}
			}
		}
	}

	/**
     * Save the raw JSON data collected hourly to a file
     * @param filePath
     * @param fileName
     * @return
     * @throws Exception 
     */
	private File saveRawJSONFile(List<IqiyiSearchTermsTV> tvList, String filePath, String fileName) throws Exception{
		File folder = new File(filePath);
		if (!folder.exists() && !folder.isDirectory()) {
			folder.mkdirs();
		}
		File file = new File(filePath +fileName);
		String url = env.getProperty("API_URL");
		StringBuffer sbContent = new StringBuffer();
		if(tvList != null) {
	    	for(IqiyiSearchTermsTV istTV : tvList) {
	    		//url should be as follow
	    		// url = url + "?file_id=" + istTV.getTitleId();
	    		sbContent.append(loadJson(url));
	    	}
		    Writer writer = new OutputStreamWriter(new FileOutputStream(file));
		    writer.write(sbContent.toString());
		    writer.close();
		}
        return file;
	}
	
	/**
	 *  Read API return string
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private static StringBuffer loadJson(String url) throws Exception {
		//读取url,返回json串
		StringBuffer json = new StringBuffer();
		URL oracle = new URL(url);
		URLConnection yc = oracle.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(),CHARSET_NAME));
		String inputLine = null;
		while((inputLine = in.readLine()) != null){
			json.append(inputLine);
		}
		in.close();
		return json;
	}
}
