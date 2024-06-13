package com.keep.changes.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

	@Override
	public String uploadImage(String path, MultipartFile file) throws IOException {
		String originalFilename = file.getOriginalFilename();
		String randomUuid = UUID.randomUUID().toString();
		String newFileName = randomUuid.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));

		String filePath = path + File.separator + newFileName;

		File f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
		return newFileName;
	}

	@Override
	public InputStream getResource(String path, String fileName) throws FileNotFoundException {
		String fullPath = path + File.separator + fileName;
		InputStream inputStream = new FileInputStream(fullPath);
		return inputStream;
	}

	public void deleteFile(String path, String fileName) throws IOException {

		String fullPath = path + File.separator + fileName;
		Path filePath = Paths.get(fullPath);
		try {
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new IOException("Could not delete File", e);
		}

	}

	@Override
	public String updateImage(String publicId, MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
