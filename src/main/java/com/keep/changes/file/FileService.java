package com.keep.changes.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	String uploadImage(String path, MultipartFile file) throws IOException;

	String updateImage(String publicId, MultipartFile file) throws IOException;

	InputStream getResource(String path, String fileName) throws FileNotFoundException;

	void deleteFile(String path, String fileName) throws IOException;

}
