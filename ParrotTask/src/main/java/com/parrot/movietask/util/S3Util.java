package com.parrot.movietask.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * S3 Utilities
 * @author cuilijian
 *
 */
public class S3Util {
	private static Logger logger = Logger.getLogger(S3Util.class);
	private static AmazonS3 s3 = null;
    private static final String BUCKET_NAME="techtasks";
    private static final String FILE_PREFIX = "spe-nicolas";
    private static final String LINK_STR = "/";
    private static final String FILE_POSTFIX = ".csv";
    private static final String CHARSET_NAME = "UTF-8";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
    
    /***
     * s3 initial method
     * */
    private static void initS3() {
    	/*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (C:\\Users\\xxx\\.aws\\credentials).
         */
        if(s3 == null){
            try {
                AWSCredentials  credentials = new ProfileCredentialsProvider().getCredentials();
                s3 = AmazonS3ClientBuilder.standard()
    	                .withCredentials(new AWSStaticCredentialsProvider(credentials))
    	                .withRegion(Regions.US_EAST_1)
    	                .build();
            } catch (Exception e) {
                logger.error("Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (C:\\Users\\xxx\\.aws\\credentials), and is in valid format.", e);
            }
        }
    }

    /**
     * upload a file to s3
     * @throws IOException
     */
    public  static  boolean uploadFile(File file, String bucketName,Date now) throws IOException {
            // 判断s3桶是否存在
            initS3();
        try{
        	String key = FILE_PREFIX+LINK_STR+dateFormat.format(new Date()) + LINK_STR + timeFormat.format(new Date());
            s3.putObject(new PutObjectRequest(bucketName, key, file));
        } catch (Exception e) {
        	logger.error("Upload an object to your bucket error:{}" + e);
            return false;
        }
        return true;
    }

   
    /**
     * Write files in multithread
     */
    
    public  static void saveFileToS3(String content, String bucketName) {
        try {
            Date now = new Date();
            File tempFile = createSampleFile(content, now);
            if(null!=tempFile){
                boolean result = uploadFile(tempFile, bucketName, now);
                if(result){
                    tempFile.delete();
                }
            }else{
                logger.info("Failed to create local file");
            }
        } catch (IOException e) {
        	logger.error("Write the s3 file system exception :{}" + e);
        }
    }
    
    public static File createSampleFile(String content,Date now) throws IOException {
        File file = File.createTempFile(timeFormat.format(new Date()), FILE_POSTFIX);
        file.deleteOnExit();
        Writer writer = new OutputStreamWriter(new FileOutputStream(file),CHARSET_NAME);
        writer.write(content);
        writer.close();
        return file;
    }

    
}
